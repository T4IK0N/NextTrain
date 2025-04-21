package com.example.nexttrain

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        window.navigationBarColor = resources.getColor(R.color.background, theme)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setChosenTimestampString(getCurrentTimestamp()[0])

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MainFragment())
            .commit()

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MainFragment())
                        .commit()
                    true
                }
                R.id.tickets -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TicketsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTimestamp(): List<String> {
        val tempCurrentDate = Date()

        val dateFormatter = SimpleDateFormat("dd.MM.yy")
        val timeFormatter = SimpleDateFormat("HH:mm")
        val dayFormatter = SimpleDateFormat("EEEE", java.util.Locale.ENGLISH)

        val date = dateFormatter.format(tempCurrentDate)
        val time = timeFormatter.format(tempCurrentDate)
        val dayOfWeek = dayFormatter.format(tempCurrentDate)

        val timestamp = "$date $dayOfWeek, $time"
        return listOf(timestamp, date, time) // 0-timestamp, 1-date, 2-time
    }


    private fun setChosenTimestampString(timestamp: String) {
        val sharedPreferences = getSharedPreferences("TimestampPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("timestamp", timestamp)
            .apply()
    }
}
