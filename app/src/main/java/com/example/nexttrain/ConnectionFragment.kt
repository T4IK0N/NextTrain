package com.example.nexttrain

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.InputStreamReader

class ConnectionFragment : Fragment(R.layout.fragment_connection) {

    private lateinit var connectionRecyclerView: RecyclerView
    private lateinit var adapter: ConnectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectionRecyclerView = view.findViewById(R.id.connectionRecyclerView)
        connectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val connectionList = readJsonFile(requireContext(), "rozklad.json")
        Log.d("CONNECTION LIST", connectionList.toString())

        if (connectionList != null) {
            adapter = ConnectionAdapter(connectionList)
            connectionRecyclerView.adapter = adapter
        }
    }

    private fun readJsonFile(context: Context, fileName: String): List<Connection>? {
        return try {
            val inputStream = context.openFileInput(fileName)
            val reader = InputStreamReader(inputStream)
            val gson = Gson()

            val connectionListType = object : TypeToken<List<Connection>>() {}.type

            gson.fromJson(reader, connectionListType)
        } catch (e: Exception) {
            Log.e("JSON_ERROR", "Error reading JSON file", e)
            Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            null
        }
    }

}