package com.adr.opsc7312_poe

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class AutomationPage : Fragment() {
    private lateinit var lstAutomations: LinearLayout
    private val serviceAutomations = ServiceRoutine()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_automation_page, container, false)

        lstAutomations = layout.findViewById(R.id.lstAutomations)

        fetchAndDisplayAutomations()

        val btnAddAutomationRedirect: FloatingActionButton = layout.findViewById(R.id.btnAddAutomation)
        btnAddAutomationRedirect.setOnClickListener {
            val intent = Intent(activity, AddRoutinePage::class.java)
            startActivity(intent)
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        lstAutomations.removeAllViews()

        fetchAndDisplayAutomations()
    }

    private fun fetchAndDisplayAutomations() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                val automations = serviceAutomations.GetAllRoutinesByUserId(requireContext(), userID)

                lstAutomations.removeAllViews()

                automations?.let {
                    for (automation in it) {
                        val automationLayout = createAutomationLayout(automation)
                        lstAutomations.addView(automationLayout)
                    }
                }
            } catch (e: Exception) {
                Log.e("AutomationPage", "Error fetching automations: ${e.message}")
            }
        }
    }

    private fun createAutomationLayout(automation: ParseRoutine): View {
        val automationLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                //add space between themes
                setMargins(0, 8, 0, 8)
            }

            setBackgroundResource(R.drawable.theme_item_background)

            setOnClickListener {
                // Create intent to navigate to the new activity
                val intent = Intent(requireContext(), ManageRoutine::class.java).apply {
                    // Pass the Routine ID (or any other data you need)
                    putExtra("Routine_ID", automation.RoutineId)
                }
                // Start the activity
                startActivity(intent)
            }
        }

        val automationName = TextView(requireContext()).apply {
            text = automation.Name
            textSize = 18f
            setTextColor(Color.parseColor("#FFFFFF"))
            setPadding(0, 25, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.imgdelete)

            layoutParams = LinearLayout.LayoutParams(120, 120)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setPadding(8, 8, 8, 8)
            background = null

            setOnClickListener {
                automation.Name
                deleteAutomation(automation.RoutineId, automation.Name, automationLayout)
            }
        }

        automationLayout.addView(automationName)
        automationLayout.addView(deleteButton)

        return automationLayout
    }

    private fun deleteAutomation(automationId: Int, automationName: String, automationLayout: View) {
        lifecycleScope.launch {
            try {
                serviceAutomations.RemoveRoutine(requireContext(),automationId)
                lstAutomations.removeView(automationLayout)

                // Remove service worker
                val context = requireContext()
                val uniqueWorkerID = "${automationId}${automationName}Worker"
                Log.d("AutomationPage", "Worker $uniqueWorkerID set for removal")

                cancelUniqueRoutineWork(context, uniqueWorkerID)
                Log.d("AutomationPage", "Automation $automationId deleted")
            } catch (e: Exception) {
                Log.e("AutomationPage", "Error deleting automation: ${e.message}")
            }
        }
    }
}
