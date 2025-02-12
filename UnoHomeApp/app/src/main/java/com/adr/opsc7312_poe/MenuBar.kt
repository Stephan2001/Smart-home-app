package com.adr.opsc7312_poe

import android.content.res.Resources.Theme
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MenuBar : AppCompatActivity() {

    private val HomeFrag = DashboardFragment()
    private val DeviceFrag = DevicesPage()
    private val AutomationFrag = AutomationPage()
    private val ThemesFrag = ThemesPage()
    private val SettingsFrag = SettingsPage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loadLocale()
        setContentView(R.layout.activity_menu_bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replaceFrag(HomeFrag)
        val bottomBar = findViewById<BottomNavigationView>(R.id.NavBar)
        bottomBar.setOnItemSelectedListener{
            when(it.itemId)
            {
                R.id.ic_home->replaceFrag(HomeFrag)
                R.id.ic_devices->replaceFrag(DeviceFrag)
                R.id.ic_automation->replaceFrag(AutomationFrag)
                R.id.ic_themes->replaceFrag(ThemesFrag)
                R.id.ic_settings->replaceFrag(SettingsFrag)
            }
            true
        }

        bottomBar.menu.clear()
        bottomBar.inflateMenu(R.menu.menu)

    }

    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("My_Lang", "") ?: ""
        setLocale(language)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics) }

    private fun replaceFrag(fragment: Fragment)
    {
        if(fragment!=null)
        {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, fragment)
            transaction.commit()
        }
    }
}