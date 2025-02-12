package com.adr.opsc7312_poe

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class LanguagePage : AppCompatActivity() {
    private lateinit var btnBackButton: ImageButton
    private lateinit var btnEnglish: Button
    private lateinit var btnAfrikaans: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_language_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBackButton = findViewById(R.id.imgBackRT)
        btnEnglish = findViewById(R.id.btnEnglish)
        btnAfrikaans = findViewById(R.id.btnAfrikaans)

        btnBackButton.setOnClickListener {
            finish()
        }

        btnEnglish.setOnClickListener {
            setLocale("en")
        }

        btnAfrikaans.setOnClickListener {
            setLocale("afr")
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", languageCode)
        editor.apply()

        // Restart the main activity to apply changes to the navigation bar
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
