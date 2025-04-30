package com.example.nexttrain

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nexttrain.databinding.FragmentConnectionItemBinding

class ConnectionAdapter(
    private var connectionList: List<Connection>
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
        val item = connectionList[position]
        val dep = "${item.departureTime}"
        val arr = "${item.arrivalTime}"
        val delayDep = item.delayDeparture ?: ""
        val delayArr = item.delayArrival ?: ""

        val text = "$dep$delayDep → $arr$delayArr"
        val spannable = SpannableString(text)

        val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
        val redColor = ContextCompat.getColor(holder.itemView.context, R.color.red)

        spannable.setSpan(
            ForegroundColorSpan(textColor),
            0,
            dep.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        var currentIndex = dep.length

        if (delayDep.isNotEmpty()) {
            spannable.setSpan(
                ForegroundColorSpan(redColor),
                currentIndex,
                currentIndex + delayDep.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            currentIndex += delayDep.length
        }

        spannable.setSpan(
            ForegroundColorSpan(textColor),
            currentIndex,
            currentIndex + 3, // strzałka + spacje
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        currentIndex += 3

        spannable.setSpan(
            ForegroundColorSpan(textColor),
            currentIndex,
            currentIndex + arr.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        currentIndex += arr.length

        if (delayArr.isNotEmpty()) {
            spannable.setSpan(
                ForegroundColorSpan(redColor),
                currentIndex,
                currentIndex + delayArr.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        holder.departureArrival.text = spannable

        when (item.transfers) {
            0 -> holder.transfersTravel.text = "direct, ${item.travelTime}h"
            1 -> holder.transfersTravel.text = "${item.transfers} change, ${item.travelTime}h"
            else -> holder.transfersTravel.text = "${item.transfers} chages, ${item.travelTime}h"
        }

        val transportImages = item.getTransportImageResource()
        addTransportImages(holder.transportImagesContainer, transportImages)

        if (position == 0 || item.date != connectionList[position - 1].date) {
            holder.dividerView.visibility = GONE
            holder.newDateDivider.visibility = VISIBLE
            holder.newDateDividerTextView.text = item.date
        } else {
            holder.dividerView.visibility = VISIBLE
            holder.newDateDivider.visibility = GONE
        }
    }

    override fun getItemCount(): Int = connectionList.size

    inner class ViewHolder(binding: FragmentConnectionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        //        val stationName: TextView = binding.stationName
        val departureArrival: TextView = binding.departureArrival
        val transfersTravel: TextView = binding.transfersTravel
//        val transportTypes: TextView = binding.transportTypes
        val transportImagesContainer: LinearLayout = binding.transportImagesContainer
        val dividerView: View = binding.dividerView
        val newDateDivider: LinearLayout = binding.newDateDivider
        val newDateDividerTextView: TextView = binding.newDateDividerTextView
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addTransportImages(container: LinearLayout, transportImages: List<Int>) {
        container.removeAllViews()

        transportImages.forEach { imageResId ->
            val imageView = ImageView(container.context)
            imageView.setImageResource(imageResId)

            val drawable = container.context.resources.getDrawable(imageResId, null)
            val originalWidth = drawable.intrinsicWidth
            val originalHeight = drawable.intrinsicHeight

            var newHeight = 75
            if (imageResId == R.drawable.regio) {
                newHeight = 65
            }
            val newWidth = ((originalWidth * newHeight.toFloat()) / originalHeight.toFloat()).toInt()

            val layoutParams = LinearLayout.LayoutParams(newWidth, newHeight)
            imageView.layoutParams = layoutParams

            imageView.setPadding(0, 0, 8, 0)
            container.addView(imageView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Connection>) {
        connectionList = newData
        notifyDataSetChanged()
    }
}
