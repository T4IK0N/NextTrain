package com.example.nexttrain

import com.google.gson.annotations.SerializedName

data class Connection(
    @SerializedName("id") val id: Int?,
    @SerializedName("station") val station: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("travel_time") val travelTime: String?,
    @SerializedName("departure_time") val departureTime: String?,
    @SerializedName("arrival_time") val arrivalTime: String?,
    @SerializedName("transfers") val transfers: Int?,
    @SerializedName("transport") val transport: List<String>?,
    @SerializedName("delay_departure") val delayDeparture: String?,
    @SerializedName("delay_arrival") val delayArrival: String?,
    var isNewDateDividerVisible: Boolean = false
) {
    fun getTransportImageResource(): List<Int> {
        val transportImages = mutableListOf<Int>()
        transport?.forEach { train ->
            val imageResId = when (train) {
                "REGIO" -> R.drawable.regio
                "IC" -> R.drawable.ic
                "EIC" -> R.drawable.eic
                "EIP" -> R.drawable.eip
                "TLK" -> R.drawable.tlk
                "KM" -> R.drawable.km
                else -> R.drawable.default_train
            }
            transportImages.add(imageResId)
        }
        return transportImages
    }
}
