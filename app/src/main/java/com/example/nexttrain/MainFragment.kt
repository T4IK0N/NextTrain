package com.example.nexttrain

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("TimestampPrefs", Context.MODE_PRIVATE)
        val savedTimestamp = sharedPreferences.getString("timestamp", "brak")
        view.findViewById<TextView>(R.id.timestampTextView).text = savedTimestamp

        val calendarButton = view.findViewById<LinearLayout>(R.id.calendarButton)
        calendarButton.setOnClickListener {
            val dialog = CalendarDialogFragment()
            dialog.show(parentFragmentManager, "CalendarDialog")
        }

        parentFragmentManager.setFragmentResultListener("timestampKey", viewLifecycleOwner) { _, bundle ->
            val timestamp = bundle.getString("timestamp")
            view.findViewById<TextView>(R.id.timestampTextView).text = timestamp
        }
    }
}
