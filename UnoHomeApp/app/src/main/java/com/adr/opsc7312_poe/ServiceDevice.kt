package com.adr.opsc7312_poe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceDevice {

    /*
     * Calling API to return devices details on our database
     * Parameters -> deviceID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns -> ParseDevice
     */
    suspend fun GetDeviceById(context: Context, id: Int): ParseDevice? = suspendCancellableCoroutine { continuation ->
        if(!isConnected(context)){
            val device = getDeviceById(id)
            Log.d("device", "What is my device ${device?.Name}")
            continuation.resume(device)
        }
        else{
            val endpoint = "api/Device/$id"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<ParseDevice>()) { _, response, result ->
                    result.fold(
                        success = { device ->
                            Log.d("GetDeviceById", "Details for device $id: $device")
                            continuation.resume(device)
                        },
                        failure = { error ->
                            Log.e("GetDeviceById", "Error fetching device $id: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to toggle a device's active status on our database
     * Parameters -> deviceID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     */
    suspend fun ChangeDeviceStatus(context: Context, id: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isConnected(context)){
            val dao = DaoDeviceActivations(context)
            dao.addActivationOffline(id)
        }
        else {
            val endpoint = "api/Device/toggle/$id"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.put(url)
                .responseString { _, response, result ->
                    result.fold(
                        success = { responseBody ->
                            if (response.statusCode == 200) {

                                val jsonObject = JSONObject(responseBody)
                                val deviceName = jsonObject.optString("name", "Empty device")
                                val status = jsonObject.optString("status", "off")
                                val daoEvents = DaoEvents(context)
                                daoEvents.addEvent("device $deviceName was turned $status")

                                continuation.resume(Unit)
                            } else {
                                Log.e("ChangeDeviceStatus", "Failed to change device status. Status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("ChangeDeviceStatus", "Error changing device status: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to return a device's active status on our database
     * Parameters -> deviceID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns => boolean (True=Active/False=Inactive)
     */
    suspend fun GetDeviceStatus(context: Context, id: Int): Boolean? = suspendCancellableCoroutine { continuation ->
        if (!isConnected(context)){
            continuation.resume(false)
        }
        else{
            val endpoint = "api/Device/status/$id"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<JsonObject>()) { _, response, result ->
                    result.fold(
                        success = { json ->
                            if (response.statusCode == 200) {
                                val status = json?.get("status")?.asBoolean ?: false
                                Log.d("GetDeviceStatus", "Device $id is now ${if (status) "ON" else "OFF"}")
                                continuation.resume(status)
                            } else {
                                Log.e("GetDeviceStatus", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("GetDeviceStatus", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to return all devices on our database
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns => listOf ParseDevice
     */
    suspend fun GetAllDevices(): List<ParseDevice>? = suspendCancellableCoroutine { continuation ->
        val endpoint = "api/Device/all"
        val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

        Fuel.get(url)
            .responseObject(gsonDeserializer<JsonArray>()) { _, response, result ->
                if (response.statusCode == 404) {
                    Log.d("GetAllDevices", "No devices found (404 Not Found)")
                    continuation.resume(emptyList()) // Resume with an empty list
                } else {
                    result.fold(
                        success = { jsonArray ->
                            val devices = parseMultipleDevices(jsonArray)
                            Log.d("GetAllDevices", "Retrieved ${devices.size} devices")
                            continuation.resume(devices)
                        },
                        failure = { error ->
                            // Handle other errors
                            Log.e("GetAllDevices", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
            }
    }

    /*
     * Calling API to return all devices on our database associated to a specified user
     * Parameters -> userID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns => listOf ParseDevice
     */
    suspend fun GetAllUserDevices(context: Context, userId: Int): List<ParseDevice>? = suspendCancellableCoroutine { continuation ->
        if (!isConnected(context)){
            val dao = DaoDevices(context)
            val devices = dao.getOfflineDevices()
            continuation.resume(devices)
        }
        else{
            val endpoint = "api/Device/user/$userId"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<JsonArray>()) { _, response, result ->
                    if (response.statusCode == 404) {
                        val device = ParseDevice(99, "", false , "")
                        Log.d("GetAllDevices", "No devices found (404 Not Found). Treating as a successful response.")
                        continuation.resume(listOf(device)) // Resume with an empty list
                    } else {
                        result.fold(
                            success = { jsonArray ->
                                val devices = parseMultipleDevices(jsonArray)
                                Log.d("GetAllUserDevices", "Retrieved ${devices.size} devices")
                                continuation.resume(devices)
                            },
                            failure = { error ->
                                // Handle other errors
                                Log.e("GetAllUserDevices", "Error: ${error.message}")
                                continuation.resumeWithException(error)
                            }
                        )
                    }
                }
        }
    }

    /*
     * Calling API to create a device on our database associated to a specified user
     * Parameters -> userID, deviceID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     */
    suspend fun AddDevice(context:Context, UserId: Int, DeviceId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isConnected(context)){
            val dao = DaoDevices(context)
            dao.addDeviceOffline(UserId, DeviceId)
            Log.d("AddDevice", "Device $DeviceId saved locally")
            continuation.resume(Unit)
        }
        else{
            val endpoint = "api/Device/addDevice"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            val jsonBody = """
        {
            "UserId": $UserId,
            "DeviceId": $DeviceId
        }
        """.trimIndent()

            Fuel.post(url)
                .body(jsonBody)
                .header("Content-Type", "application/json")
                .response { _, response, result ->
                    result.fold(
                        success = {
                            if (response.statusCode == 200) {
                                Log.d("AddDevice", "Device $DeviceId added to User $UserId profile successfully")
                                val daoEvents = DaoEvents(context)
                                daoEvents.addEvent("Device was added to profile")
                                continuation.resume(Unit)
                            } else {
                                Log.e("AddDevice", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("AddDevice", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to remove a device on our database associated to a specified user
     * Parameters -> userID, deviceID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     */
    suspend fun RemoveDevice(context: Context, UserId: Int, DeviceId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        if(!isConnected(context)){
            val dao = DaoDevices(context)
            dao.removeDeviceById(deviceId = DeviceId)
            continuation.resume(Unit)
        }
        else{
            val endpoint = "api/Device/deleteDevice/$UserId/$DeviceId"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.delete(url)
                .response { _, response, result ->
                    result.fold(
                        success = {
                            if (response.statusCode == 200) {
                                Log.d("RemoveDevice", "Device $DeviceId removed from User $UserId profile successfully")
                                continuation.resume(Unit)
                            } else {
                                Log.e("RemoveDevice", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("RemoveDevice", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    suspend fun panicDevice(context: Context) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isConnected(context)) {
            Log.e("panicDevice", "No internet connection. Cannot trigger panic mode.")
            continuation.resumeWithException(Exception("No internet connection"))
        } else {
            val endpoint = "api/Device/panic"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.put(url)
                .response { _, response, result ->
                    result.fold(
                        success = {
                            if (response.statusCode == 200) {
                                Log.d("panicDevice", "Panic mode triggered successfully")
                                continuation.resume(Unit)
                            } else {
                                Log.e("panicDevice", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("panicDevice", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }


    suspend fun syncDevices(context: Context) {
        val dao = DaoDevices(context)
        val unsyncedDevices = dao.getUnsyncedDevices()

        for (device in unsyncedDevices) {
            try {
                AddDevice(context, device.userId, device.deviceId)
                dao.markDeviceAsSynced(device.id)
            } catch (e: Exception) {
                Log.e("SyncDevices", "Failed to sync device ${device.deviceId}: ${e.message}")
            }
        }
    }

    suspend fun syncActivations(context: Context) {
        val dao = DaoDeviceActivations(context)
        val unsyncedDevices = dao.getUnsyncedActivations()

        for (device in unsyncedDevices) {
            try {
                ChangeDeviceStatus(context, device.deviceId)
                dao.markActivationAsSynced(device.id)
            } catch (e: Exception) {
                Log.e("syncActivations", "Failed to sync activation ${device.deviceId}: ${e.message}")
            }
        }
        dao.clearAllActivations()
    }

    fun getDeviceById(deviceId: Int): ParseDevice? {
        val devices = OfflineDevices() ?: return null
        return devices.find { it.DeviceId == deviceId }
    }

    // temporary hardcoded data for devices when users are using offline mode
    fun OfflineDevices(): List<ParseDevice>? {
        return listOf(
            ParseDevice(DeviceId = 1, Name = "PIR Motion Sensor", Status = null, Category = "Sensors"),
            ParseDevice(DeviceId = 2, Name = "Photocell Sensor", Status = null, Category = "Sensors"),
            ParseDevice(DeviceId = 9, Name = "Yellow LED", Status = null, Category = "Lights"),
            ParseDevice(DeviceId = 10, Name = "White LED", Status = null, Category = "Lights"),
            ParseDevice(DeviceId = 12, Name = "Fan", Status = null, Category = "Fans"),
            ParseDevice(DeviceId = 13, Name = "Door Controller", Status = null, Category = "Controls"),
            ParseDevice(DeviceId = 14, Name = "Window Controller", Status = null, Category = "Controls")
        )
    }

    /*
     * Simple method for parsing devices to list of ParseDevice
     */
    fun parseMultipleDevices(jsonArray: JsonArray): List<ParseDevice> {
        return jsonArray.map { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            ParseDevice(
                DeviceId = if (jsonObject.has("deviceId")) jsonObject["deviceId"].asInt else 0,
                Name = if (jsonObject.has("name")) jsonObject["name"].asString else "",
                Status = if (jsonObject.has("status")) jsonObject["status"].asBoolean else null,
                Category = if (jsonObject.has("category")) jsonObject["category"].asString else null
            )
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}