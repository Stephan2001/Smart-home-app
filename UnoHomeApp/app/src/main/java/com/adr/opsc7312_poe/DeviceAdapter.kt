package com.adr.opsc7312_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(
    private var devices: MutableList<ParseDevice>,
    private val onDeviceClick: (ParseDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.txtDeviceName.text = device.Name

        // on click to navigate
        holder.itemView.setOnClickListener {
            onDeviceClick(device)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    // update list and notify changes
    fun updateDevices(newDevices: List<ParseDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDeviceName: TextView = itemView.findViewById(R.id.txtDeviceName)
    }
}

