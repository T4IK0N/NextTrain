package com.example.nexttrain

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
//    private lateinit var appNameTextView: TextView
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        window.navigationBarColor = resources.getColor(R.color.background, theme)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

//        appNameTextView = findViewById(R.id.appNameTextView)

        setChosenTimestampString(getCurrentTimestamp()[0])

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MainFragment())
            .commit()

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MainFragment())
                        .commit()
//                    appNameTextView.textSize = 35F
//                    appNameTextView.setText(R.string.app_name)
                    true
                }
                R.id.search -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ConnectionFragment())
                        .commit()
                    true
                }
                R.id.tickets -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TicketFragment())
                        .commit()
//                    appNameTextView.textSize = 35F
//                    appNameTextView.setText(R.string.app_name)
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
