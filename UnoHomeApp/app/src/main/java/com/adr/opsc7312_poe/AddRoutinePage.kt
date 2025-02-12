package com.adr.opsc7312_poe

import android.app.TimePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.widget.TextViewCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit


class AddRoutinePage : AppCompatActivity() {
    private lateinit var lstDevices: LinearLayout
    private lateinit var btnCreateRoutine: Button
    private lateinit var btnBackButton: ImageButton
    private val serviceRoutine = ServiceRoutine()
    private val serviceDevice = ServiceDevice()
    private val selectedDeviceIds = mutableListOf<Int>()
    private val calender = Calendar.getInstance()
    private val formatter2 = SimpleDateFormat("HH:mm", Locale.UK)
    private var currentHour = 0
    private var currentMinutes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_routine_page)

        lstDevices = findViewById(R.id.lstDevices)
        btnCreateRoutine = findViewById(R.id.btnCreateRoutine)
        btnBackButton = findViewById(R.id.btnBackARP)


        fetchAndDisplayDevices()

        btnCreateRoutine.setOnClickListener {
            createRoutineWithSelectedDevices()
        }

        btnBackButton.setOnClickListener {
            finish()
        }

        var startingTime = findViewById<Button>(R.id.btnSetTime)

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

    private fun fetchAndDisplayDevices() {
        lifecycleScope.launch {
            try {
                val devices: List<ParseDevice>?
                if (!isConnected(this@AddRoutinePage)){
                    devices = serviceDevice.OfflineDevices()
                }
                else{
                    devices = serviceDevice.GetAllDevices()
                }

                devices?.let {
                    for (device in it) {
                        val deviceLayout = createDeviceLayout(device)
                        lstDevices.addView(deviceLayout)
                    }
                }
            } catch (e: Exception) {
                Log.e("AddRoutinePage", "Error fetching devices: ${e.message}")
            }
        }
    }

    private fun createDeviceLayout(device: ParseDevice): View {
        val deviceLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }

        val deviceName = TextView(this).apply {
            text = device.Name
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        TextViewCompat.setTextAppearance(deviceName, R.style.TEXT)
        val deviceSwitch = Switch(this).apply {
            isChecked = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDeviceIds.add(device.DeviceId)
                } else {
                    selectedDeviceIds.remove(device.DeviceId)
                }
            }
        }

        deviceLayout.addView(deviceName)
        deviceLayout.addView(deviceSwitch)

        return deviceLayout
    }

    private fun createRoutineWithSelectedDevices() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        val name = findViewById<EditText>(R.id.edtRoutineName).text.toString()
        val schedule = "$currentHour:$currentMinutes"

        if (name.isEmpty()) {
            // Show message
            Toast.makeText(this, "Please enter a routine name.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if selectedDeviceIds is empty
        if (selectedDeviceIds.isEmpty()) {
            // Show message
            Toast.makeText(this, "Please select at least one device.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (!isConnected(this@AddRoutinePage)){
                    val routineID = serviceRoutine.AddRoutine(
                        this@AddRoutinePage ,name, "DescriptionHere", true,
                        schedule, false, userID, selectedDeviceIds.toTypedArray(), currentMinutes, currentHour)
                    finish()
                }
                else{
                    val routineID = serviceRoutine.AddRoutine(
                        this@AddRoutinePage ,name, "DescriptionHere", true,
                        schedule, false, userID, selectedDeviceIds.toTypedArray(), currentMinutes, currentHour)

                    // add background worker for routine
                    // "${automationId}${automationName}Worker"    -> naming scheme for RoutineNameHere, must match this
                    val uniqueName = "${routineID}${name}Worker"
                    ServiceScheduleWorker(this@AddRoutinePage, currentHour, currentMinutes, uniqueName, routineID!!)
                    Log.d("AddRoutinePage", "Routine created with devices: $selectedDeviceIds")
                    finish()
                }
            } catch (e: Exception) {
                Log.e("AddRoutinePage", "Error creating routine: ${e.message}")
            }
        }
    }

    fun displayFormattedTime1(timestamp: Long) {
        findViewById<TextView>(R.id.btnSetTime).text = formatter2.format(timestamp)
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
