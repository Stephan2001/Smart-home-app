package com.adr.opsc7312_poe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddThemePage : AppCompatActivity() {
    private lateinit var lstAddDevices: LinearLayout
    private lateinit var btnCreateTheme: Button
    private lateinit var btnBackButton: ImageButton
    private val serviceDevice = ServiceDevice()
    private val serviceThemes = ServiceThemes()
    private val selectedDeviceIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_theme_page)

        //view binding
        lstAddDevices = findViewById(R.id.lstAddDevicesThemes)
        btnCreateTheme = findViewById(R.id.btnCreateTheme)
        btnBackButton = findViewById(R.id.btnBackATP)

        //displays devices to user to choose from
        fetchAndDisplayDevices()

        //button to create theme
        btnCreateTheme.setOnClickListener {
            createTheme()
        }

        btnBackButton.setOnClickListener {
            finish()
        }
    }

    //fetches devices from database
    private fun fetchAndDisplayDevices() {
        lifecycleScope.launch {
            try {
                // fetch devices
                var devices: List<ParseDevice>?
                if (isConnected(this@AddThemePage)){
                    devices = serviceDevice.GetAllDevices()
                }
                else{
                    devices = serviceDevice.OfflineDevices()
                }
                //add to layout
                devices?.let {
                    for (device in it) {
                        val deviceLayout = createDeviceLayout(device)
                        lstAddDevices.addView(deviceLayout)
                    }
                }
            } catch (e: Exception) {
                Log.e("AddThemePage", "Error fetching devices: ${e.message}")
            }
        }
    }

    //create layout (devices with switches)
    private fun createDeviceLayout(device: ParseDevice): View {
        val deviceThemeLayout = LinearLayout(this).apply {
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

        deviceThemeLayout.addView(deviceName)
        deviceThemeLayout.addView(deviceSwitch)

        return deviceThemeLayout
    }

    //create theme with toggled devices (list of IDs)
    private fun createTheme() {
        // theme name
        val themeName = findViewById<EditText>(R.id.themeName).text.toString()

        // Check if themeName is empty
        if (themeName.isEmpty()) {
            // Show message
            Toast.makeText(this, "Please enter a theme name.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if selectedDeviceIds is empty
        if (selectedDeviceIds.isEmpty()) {
            // Show message
            Toast.makeText(this, "Please select at least one device.", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)

        lifecycleScope.launch {
            try {
                //call service method to create theme. Takes userID, theme name and the list of Device IDs obtained from toggling switches
                serviceThemes.CreateTheme(this@AddThemePage, themeName, userID, selectedDeviceIds.toTypedArray())
                Log.d("AddThemePage", "Theme created with devices: $selectedDeviceIds")
                finish()
            } catch (e: Exception) {
                Log.e("AddThemePage", "Error creating theme: ${e.message}")
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
