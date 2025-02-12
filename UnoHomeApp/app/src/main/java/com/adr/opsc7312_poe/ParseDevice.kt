package com.adr.opsc7312_poe

import com.google.gson.annotations.SerializedName

data class ParseDevice(
    @SerializedName("deviceId") val DeviceId: Int,
    @SerializedName("name") val Name: String,
    @SerializedName("status") val Status: Boolean?,
    @SerializedName("category") val Category: String?
)
