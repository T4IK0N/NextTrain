package com.example.nexttrain

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.button.MaterialButton
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class ConnectionFragment : Fragment(R.layout.fragment_connection) {

    private lateinit var appNameTextView: TextView
    private lateinit var connectionRecyclerView: RecyclerView
    private lateinit var adapter: ConnectionAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchMoreButton: MaterialButton

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentRoute = getCurrentRoute()

        appNameTextView = view.findViewById(R.id.appNameTextView)
        appNameTextView.text = "${currentRoute.start} → ${currentRoute.end}"

        connectionRecyclerView = view.findViewById(R.id.connectionRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        searchMoreButton = view.findViewById(R.id.searchMoreButton)

        connectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressBar.visibility = VISIBLE
        connectionRecyclerView.visibility = GONE
        searchMoreButton.visibility = GONE

        lifecycleScope.launch {
            val connectionList = loadDataInBackground()

            if (connectionList != null) {
                adapter = ConnectionAdapter(connectionList)
                connectionRecyclerView.adapter = adapter

                progressBar.visibility = GONE
                connectionRecyclerView.visibility = VISIBLE
                searchMoreButton.visibility = VISIBLE

                setupRecyclerViewScroll()
            } else {
                Toast.makeText(requireContext(),
                    "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentRoute(): Route {
        val routePrefs = requireContext().getSharedPreferences("RoutePrefs",
            Context.MODE_PRIVATE)
        return Route(
            routePrefs.getString("start", "") ?: "",
            routePrefs.getString("end", "") ?: "",
            routePrefs.getString("timestamp", "") ?: "",
            routePrefs.getString("date", "") ?: "",
            routePrefs.getString("time", "") ?: "",
            routePrefs.getBoolean("direct", false)
        )
    }

    private suspend fun loadDataInBackground(): List<Connection>? {
        return withContext(Dispatchers.IO) {
            try {
                val currentRoute = getCurrentRoute()

                // Python init
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(requireContext()))
                }

                val py = Python.getInstance()
                val main = py.getModule("main")
                val filesDirPath = requireContext().filesDir.absolutePath

                main.callAttr(
                    "main",
                    filesDirPath,
                    currentRoute.start,
                    currentRoute.end,
                    currentRoute.date,
                    currentRoute.time,
                    currentRoute.direct
                )

                main.callAttr(
                    "fetch_next_trains",
                    filesDirPath,
                    currentRoute.start,
                    currentRoute.end,
                    currentRoute.direct
                )

                readJsonFile(requireContext())
            } catch (e: Exception) {
                Log.e("LOAD_ERROR", "Error loading data", e)
                null
            }
        }
    }

    private fun setupRecyclerViewScroll() {
//        connectionRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            private var isLoading = false
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                if (dy > 0) {
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val visibleItemCount = layoutManager.childCount
//                    val totalItemCount = layoutManager.itemCount
//                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//
//                    if (!isLoading) {
//                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
//                            loadMoreData()
//                            isLoading = true
//                        }
//                    }
//                }
//            }
//
//            private fun loadMoreData() {
//                lifecycleScope.launch {
//                    val currentConnections = readJsonFile(requireContext())
//
//                    if (currentConnections != null && currentConnections.size > 10) {
//                        //wiecej niz 10 bo jeszcze raz pobierze
//                        Toast.makeText(requireContext(),
//                            "Nie można pobrać więcej pociągów",Toast.LENGTH_SHORT).show()
//                        isLoading = false
//                        return@launch
//                    }
//
//                    withContext(Dispatchers.IO) {
//                        val py = Python.getInstance()
//                        val main = py.getModule("main")
//                        val filesDirPath = requireContext().filesDir.absolutePath
//
//                        val routePrefs =requireContext().getSharedPreferences("RoutePrefs",
//                            Context.MODE_PRIVATE)
//                        val start = routePrefs.getString("start", "") ?: ""
//                        val end = routePrefs.getString("end", "") ?: ""
//                        val direct = routePrefs.getBoolean("direct", false)
//
//                        main.callAttr(
//                            "fetch_next_trains",
//                            filesDirPath,
//                            start,
//                            end,
//                            direct
//                        )
//                    }
//
//                    val newConnectionList = readJsonFile(requireContext())
//                    if (newConnectionList != null) {
//                        adapter.updateData(newConnectionList)
//                    }
//
//                    isLoading = false
//                }
//            }
//        })

        searchMoreButton.setOnClickListener {
            var isLoading = false

            val layoutManager = connectionRecyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            fun loadMoreData() {
                lifecycleScope.launch {
                    val currentConnections = readJsonFile(requireContext())

                    if (currentConnections != null && currentConnections.size > 10) {
                        //wiecej niz 10 bo jeszcze raz pobierze
                        Toast.makeText(requireContext(),
                            "Nie można pobrać więcej pociągów",Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@launch
                    }

                    withContext(Dispatchers.IO) {
                        val py = Python.getInstance()
                        val main = py.getModule("main")
                        val filesDirPath = requireContext().filesDir.absolutePath

                        val routePrefs =requireContext().getSharedPreferences("RoutePrefs",
                            Context.MODE_PRIVATE)
                        val start = routePrefs.getString("start", "") ?: ""
                        val end = routePrefs.getString("end", "") ?: ""
                        val direct = routePrefs.getBoolean("direct", false)

                        main.callAttr(
                            "fetch_next_trains",
                            filesDirPath,
                            start,
                            end,
                            direct
                        )
                    }

                    val newConnectionList = readJsonFile(requireContext())
                    if (newConnectionList != null) {
                        adapter.updateData(newConnectionList)
                    }

                    isLoading = false
                }
            }

            if (!isLoading) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                    loadMoreData()
                    isLoading = true
                }
            }
        }
    }

    private fun readJsonFile(context: Context,): List<Connection>? {
        return try {
            val inputStream = context.openFileInput("rozklad.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()

            val connectionListType = object : TypeToken<List<Connection>>() {}.type
            gson.fromJson(reader, connectionListType)
        } catch (e: Exception) {
            Log.e("JSON_ERROR", "Error reading JSON file", e)
            null
        }
    }
}
