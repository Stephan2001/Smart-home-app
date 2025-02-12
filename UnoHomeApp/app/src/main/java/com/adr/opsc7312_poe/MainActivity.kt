package com.adr.opsc7312_poe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.Manifest
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        saveUserIDToPreferences(1)
        //temp - use this if event history is finicky 
        //val dbHelper = DatabaseHelper(this)
        //dbHelper.clearDatabase(dbHelper.writableDatabase)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = NetworkCallback(this)

        registerNetworkCallback()

        val intent = Intent(this, MenuBar::class.java)
        startActivity(intent)
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun registerNetworkCallback() {
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the network callback when the activity is destroyed
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    // just here for testing
    private fun saveUserIDToPreferences(userID: Int) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("userid", userID)  // Save as int
        editor.apply()  // Save asynchronously
    }
}