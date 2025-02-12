package com.adr.opsc7312_poe

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotificationsPage : AppCompatActivity() {

    private lateinit var btnBackButton: ImageButton
    private lateinit var switchNotifications: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        switchNotifications = findViewById(R.id.switchNotificationsOnOff)

        // Get the current setting from SharedPreferences
        val sharedPref = getSharedPreferences("notif_pref", MODE_PRIVATE)
        val notificationsEnabled = sharedPref.getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = notificationsEnabled

        // Set listener for switch changes
        switchNotifications.setOnCheckedChangeListener {_, isChecked ->
            // Save the new setting to SharedPreferences
            val editor = sharedPref.edit()
            editor.putBoolean("notifications_enabled", isChecked)
            editor.apply()
        }

        btnBackButton = findViewById(R.id.btnBackNP)

        btnBackButton.setOnClickListener{
            finish()
        }
    }
}