package com.adr.opsc7312_poe

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils.substring
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adr.opsc7312_poe.SensorReadingsAdapter.ReadingViewHolder

class EventAdapter(
    private val events: List<Event>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_reading, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Create the text with both message and timestamp
        val eventText = event.message + "\n"
        val formattedTime = event.timestamp
        val spannable = SpannableString("$eventText$formattedTime")

        // Calculate the start and end indices for formattedTime
        val timeStartIndex = eventText.length
        val timeEndIndex = timeStartIndex + formattedTime.length

        // Apply smaller and italic style to timestamp
        spannable.setSpan(
            RelativeSizeSpan(0.6f),
            timeStartIndex,
            timeEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.ITALIC),
            timeStartIndex,
            timeEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the styled text to the TextView
        holder.eventText.text = spannable
    }


    override fun getItemCount(): Int = events.size

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventText: TextView = view.findViewById(R.id.txtReadingText)
    }
}