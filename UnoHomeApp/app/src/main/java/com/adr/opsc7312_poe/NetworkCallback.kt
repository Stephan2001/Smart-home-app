package com.adr.opsc7312_poe

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast

class NetworkCallback(private val context: Context) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d("NetworkCallback", "Network is available")
        Toast.makeText(context, "Back online", Toast.LENGTH_SHORT).show()
        ServiceScheduleSyncWorker(context)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.d("NetworkCallback", "Network is lost")
        Toast.makeText(context, "You are offline", Toast.LENGTH_SHORT).show()
    }

    override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, capabilities)
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            Log.d("NetworkCallback", "Internet IS available")
        } else {
            Log.d("NetworkCallback", "Internet is NOT available")
        }
    }
}