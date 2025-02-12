package com.adr.opsc7312_poe

import android.app.TimePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.TextUtils.split
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageRoutine : AppCompatActivity() {
    private val serviceRoutine = ServiceRoutine()
    private lateinit var btnSaveRoutine: Button
    private lateinit var switch:Switch
    private lateinit var displayName: TextView
    private lateinit var btnBackButton: ImageButton
    private lateinit var turnOff:Button
    private lateinit var startingTime:Button
    private var routineID = 0
    private val calender = Calendar.getInstance()
    private val formatter2 = SimpleDateFormat("HH:mm", Locale.UK)
    private var currentHour = 0
    private var currentMinutes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_routine)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnSaveRoutine = findViewById<Button>(R.id.btnSaveRoutineRT)
        switch = findViewById<Switch>(R.id.switch1RT)
        routineID = intent.getIntExtra("Routine_ID", 0)
        displayName = findViewById<TextView>(R.id.lblRoutineNameRT)
        btnBackButton = findViewById(R.id.imgBackRT)
        turnOff = findViewById(R.id.btnTurnOffRT)
        UpdateUI()

        turnOff.setOnClickListener {
            lifecycleScope.launch {
                try {
                    if (!isConnected(this@ManageRoutine)){
                        Toast.makeText(this@ManageRoutine, "Unavailable while offline", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val service = ServiceRoutine()
                        service.ToggleRoutineStatus(this@ManageRoutine, routineID)
                        Toast.makeText(this@ManageRoutine, "Devices are turned off", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("turnOff", "Error: ${e.message}")
                }
            }
        }

        btnSaveRoutine.setOnClickListener {
            updateRoutine()
        }

        btnBackButton.setOnClickListener {
            finish()
        }

        startingTime = findViewById<Button>(R.id.btnSetTimeRT)

        startingTime.setOnClickListener {
            displayFormattedTime1(calender.timeInMillis)
            TimePickerDialog(
                this,
                object : TimePickerDialog.OnTimeSetListener {
                    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                        calender.apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }
                        displayFormattedTime1(calender.timeInMillis)
                    }
                },
                calender.get(Calendar.HOUR_OF_DAY),
                calender.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun updateRoutine() {
        val uniqueWorkerName = "${routineID}${displayName.text}Worker"
        val newSchedule = String.format("%02d:%02d", currentHour, currentMinutes)
        Log.d("ManageRoutine", "Updating routine with worker: $uniqueWorkerName")

        lifecycleScope.launch {
            try {
                if (!isConnected(this@ManageRoutine)){
                    serviceRoutine.UpdateRoutineScheduleOffline(this@ManageRoutine,routineID, currentMinutes, currentHour)
                    Toast.makeText(this@ManageRoutine, "Routine activated with new schedule", Toast.LENGTH_SHORT).show()
                }
                else{
                    // Update routine status and schedule in the database
                    serviceRoutine.UpdateRoutineStatus(routineID, switch.isChecked)
                    serviceRoutine.UpdateRoutineSchedule(routineID, newSchedule)

                    // Handle worker logic based on the switch status
                    if (switch.isChecked) {
                        cancelUniqueRoutineWork(this@ManageRoutine, uniqueWorkerName)
                        ServiceScheduleWorker(this@ManageRoutine, currentHour, currentMinutes, uniqueWorkerName, routineID)
                        Log.d("updateRoutine", "Worker recreated for active routine")
                        Toast.makeText(this@ManageRoutine, "Routine activated with new schedule", Toast.LENGTH_SHORT).show()
                    } else {
                        cancelUniqueRoutineWork(this@ManageRoutine, uniqueWorkerName)
                        Log.d("updateRoutine", "Worker cancelled for inactive routine")
                        Toast.makeText(this@ManageRoutine, "Routine deactivated", Toast.LENGTH_SHORT).show()
                    }
                    UpdateUI()
                }
            } catch (e: Exception) {
                Log.e("ManageRoutine", "Error updating routine: ${e.message}")
                Toast.makeText(this@ManageRoutine, "Error updating routine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun UpdateUI() {
        lifecycleScope.launch {
            try {
                val routine = serviceRoutine.GetRoutineById(routineID)
                routine?.let {
                    displayName.text = it.Name
                    switch.isChecked = it.Status
                    startingTime.text = it.Scheduling
                    it.Scheduling?.let { schedule ->
                        val timeParts = schedule.split(":")
                        if (timeParts.size == 2) {
                            currentHour = timeParts[0].toIntOrNull() ?: 0
                            currentMinutes = timeParts[1].toIntOrNull() ?: 0
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ManageRoutine", "Error updating UI: ${e.message}")
            }
        }
    }

    fun displayFormattedTime1(timestamp: Long) {
        findViewById<TextView>(R.id.btnSetTimeRT).text = formatter2.format(timestamp)
        // Set the currentHour and currentMinutes using Calendar
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        currentMinutes = calendar.get(Calendar.MINUTE)

        Log.d("deeztimes", "currentHour: $currentHour, currentMinutes: $currentMinutes")
        Log.i("Formatting", timestamp.toString())
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}