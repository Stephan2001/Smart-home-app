package com.adr.opsc7312_poe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ManageDevice : AppCompatActivity() {

    private lateinit var tvDeviceName: TextView
    private lateinit var switchToggleStatus: Switch
    private lateinit var btnRemoveDevice: Button
    private lateinit var btnBackButton: ImageButton
    private val serviceDevice = ServiceDevice()
    private var deviceId: Int = 0
    private val CHANNEL_ID = "device_notifications"
    private val NOTIFICATION_PERMISSION_CODE = 1001

    //bool flag to prevent recursion on switch
    private var isSwitchChanging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device_detail)

        tvDeviceName = findViewById(R.id.lblDeviceName)
        switchToggleStatus = findViewById(R.id.switch3)
        btnRemoveDevice = findViewById(R.id.btnRemoveDevice)
        btnBackButton = findViewById(R.id.btnBackMDP)

        // getting device id from the devicespage
        deviceId = intent.getIntExtra("DEVICE_ID", 0)

        // load devices and their status
        loadDeviceDetails()

        // toggle status
        switchToggleStatus.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchChanging) {
                changeDeviceStatus(isChecked)
            }
        }

        // remove device from profile
        btnRemoveDevice.setOnClickListener {
            removeDeviceFromProfile()
        }

        btnBackButton.setOnClickListener {
            finish()
        }

        createNotificationChannel()
    }

    private fun loadDeviceDetails() {
        lifecycleScope.launch {
            try {
                // get device and its status
                val device = serviceDevice.GetDeviceById(this@ManageDevice, deviceId)
                val status = serviceDevice.GetDeviceStatus(this@ManageDevice, deviceId)

                // updating UI (name and status toggle)
                if (device != null) {
                    tvDeviceName.text = device.Name
                    // set switch state based on the device status.
                    isSwitchChanging = true
                    switchToggleStatus.isChecked = status == true
                    isSwitchChanging = false
                }
            } catch (e: Exception) {
                Log.e("ManageDevice", "Error loading device details: ${e.message}")
            }
        }
    }

    private fun changeDeviceStatus(turnOn: Boolean) {
        lifecycleScope.launch {
            try {
                // api to toggle device status
                serviceDevice.ChangeDeviceStatus(this@ManageDevice, deviceId)

                // fetch status after toggle
                val updatedStatus = serviceDevice.GetDeviceStatus(this@ManageDevice, deviceId)

                // update switch on UI
                isSwitchChanging = true
                switchToggleStatus.isChecked = updatedStatus == true
                isSwitchChanging = false


                // show notification
                if (updatedStatus == true) {
                    showNotification("Device Status Changed", "Device Turned On")
                } else {
                    showNotification("Device Status Changed", "Device Turned Off")
                }


            } catch (e: Exception) {
                Log.e("ManageDevice", "Error changing device status: ${e.message}")
            }
        }
    }
    private fun removeDeviceFromProfile() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                serviceDevice.RemoveDevice(this@ManageDevice, userID, deviceId)
                //redirect
                finish()
            } catch (e: Exception) {
                Log.e("ManageDevice", "Error removing device: ${e.message}")
            }
        }
    }


    //notifications
    //https://developer.android.com/develop/ui/views/notifications/build-notification
    private fun showNotification(title: String, message: String) {
        // Check if notifications are enabled
        val sharedPref = getSharedPreferences("notif_pref", MODE_PRIVATE)
        val notificationsEnabled = sharedPref.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) {
            // Notifications are disabled, do not show notification
            return
        }
        //checks notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE)
                return
            }
        }

        val notificationIntent = Intent(this, DevicesPage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.uno_home_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Device Status"
            val descriptionText = "Notification for device status change"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // notification permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("ManageDevice", "Notification permission granted")
            } else {
                Log.e("ManageDevice", "Notification permission denied")
            }
        }
    }
}
