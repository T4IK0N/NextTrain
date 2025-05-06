package com.example.nexttrain

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarDialogFragment : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendar_dialog, container, false)
        val background = view.findViewById<FrameLayout>(R.id.blur_background)
        val dialogContent = view.findViewById<LinearLayout>(R.id.dialog_content)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val timestampTextView = view.findViewById<TextView>(R.id.timestampTextView)
        val datePicker = view.findViewById<DatePicker>(R.id.datePicker)
        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)

        // prefs
        val timestampPrefs = requireContext().getSharedPreferences("TimestampPrefs", Context.MODE_PRIVATE)
        val savedTimestamp = timestampPrefs.getString("timestamp", "brak")

        timestampTextView.text = savedTimestamp
        timePicker.setIs24HourView(true)

        // download from prefs
        if (savedTimestamp != null && savedTimestamp != "brak") {
            try {
                val sdf = java.text.SimpleDateFormat("dd.MM.yy EEEE, HH:mm", Locale.ENGLISH)
                val parsedDate = sdf.parse(savedTimestamp)

                val calendar = java.util.Calendar.getInstance()
                calendar.time = parsedDate!!

                datePicker.updateDate(
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )

                timePicker.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                timePicker.minute = calendar.get(java.util.Calendar.MINUTE)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // on confirm button (put to prefs)
        confirmButton.setOnClickListener {
            val calendar = java.util.Calendar.getInstance().apply {
                set(datePicker.year, datePicker.month, datePicker.dayOfMonth, timePicker.hour, timePicker.minute)
            }

            val dayOfWeekFormat = java.text.SimpleDateFormat("EEEE", Locale.ENGLISH)
            val formattedDay = dayOfWeekFormat.format(calendar.time)

            val formattedDate = String.format("%02d.%02d.%02d", datePicker.dayOfMonth, datePicker.month + 1, datePicker.year % 100)
            val formattedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            val fullTimestamp = "$formattedDate $formattedDay, $formattedTime"

            timestampPrefs.edit().putString("timestamp", fullTimestamp).apply()

            val result = Bundle()
            result.putString("timestamp", fullTimestamp)
            parentFragmentManager.setFragmentResult("timestampKey", result)

            timestampTextView.text = fullTimestamp // update view

            dismiss() // close dialog
        }

        // realtime changing date and time (not put to prefs cuz its not worth it)
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.ENGLISH)
        val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)

        datePicker.setOnDateChangedListener { _, year, month, day ->
            val date = LocalDate.of(year, month + 1, day)
            val formattedDate = date.format(dateFormatter)
            val formattedDay = date.format(dayFormatter)

            val formattedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            val fullTimestamp = "$formattedDate $formattedDay, $formattedTime"
            timestampTextView.text = fullTimestamp
        }

        timePicker.setOnTimeChangedListener { _, hour, minute ->
            val date = LocalDate.of(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
            val formattedDate = date.format(dateFormatter)
            val formattedDay = date.format(dayFormatter)

            val formattedTime = String.format("%02d:%02d", hour, minute)
            val fullTimestamp = "$formattedDate $formattedDay, $formattedTime"
            timestampTextView.text = fullTimestamp
        }


        background.setOnClickListener {
            dismiss() // close fragment on background
        }

        dialogContent.setOnClickListener {
            // DON'T CHANGE - disable closing from clicking on content
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
