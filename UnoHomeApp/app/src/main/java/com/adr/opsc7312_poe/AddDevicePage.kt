package com.adr.opsc7312_poe

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddDevicePage : AppCompatActivity() {
    private lateinit var lstAddDevices: LinearLayout
    private lateinit var btnAddDevice: Button
    private val serviceDevice = ServiceDevice()
    private val selectedDeviceIds = mutableListOf<Int>()
    private lateinit var btnBackButton: ImageButton

    //used to store device IDs already added to profile
    private val existingDeviceIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device_page)

        lstAddDevices = findViewById(R.id.lstAddDevices)
        btnAddDevice = findViewById(R.id.btnAddDevices)
        btnBackButton = findViewById(R.id.btnBackADP)

        fetchAndDisplayDevices()

        btnAddDevice.setOnClickListener {
            addSelectedDevicesToUserProfile()
        }

        btnBackButton.setOnClickListener{
            finish()
        }
    }

    //fetch devices using API from ServiceDevice
    private fun fetchAndDisplayDevices() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                val userDevices: List<ParseDevice>?
                val devices: List<ParseDevice>?
                if (!isConnected(this@AddDevicePage)){
                    userDevices = listOf<ParseDevice>()
                    devices = serviceDevice.OfflineDevices()
                }
                else{
                    userDevices = serviceDevice.GetAllUserDevices(this@AddDevicePage, userID)
                    devices = serviceDevice.GetAllDevices()
                }
                userDevices?.let {
                    //adding existing device IDs linked to profile
                    existingDeviceIds.addAll(it.map { userDevice -> userDevice.DeviceId })
                }

                devices?.let {
                    for (device in it) {
                        //see if device has been added already
                        val isUserDevice = existingDeviceIds.contains(device.DeviceId)
                        Log.d("IsUserDevices", "ID: ${device.DeviceId} Name: ${device.Name}")
                        val deviceLayout = createDeviceLayout(device, isUserDevice)
                        lstAddDevices.addView(deviceLayout)
                    }
                }
            } catch (e: Exception) {
                Log.e("AddDevicePage", "Error fetching devices: ${e.message}")
            }
        }
    }

    private fun createDeviceLayout(device: ParseDevice, isUserDevice: Boolean): View {
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
            //toggle switch if device has already been added
            isChecked = isUserDevice
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Add device ID if it hasnt been added already
                    if (!existingDeviceIds.contains(device.DeviceId)) {
                        selectedDeviceIds.add(device.DeviceId)
                    }
                } else {
                    // if unchecked, removes device ID from list
                    selectedDeviceIds.remove(device.DeviceId)

                    // calls function to also remove device from profile if unchecked
                    removeDeviceFromUserProfile(device.DeviceId)
                }
            }
        }

        deviceLayout.addView(deviceName)
        deviceLayout.addView(deviceSwitch)

        return deviceLayout
    }

    //adding device to user profile
    private fun addSelectedDevicesToUserProfile() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                for (deviceId in selectedDeviceIds) {
                    // only add device if it hasn't been added already
                    if (!existingDeviceIds.contains(deviceId)) {
                        serviceDevice.AddDevice(this@AddDevicePage, userID, deviceId)

                        // add device ID to list of added device IDs
                        existingDeviceIds.add(deviceId)
                        Log.d("AddDevicePage", "Added device $deviceId to profile")
                    } else {
                        Log.d("AddDevicePage", "Device $deviceId is already in the profile, skipping.")
                    }
                }
                //navigates back to devices page
                finish()
                Log.d("AddDevicePage", "All selected devices processed")
            } catch (e: Exception) {
                Log.e("AddDevicePage", "Error adding devices: ${e.message}")
            }
        }
    }

    //function to remove device
    private fun removeDeviceFromUserProfile(deviceId: Int) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                //api call to remove device
                serviceDevice.RemoveDevice(this@AddDevicePage, userID, deviceId)
                Log.d("AddDevicePage", "Removed device $deviceId from profile")
            } catch (e: Exception) {
                Log.e("AddDevicePage", "Error removing device: ${e.message}")
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
