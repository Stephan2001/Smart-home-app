package com.adr.opsc7312_poe

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.m

class ThemesPage : Fragment() {
    private lateinit var lstThemes: LinearLayout
    private val serviceThemes = ServiceThemes()
    private val CHANNEL_ID = "theme_notifications"
    private val NOTIFICATION_PERMISSION_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_theme_page, container, false)

        lstThemes = layout.findViewById(R.id.lstThemes)

        fetchAndDisplayThemes()

        val btnAddThemeRedirect: FloatingActionButton = layout.findViewById(R.id.btnAddTheme)
        btnAddThemeRedirect.setOnClickListener {
            val intent = Intent(activity, AddThemePage::class.java)
            startActivity(intent)
        }
        createNotificationChannel()
        return layout
    }

    override fun onResume() {
        super.onResume()
        // clear list to prevent duplicates
        lstThemes.removeAllViews()

        // refresh list of themes
        fetchAndDisplayThemes()
    }

    private fun fetchAndDisplayThemes() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                val themes = serviceThemes.GetAllThemesByUserId(requireContext(), userID)
                lstThemes.removeAllViews()

                themes?.let {
                    for (theme in it) {
                        val themeLayout = createThemeLayout(theme)
                        lstThemes.addView(themeLayout)
                    }
                }
            } catch (e: Exception) {
                Log.e("ThemesPage", "Error fetching themes: ${e.message}")
            }
        }
    }

    private fun createThemeLayout(theme: ParseTheme): View {
        val themeLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                //add space between themes
                setMargins(0, 8, 0, 8)
            }

            // Set the custom background to look like a button
            setBackgroundResource(R.drawable.theme_item_background)
        }

        // play/pause button
        val playButton = ImageButton(requireContext()).apply {

            //if theme is active, show pause icon
            if (theme.isActive) {
                setImageResource(R.drawable.imgpauseicon)
            } else { //show play icon
                setImageResource(R.drawable.imgplayicon)
            }
            layoutParams = LinearLayout.LayoutParams(120, 120)
            setPadding(8, 8, 8, 8)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            background = null

            //button to turn theme on/off (calls method)
            setOnClickListener {
                playTheme(theme.themeId, this)
            }
        }

        // theme name
        val themeName = TextView(requireContext()).apply {
            text = theme.name
            textSize = 18f
            setTextColor(Color.parseColor("#FFFFFF"))
            setPadding(0, 25, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }


        // button to delete a theme
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.imgdelete)
            layoutParams = LinearLayout.LayoutParams(120, 120)
            setPadding(8, 8, 8, 8)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            background = null

            //click button to delete the theme (pops up dialog box)
            setOnClickListener {
                deleteDialogBox(theme.themeId)
            }
        }

        themeLayout.addView(playButton)
        themeLayout.addView(themeName)
        themeLayout.addView(deleteButton)

        return themeLayout
    }


    //method to turn theme on or off
    private fun playTheme(themeId: Int, button: ImageButton) {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                serviceThemes.ToggleThemeStatus(requireContext(), themeId)
                Log.d("ThemesPage", "Toggled theme $themeId on/off")

                //get theme status to update icon (using default userId of 1)
                val updatedTheme = serviceThemes.GetAllThemesByUserId(requireContext(),userID)?.find { it.themeId == themeId }


                updatedTheme?.let {
                    if(it.isActive){
                        button.setImageResource(R.drawable.imgpauseicon)
                        showNotification("Theme Turned On", "${updatedTheme.name} has been turned on.")
                    }else {
                        button.setImageResource(R.drawable.imgplayicon)
                        showNotification("Theme Turned Off", "${updatedTheme.name} has been turned off.")
                    }
                }

            } catch (e: Exception) {
                Log.e("ThemesPage", "Error changing theme status: ${e.message}")
            }
        }
    }

    private fun deleteTheme(themeId: Int) {
        lifecycleScope.launch {
            try {
                // call delete method from service class
                serviceThemes.RemoveTheme(requireContext(), themeId)
                Log.d("ThemesPage", "Deleted theme $themeId successfully")

                // refresh list of themes after deletion
                lstThemes.removeAllViews()
                fetchAndDisplayThemes()

            } catch (e: Exception) {
                Log.e("ThemesPage", "Error deleting theme: ${e.message}")
            }
        }
    }

    private fun deleteDialogBox(themeId: Int) {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Delete Theme?")
        builder.setMessage("This can't be undone")

        // if user clicks Delete, calls deleteTheme method and deletes theme
        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteTheme(themeId)
            dialog.dismiss()
        }

        // closes dialog if discard is clicked
        builder.setNegativeButton("Discard") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()

        //set colours
        val DeleteButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        DeleteButton.setTextColor(Color.RED)
        val CancelButton: Button = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        CancelButton.setTextColor(Color.BLUE)
    }

    //notifications
    //https://developer.android.com/develop/ui/views/notifications/build-notification
    private fun showNotification(title: String, message: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33+
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE)
                return
            }
        }

        val notificationIntent = Intent(requireContext(), ThemesPage::class.java).apply {
        }
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.uno_home_logo)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.notify(1, notification) // Use a unique notification ID
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Theme Notification"
            val descriptionText = "Notification for theme status change"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, you can send the notification now
            } else {
                Log.e("ThemesPage", "Notification permission denied")
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