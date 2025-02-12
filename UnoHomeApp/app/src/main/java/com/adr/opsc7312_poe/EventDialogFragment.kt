package com.adr.opsc7312_poe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class EventDialogFragment : DialogFragment() {
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var dao: DaoEvents

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.event_dialog, container, false)
        dao = DaoEvents(requireContext())

        eventRecyclerView = view.findViewById(R.id.eventRecyclerView)
        eventRecyclerView.layoutManager = LinearLayoutManager(context)


        loadHistory()

        return view
    }

    //load history
    private fun loadHistory() {
        val history = dao.getAllEvents()
        eventRecyclerView.adapter = EventAdapter(history)
    }

    companion object {
        fun newInstance(): EventDialogFragment {
            val fragment = EventDialogFragment()
            val args = Bundle()
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