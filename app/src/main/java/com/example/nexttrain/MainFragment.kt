package com.example.nexttrain

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var favoriteRoute: Route? = null
    private var currentCity: String? = null

    private lateinit var startLocationEditText: EditText
    private lateinit var endLocationEditText: EditText
    private lateinit var timestampTextView: TextView
    private lateinit var favoriteRouteImageView: ImageView
    private lateinit var replaceDirectionsImageView: ImageView
    private lateinit var locationImageView: ImageView
    private lateinit var directConnectionCheckbox: CheckBox
    private lateinit var favoriteRouteButton: MaterialButton
    private lateinit var searchButton: MaterialButton

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        startLocationEditText = view.findViewById(R.id.startLocationEditText)
        endLocationEditText = view.findViewById(R.id.endLocationEditText)
        timestampTextView = view.findViewById(R.id.timestampTextView)
        favoriteRouteImageView = view.findViewById(R.id.favoriteRouteImageView)
        replaceDirectionsImageView = view.findViewById(R.id.replaceDirectionsImageView)
        locationImageView = view.findViewById(R.id.locationImageView)
        directConnectionCheckbox = view.findViewById(R.id.directConnectionCheckBox)
        favoriteRouteButton = view.findViewById(R.id.favoriteRouteButton)
        searchButton = view.findViewById(R.id.searchButton)

        // prefs
        val timestampPrefs =
            requireContext().getSharedPreferences("TimestampPrefs", Context.MODE_PRIVATE)
        val savedTimestamp = timestampPrefs.getString("timestamp", "brak")

        val favoritePrefs =
            requireContext().getSharedPreferences("FavoritePrefs", Context.MODE_PRIVATE)
        val savedStart = favoritePrefs.getString("start", null)
        val savedEnd = favoritePrefs.getString("end", null)

        val routePrefs =
            requireContext().getSharedPreferences("RoutePrefs", Context.MODE_PRIVATE)

        setCurrentRoute( // default on start fragment
            Route(
                routePrefs.getString("start", "") ?: "",
                routePrefs.getString("end", "") ?: "",
                routePrefs.getString("timestamp", "") ?: "",
                routePrefs.getString("date", "") ?: "",
                routePrefs.getString("time", "") ?: "",
                routePrefs.getBoolean("direct", false)
            )
        )

        favoriteRoute = if (savedStart != null && savedEnd != null) {
            val date = savedTimestamp?.let { formatDate(it) }
            val time = savedTimestamp?.let { formatTime(it) }
            Route(savedStart, savedEnd, savedTimestamp ?: "", date ?: "", time ?: "", false)
        } else {
            null
        }

        val appNameTextView = (activity as? MainActivity)?.findViewById<TextView>(R.id.appNameTextView)
        if (appNameTextView != null) {
            appNameTextView.textSize = 35F
            appNameTextView.text = "NextTrain"
        }

        locationImageView.setOnClickListener {
            getLocation()
        }

        timestampTextView.text = savedTimestamp
        updateFavoriteIcon()

        // timestamp click listener
        timestampTextView.setOnClickListener {
            val dialog = CalendarDialogFragment()
            dialog.show(parentFragmentManager, "CalendarDialog")
        }

        parentFragmentManager.setFragmentResultListener(
            "timestampKey",
            viewLifecycleOwner
        ) { _, bundle ->
            val timestamp = bundle.getString("timestamp")
            timestampTextView.text = timestamp
        }

        // favorite route button listener
        favoriteRouteImageView.setOnClickListener {
            val currentStart = startLocationEditText.text.toString()
            val currentEnd = endLocationEditText.text.toString()

            if (favoriteRoute != null && favoriteRoute?.start == currentStart && favoriteRoute?.end == currentEnd) {
                // delete favorite route
                favoriteRoute = null
                favoritePrefs.edit().clear().apply()
            } else {
                // set favorite route
                val date = formatDate(timestampTextView.text.toString())
                val time = formatTime(timestampTextView.text.toString())
                favoriteRoute = Route(
                    currentStart,
                    currentEnd,
                    timestampTextView.text.toString(),
                    date,
                    time,
                    directConnectionCheckbox.isChecked
                )
                favoritePrefs.edit()
                    .putString("start", currentStart)
                    .putString("end", currentEnd)
                    .putString("timestamp", timestampTextView.text.toString())
                    .apply()
            }

            updateFavoriteIcon()
        }

        // favorite route listener
        favoriteRouteButton.setOnClickListener {
            favoriteRoute?.let {
                startLocationEditText.setText(it.start)
                endLocationEditText.setText(it.end)
                updateFavoriteIcon()
            }
        }

        // watcher for instant favorite route checking
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateFavoriteIcon()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        startLocationEditText.addTextChangedListener(textWatcher)
        endLocationEditText.addTextChangedListener(textWatcher)

        replaceDirectionsImageView.setOnClickListener {
            val temp = startLocationEditText.text
            startLocationEditText.text = endLocationEditText.text
            endLocationEditText.text = temp
        }

        searchButton.setOnClickListener {
            try {
                if (startLocationEditText.text.isNullOrEmpty() || endLocationEditText.text.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "start or end station doesnt have to be empty or null", Toast.LENGTH_SHORT).show()
                    throw IllegalArgumentException("start or end station doesnt have to be empty or null")
                }

                if (startLocationEditText.text == endLocationEditText.text) {
                    Toast.makeText(requireContext(), "start or end station doesnt have to be same", Toast.LENGTH_SHORT).show()
                    throw IllegalArgumentException("start and end station doesnt have to be the same")
                }

                val currentRoute = getCurrentRoute()

                routePrefs.edit()
                    .putString("start", currentRoute.start)
                    .putString("end", currentRoute.end)
                    .putString("timestamp", currentRoute.timestamp)
                    .putString("date", currentRoute.date)
                    .putString("time", currentRoute.time)
                    .putBoolean("direct", currentRoute.direct)
                    .apply()

                val connectionFragment = ConnectionFragment()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, connectionFragment)
                    .addToBackStack(null)
                    .commit()

                val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                bottomNavigationView.selectedItemId = R.id.search

            } catch (e: Exception) {
                Log.e("PYTHON_ERROR", "error calling python script", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun formatDate(timestamp: String): String {
        val sdf = SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)
        return try {
            val date = SimpleDateFormat("dd.MM.yy EEEE, HH:mm", Locale.ENGLISH).parse(timestamp)
            sdf.format(date!!)
        } catch (e: Exception) {
            "invalid date"
        }
    }

    private fun formatTime(timestamp: String): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return try {
            val date = SimpleDateFormat("dd.MM.yy EEEE, HH:mm", Locale.ENGLISH).parse(timestamp)
            sdf.format(date!!)
        } catch (e: Exception) {
            "invalid time"
        }
    }

    fun updateFavoriteIcon() {
        val currentStart = startLocationEditText.text.toString()
        val currentEnd = endLocationEditText.text.toString()

        if (favoriteRoute != null && favoriteRoute?.start == currentStart && favoriteRoute?.end == currentEnd) {
            favoriteRouteImageView.setImageResource(R.drawable.favorite_icon)
        } else {
            favoriteRouteImageView.setImageResource(R.drawable.not_favorite_icon)
        }
    }

    private fun getCurrentRoute(): Route {
        return Route(
            start = startLocationEditText.text.toString(),
            end = endLocationEditText.text.toString(),
            timestamp = timestampTextView.text.toString(),
            date = formatDate(timestampTextView.text.toString()),
            time = formatTime(timestampTextView.text.toString()),
            direct = directConnectionCheckbox.isChecked
        )
    }

        fun setCurrentRoute(route: Route) {
            startLocationEditText.setText(route.start)
            endLocationEditText.setText(route.end)
            timestampTextView.text = route.timestamp
            directConnectionCheckbox.isChecked = route.direct

            updateFavoriteIcon()
        }

    private fun getLocation() {
        // check if u have permissions
        // !!! in manifest.permission u have to add android. before
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request for permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
            val location = task.result
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                getCityName(latitude, longitude)
            } else {
                Log.e("Location", "nie ma lokalizacji")
            }
        }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                currentCity = address.locality
                startLocationEditText.setText(currentCity)
//                Log.i("CurrentCity", "aktualna miejscowosc: $currentCity")
            } else {
                Log.e("Location", "nie ma nazwy miejscowosci")
            }
        } catch (e: Exception) {
            Log.e("Location", "error podczas geokodowania: ${e.message}")
        }
    }
}
