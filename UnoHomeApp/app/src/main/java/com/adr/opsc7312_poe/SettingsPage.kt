package com.adr.opsc7312_poe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class SettingsPage : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switcher: Switch
    private lateinit var btnPrivacy: Button
    private lateinit var btnNotifications: Button
    private lateinit var btnAppLanguage: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_page, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

        switcher = view.findViewById(R.id.switcher)
        switcher.isChecked = isDarkMode

        setAppTheme(isDarkMode)

        switcher.setOnCheckedChangeListener { _, isChecked ->
            setAppTheme(isChecked)
            saveThemePreference(isChecked)
        }

        btnPrivacy = view.findViewById(R.id.btnPrivacy)
        btnNotifications = view.findViewById(R.id.btnNotifications)
        btnAppLanguage = view.findViewById(R.id.btnAppLanguage)

        btnPrivacy.setOnClickListener {
            val intent = Intent(activity, PrivacyPage::class.java)
            startActivity(intent)
        }

        btnNotifications.setOnClickListener {
            val intent = Intent(activity, NotificationsPage::class.java)
            startActivity(intent)
        }

        btnAppLanguage.setOnClickListener {
            val intent = Intent(activity, LanguagePage::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkMode", isDarkMode)
        editor.apply()
    }

    private fun setAppTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}