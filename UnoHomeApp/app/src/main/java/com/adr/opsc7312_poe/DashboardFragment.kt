package com.adr.opsc7312_poe

import android.content.Context.MODE_PRIVATE
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var displayTime: TextView
    private lateinit var displayDate: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val serviceReadings = ServiceReadings()

    private val serviceDevice = ServiceDevice()

    private lateinit var gasSensorReading: TextView
    private lateinit var steamSensorReading: TextView
    private lateinit var soilSensorReading: TextView
    private lateinit var gasSensorName: TextView
    private lateinit var steamSensorName: TextView
    private lateinit var soilSensorName: TextView
    private lateinit var history: TextView
    private lateinit var refreshGasReading: ImageView
    private lateinit var refreshSteamReading: ImageView
    private lateinit var refreshSoilReading: ImageView
    private lateinit var panicButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize TextViews after view is inflated
        displayTime = view.findViewById(R.id.Time) ?: throw IllegalStateException("View ID not found")
        displayDate = view.findViewById(R.id.date) ?: throw IllegalStateException("View ID not found")

        gasSensorReading = view.findViewById(R.id.txtGasSensorReading)
        steamSensorReading = view.findViewById(R.id.txtSteamSensorReading)
        soilSensorReading = view.findViewById(R.id.txtSoilSensorReading)

        refreshGasReading = view.findViewById(R.id.imgRefreshGasReading)
        refreshSteamReading = view.findViewById(R.id.imgRefreshSteamReading)
        refreshSoilReading = view.findViewById(R.id.imgRefreshSoilReading)

        gasSensorName = view.findViewById(R.id.txtGasSensor)
        steamSensorName = view.findViewById(R.id.txtSteamSensor)
        soilSensorName = view.findViewById(R.id.txtSoilSensor)
        history = view.findViewById(R.id.txtHistory)

        panicButton = view.findViewById(R.id.btnPanicButton)

        //on clicks for refresh buttons
        refreshGasReading.setOnClickListener {
            loadGasSensorReadings()
        }
        refreshSteamReading.setOnClickListener {
            loadSteamSensorReadings()
        }
        refreshSoilReading.setOnClickListener {
            loadSoilSensorReadings()
        }

        //on clicks to view history
        gasSensorName.setOnClickListener {
            showSensorReadingsDialog("Gas Sensor")
        }
        steamSensorName.setOnClickListener {
            showSensorReadingsDialog("Steam Sensor")
        }
        soilSensorName.setOnClickListener {
            showSensorReadingsDialog("Soil Sensor")
        }
        history.setOnClickListener{
            showHistory()
        }

        //on click for panic button
        setupPanicButton()

        updateDateTime()
        loadGasSensorReadings()
        loadSteamSensorReadings()
        loadSoilSensorReadings()

        return view
    }

    private fun updateDateTime() {
        runnable = Runnable {
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val currentDate = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date())

            displayTime.text = currentTime
            displayDate.text = currentDate

            // Update every second
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }


    private fun setupPanicButton() {
        panicButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //pulsing animation while holding button
                    startPulsingAnimation()
                    //activate panic after holding for 3 seconds
                    panicButtonHandler.postDelayed(panicRunnable, 3000)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    //stop animation if not held for 3 seconds
                    stopPulsingAnimation()
                    panicButtonHandler.removeCallbacks(panicRunnable)
                    true
                }
                else -> false
            }
        }
    }

    //button animation (pulsing)
    private fun startPulsingAnimation() {
        val pulseAnimation = ScaleAnimation(
            1f, 1.1f,
            1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
        panicButton.startAnimation(pulseAnimation)
    }

    private fun stopPulsingAnimation() {
        panicButton.clearAnimation()
    }

    private val panicButtonHandler = Handler(Looper.getMainLooper())
    private val panicRunnable = Runnable {
        stopPulsingAnimation()
        activatePanicAlarm()
    }


    private fun loadGasSensorReadings() {
        lifecycleScope.launch {
            try {
                //API call to get reading
                val reading = serviceReadings.getLatestSensorReading("Gas Sensor")

                //if not null
                if (reading != null) {
                    val readingText = "Reading: ${reading.sensorReading1}\n"
                    val formattedTime = reading.sensorTime.substring(0, 5)
                    val dateTimeText = "${reading.sensorDate} $formattedTime"

                    val spannable = SpannableString("$readingText$dateTimeText")

                    //change colour based on value
                    val readingColor = if (reading.sensorReading1 > 95) {
                        requireContext().getColor(android.R.color.holo_red_light)
                    } else {
                        requireContext().getColor(android.R.color.holo_green_light)
                    }

                    //apply colour to reading value
                    val startIndex = "Reading: ".length
                    val endIndex = startIndex + reading.sensorReading1.toString().length
                    spannable.setSpan(
                        ForegroundColorSpan(readingColor),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    //date and time smaller
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

                    gasSensorReading.text = spannable
                }
            } catch (e: Exception) {
                gasSensorReading.text = "Error Fetching Reading"
            }
        }
    }


    private fun loadSteamSensorReadings() {
        lifecycleScope.launch {
            try {
                //API call to get reading
                val reading = serviceReadings.getLatestSensorReading("Steam Sensor")

                //if not null
                if (reading != null) {
                    val readingText = "Reading: ${reading.sensorReading1}\n"
                    val formattedTime = reading.sensorTime.substring(0, 5)
                    val dateTimeText = "${reading.sensorDate} $formattedTime"

                    val spannable = SpannableString("$readingText$dateTimeText")

                    //change colour based on reading
                    val readingColor = if (reading.sensorReading1 > 900) {
                        requireContext().getColor(android.R.color.holo_red_light)
                    } else {
                        requireContext().getColor(android.R.color.holo_green_light)
                    }

                    //apply colour to reading
                    val startIndex = "Reading: ".length
                    val endIndex = startIndex + reading.sensorReading1.toString().length
                    spannable.setSpan(
                        ForegroundColorSpan(readingColor),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    //date and time smaller
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

                    steamSensorReading.text = spannable
                }
            } catch (e: Exception) {
                steamSensorReading.text = "Error Fetching Reading"
            }
        }
    }

    private fun activatePanicAlarm(){
        lifecycleScope.launch {
            try {
                context?.let { ServiceDevice().panicDevice(it) }
                Toast.makeText(context, "Panic alarm activated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error Activating Panic", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadSoilSensorReadings() {
        lifecycleScope.launch {
            try {
                //API call to get reading
                val reading = serviceReadings.getLatestSensorReading("Soil Sensor")

                // if not null
                if (reading != null) {
                    val readingText = "Reading: ${reading.sensorReading1}\n"
                    val formattedTime = reading.sensorTime.substring(0, 5)
                    val dateTimeText = "${reading.sensorDate} $formattedTime"

                    val spannable = SpannableString("$readingText$dateTimeText")

                    //change text colour based on reading
                    val readingColor = if (reading.sensorReading1 > 250) {
                        requireContext().getColor(android.R.color.holo_red_light)
                    } else {
                        requireContext().getColor(android.R.color.holo_green_light)
                    }

                    //apply colour to reading
                    val startIndex = "Reading: ".length
                    val endIndex = startIndex + reading.sensorReading1.toString().length
                    spannable.setSpan(
                        ForegroundColorSpan(readingColor),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    //date and time smaller
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

                    soilSensorReading.text = spannable
                }
            } catch (e: Exception) {
                soilSensorReading.text = "Error Fetching Reading"
            }
        }
    }

    private fun showSensorReadingsDialog(sensorName: String) {
        val dialogFragment = SensorReadingsDialogFragment.newInstance(sensorName)
        dialogFragment.show(childFragmentManager, "SensorReadingsDialog")
    }

    private fun showHistory() {
        val historyFragment = EventDialogFragment.newInstance()
        historyFragment.show(childFragmentManager, "HistoryDialog")
    }

}
