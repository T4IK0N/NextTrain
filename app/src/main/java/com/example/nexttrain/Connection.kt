package com.example.nexttrain

import android.content.Context
import com.google.gson.annotations.SerializedName

data class Connection(
    @SerializedName("id") val id: Int?,
    @SerializedName("station") val station: String?,
    @SerializedName("departure_time") val departureTime: String?,
    @SerializedName("arrival_time") val arrivalTime: String?,
    @SerializedName("travel_time") val travelTime: String?,
    @SerializedName("transfers") val transfers: Int?,
    @SerializedName("transport") val transport: List<String>?
) {
    fun getTransportImageResource(context: Context): List<Int> {
        val transportImages = mutableListOf<Int>()
        transport?.forEach { train ->
            val imageResId = when (train) {
                "REGIO" -> R.drawable.regio
                "IC" -> R.drawable.ic
                else -> R.drawable.default_train
            }
            transportImages.add(imageResId)
        }
        return transportImages
    }
}
