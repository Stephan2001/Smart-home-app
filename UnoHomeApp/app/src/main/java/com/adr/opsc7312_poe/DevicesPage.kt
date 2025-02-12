package com.adr.opsc7312_poe

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class DevicesPage : Fragment() {

    private lateinit var rvDevices: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private val serviceDevice = ServiceDevice()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_devices_page, container, false)

        val btnAddDeviceRedirect: FloatingActionButton = layout.findViewById(R.id.btnAdd)
        rvDevices = layout.findViewById(R.id.lstDevices)

        rvDevices.layoutManager = GridLayoutManager(activity, 2)

        // initilize adapter
        adapter = DeviceAdapter(mutableListOf()) { device -> onDeviceClick(device) }
        rvDevices.adapter = adapter

        // get devices from user profile
        loadUserDevices()

        //add devices redirect
        btnAddDeviceRedirect.setOnClickListener {
            val intent = Intent(activity, AddDevicePage::class.java)
            startActivity(intent)
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        // Refresh list when coming back to devices page
        loadUserDevices()
    }

    private fun loadUserDevices() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userID = sharedPref.getInt("userid", 0)
        lifecycleScope.launch {
            try {
                //API call to get devices
                val devices = serviceDevice.GetAllUserDevices(requireContext(), userID)

                //if not null
                if (devices != null) {
                    // add devices to adapter/listview
                    adapter.updateDevices(devices)
                }
            } catch (e: Exception) {
                // add error log
            }
        }
    }

    private fun onDeviceClick(device: ParseDevice) {
        // navigate to manage device page and passes deviceId
        val intent = Intent(activity, ManageDevice::class.java)
        intent.putExtra("DEVICE_ID", device.DeviceId)
        startActivity(intent)
    }
}
