// SensorReadingsDialogFragment.kt
package com.adr.opsc7312_poe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class SensorReadingsDialogFragment : DialogFragment() {

    private lateinit var readingsRecyclerView: RecyclerView
    private val serviceReadings = ServiceReadings()
    private lateinit var sensorName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sensor_readings_dialog, container, false)

        readingsRecyclerView = view.findViewById(R.id.readingsRecyclerView)
        readingsRecyclerView.layoutManager = LinearLayoutManager(context)

        //get sensor name
        sensorName = arguments?.getString("sensorName") ?: ""

        loadSensorReadings()

        return view
    }

    //load history
    private fun loadSensorReadings() {
        lifecycleScope.launch {
            val readings = serviceReadings.getSensorReadingsByDeviceName(sensorName)
            readingsRecyclerView.adapter = SensorReadingsAdapter(readings, sensorName)
        }
    }

    companion object {
        fun newInstance(sensorName: String): SensorReadingsDialogFragment {
            val fragment = SensorReadingsDialogFragment()
            val args = Bundle()
            args.putString("sensorName", sensorName)
            fragment.arguments = args
            return fragment
        }
    }

    //setting pop up size
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            300.dpToPx(requireContext()),
            400.dpToPx(requireContext())
        )
    }

    private fun Int.dpToPx(context: android.content.Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
