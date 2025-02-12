package com.adr.opsc7312_poe

import com.google.gson.annotations.SerializedName

data class ParseRoutine(
    @SerializedName("routineId") val RoutineId: Int,
    @SerializedName("name") val Name: String,
    @SerializedName("description") val Description: String?,
    @SerializedName("status") val Status: Boolean,
    @SerializedName("scheduling") val Scheduling: String?,
    @SerializedName("isActive") val IsActive: Boolean,
    @SerializedName("devices") var Devices: List<DeviceDto>
)

data class DeviceDto(
    @SerializedName("deviceId") val DeviceId: Int,
    @SerializedName("name") val Name: String,
    @SerializedName("status") var Status: Boolean
)