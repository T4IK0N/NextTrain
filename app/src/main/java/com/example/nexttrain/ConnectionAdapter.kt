package com.example.nexttrain

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexttrain.databinding.FragmentConnectionItemBinding

class ConnectionAdapter(
    private val values: List<Connection>
) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentConnectionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.stationName.text = item.station
        holder.departureArrival.text = "${item.departureTime} → ${item.arrivalTime}"

        when (item.transfers) {
            0 -> {
                holder.transfersTravel.text = "direct, ${item.travelTime}h"
            }
            1 -> {
                holder.transfersTravel.text = "${item.transfers} transfer, ${item.travelTime}h"
            }
            else -> {
                holder.transfersTravel.text = "${item.transfers} transfers, ${item.travelTime}h"
            }
        }

        // Set transport types (only as text for now)
        holder.transportTypes.text = item.transport?.joinToString()

        // Add transport images to the container using the method getTransportImageResource
        val transportImages = item.getTransportImageResource(holder.itemView.context)
        addTransportImages(holder.transportImagesContainer, transportImages)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentConnectionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val stationName: TextView = binding.stationName
        val departureArrival: TextView = binding.departureArrival
        val transfersTravel: TextView = binding.transfersTravel
        val transportTypes: TextView = binding.transportTypes
        val transportImagesContainer: LinearLayout = binding.transportImagesContainer
    }

    // Function to add transport images dynamically
    private fun addTransportImages(container: LinearLayout, transportImages: List<Int>) {
        container.removeAllViews()  // Clear previous images

        // Add images to the container
        transportImages.forEach { imageResId ->
            val imageView = ImageView(container.context)
            imageView.setImageResource(imageResId)
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            imageView.setPadding(8, 0, 8, 0)  // Optional padding between images
            container.addView(imageView)
        }
    }
}
