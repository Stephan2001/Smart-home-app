package com.adr.opsc7312_poe

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SensorReadingsAdapter(
    private val readings: List<SensorReading>,
    private val sensorName: String // Add sensor name as a parameter
) : RecyclerView.Adapter<SensorReadingsAdapter.ReadingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_reading, parent, false)
        return ReadingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        val reading = readings[position]

        val readingText = "Reading: ${reading.sensorReading1}\n"
        val formattedTime = reading.sensorTime.substring(0, 5)
        val dateTimeText = "${reading.sensorDate} $formattedTime"

        val spannable = SpannableString("$readingText$dateTimeText")

        //change colour based on reading
        val readingColor = when (sensorName) {
            "Gas Sensor" -> if (reading.sensorReading1 > 95) {
                Color.RED
            } else {
                Color.GREEN
            }
            "Steam Sensor" -> if (reading.sensorReading1 > 900) {
                Color.RED
            } else {
                Color.GREEN
            }
            "Soil Sensor" -> if (reading.sensorReading1 > 250) {
                Color.RED
            } else {
                Color.GREEN
            }
            else -> Color.BLACK
        }

        //apply colour to the reading
        val startIndex = "Reading: ".length
        val endIndex = startIndex + reading.sensorReading1.toString().length
        spannable.setSpan(
            ForegroundColorSpan(readingColor),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        //date and time italic and smaller
        spannable.setSpan(
            RelativeSizeSpan(0.6f),
            readingText.length,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.ITALIC),
            readingText.length,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        holder.readingText.text = spannable
    }

    override fun getItemCount(): Int = readings.size

    class ReadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val readingText: TextView = view.findViewById(R.id.txtReadingText)
    }
}
