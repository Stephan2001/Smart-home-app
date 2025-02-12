package com.adr.opsc7312_poe

import android.content.Context
import android.devicelock.DeviceId
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.suspendCancellableCoroutine
import org.checkerframework.checker.units.qual.m
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceRoutine {

    /*
     * Calling API to create a routine on our database tied to specified user
     * Parameters -> RoutineName, Description, status (Active/Inactive - default), scheduling (Datetime interval)
     *               UserID, ListOf DeviceID's
     * SuccessConditions -> statusCode 201
     * see logs for request details
     */
    suspend fun AddRoutine(context: Context, name: String, description: String, status:Boolean, scheduling:String, isActive: Boolean, userId:Int, deviceIds:Array<Int>, minutes:Int, hours:Int ):Int? = suspendCancellableCoroutine { continuation ->

        if (!isConnected(context)) {
            // Device is not connected
            val daoRoutine = DaoRoutine(context)
            daoRoutine.addRoutine(name, description, status, scheduling, isActive, userId, deviceIds, minutes, hours)
            Log.d("AddRoutine", "Routine $name saved locally")
            continuation.resume(0)
        } else {
            val endpoint = "api/Routine"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            val jsonBody = """
        {
            "name": "$name",
            "description": "$description",
            "status": $status,
            "scheduling": "$scheduling",
            "isActive": $isActive,
            "userId": $userId,
            "deviceIds": ${deviceIds.joinToString(prefix = "[", postfix = "]")}
        }
        """.trimIndent()

            Fuel.post(url)
                .body(jsonBody)
                .header("Content-Type", "application/json")
                .responseString { _, response, result ->

                    result.fold(
                        success = { data ->
                            if (response.statusCode == 200) {
                                try {
                                    val routineId = data.trim().toInt() // Convert the returned data to an integer
                                    Log.d("AddRoutine", "Routine $name added to User $userId profile successfully with ID: $routineId")
                                    val daoEvents = DaoEvents(context)
                                    daoEvents.addEvent("Routine $name was created")
                                    continuation.resume(routineId) // Resume with the routineId
                                } catch (e: NumberFormatException) {
                                    Log.e("AddRoutine", "Failed to parse routine ID from response: ${e.message}")
                                    continuation.resumeWithException(Exception("Invalid routine ID format"))
                                }
                            } else {
                                Log.e("AddRoutine", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("AddRoutine", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to return routine on our database
     * Parameters -> RoutineID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns -> ParseRoutine
     */
    suspend fun GetRoutineById(id: Int): ParseRoutine? = suspendCancellableCoroutine { continuation ->
        val endpoint = "api/Routine/$id"
        val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

        Fuel.get(url)
            .responseObject(gsonDeserializer<ParseRoutine>()) { _, response, result ->
                result.fold(
                    success = { routine ->
                        Log.d("GetRoutineById", "Details for routine $id: $routine")
                        continuation.resume(routine)
                    },
                    failure = { error ->
                        Log.e("GetRoutineById", "Error fetching routine $id: ${error.message}")
                        continuation.resumeWithException(error)
                    }
                )
            }
    }

    /*
     * Calling API to return all routines on our database tied to specified user
     * Parameters -> userID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns -> listOf ParseRoutine + ParseDevice data
     */
    suspend fun GetAllRoutinesByUserId(context: Context,id: Int): List<ParseRoutine>? = suspendCancellableCoroutine { continuation ->
        if (!isConnected(context)) {
            // Device is not connected
            val daoRoutine = DaoRoutine(context)
            val routines = daoRoutine.getAllOfflineRoutines()
            continuation.resume(routines)
        }
        else{
            val endpoint = "api/Routine/user/$id"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<JsonArray>()) { _, response, result ->
                    result.fold(
                        success = { jsonArray ->
                            val routines = parseMultipleRoutines(jsonArray)
                            Log.d("GetAllRoutinesByUserId", "Retrieved ${routines.size} routines for user $id")
                            continuation.resume(routines)
                        },
                        failure = { error ->
                            Log.e("GetAllRoutinesByUserId", "Error fetching routines for user $id: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to remove a routine
     * Parameters -> routineID
     * SuccessConditions -> statusCode 204
     * see logs for request details
     */
    suspend fun RemoveRoutine(context: Context,routineId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isConnected(context)) {
            // Device is not connected
            val daoRoutine = DaoRoutine(context)
            daoRoutine.deleteRoutineById(routineId)
            continuation.resume(Unit)
        }
        else{
            val endpoint = "api/Routine/$routineId"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.delete(url)
                .response { _, response, result ->
                    result.fold(
                        success = {
                            if (response.statusCode == 204) {
                                Log.d("RemoveRoutine", "Routine $routineId removed successfully")
                                continuation.resume(Unit)
                            } else {
                                Log.e("RemoveRoutine", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("RemoveRoutine", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Simple method for parsing routines to ParseRoutine
     * this was the first test implementation and was not simplified to match Service theme parse
     * if it works it works
     */
    fun parseMultipleRoutines(jsonArray: JsonArray): List<ParseRoutine> {
        return jsonArray.map { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            ParseRoutine(
                RoutineId = if (jsonObject.has("routineId")) jsonObject["routineId"].asInt else 0,
                Name = if (jsonObject.has("name")) jsonObject["name"].asString else "",
                Description = if (jsonObject.has("description")) jsonObject["description"].asString else null,
                Status = if (jsonObject.has("status")) jsonObject["status"].asBoolean else false,
                Scheduling = if (jsonObject.has("scheduling")) jsonObject["scheduling"].asString else null,
                IsActive = if (jsonObject.has("isActive")) jsonObject["isActive"].asBoolean else false,
                Devices = if (jsonObject.has("devices")) {
                    jsonObject["devices"].asJsonArray.map { deviceJson ->
                        DeviceDto(
                            DeviceId = if (deviceJson.asJsonObject.has("deviceId")) deviceJson.asJsonObject["deviceId"].asInt else 0,
                            Name = if (deviceJson.asJsonObject.has("name")) deviceJson.asJsonObject["name"].asString else "",
                            Status = if (deviceJson.asJsonObject.has("status")) deviceJson.asJsonObject["status"].asBoolean else false
                        )
                    }
                } else {
                    emptyList()
                }
            )
        }
    }

    /*
     * Calling API to toggle a routine's active status on our database tied to specified user
     * Parameters -> routineID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Notes - This API call will switch the status of any devices linked to the routine to match the
     * active status of the theme itself
     */
    suspend fun ToggleRoutineStatus(context: Context, routineId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        //val endpoint = "api/Routine/toggle/$routineId"
        val url = "https://unohomewebapiappservice.azurewebsites.net/api/Routine/toggle/$routineId"

        Fuel.put(url)
            .header("Content-Type", "application/json")
            .responseString { _, response, result ->
                result.fold(
                    success = { responseBody ->
                        if (response.statusCode == 200) {
                            Log.d("ToggleRoutineStatus", "Routine status updated successfully")

                            val jsonObject = JSONObject(responseBody)
                            val routineName = jsonObject.optString("name", "Empty Routine")
                            val status = jsonObject.optString("status", "inactive")
                            val daoEvents = DaoEvents(context)
                            daoEvents.addEvent("Routine $routineName was turned $status")

                            continuation.resume(Unit)
                        } else {
                            Log.e("ToggleRoutineStatus", "Failed with status code: ${response.statusCode}")
                            continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                        }
                    },
                    failure = { error ->
                        Log.e("ToggleRoutineStatus", "Error: ${error.message}")
                        continuation.resumeWithException(error)
                    }
                )
            }
    }

    /*
     * Calling API to update a routine's status on our database tied to specified user
     * Parameters -> routineID, status
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Notes - this api call is mostly just to reflect the current state of routine for UI in manageRoutine
     */
    suspend fun UpdateRoutineStatus(routineId: Int, status: Boolean) = suspendCancellableCoroutine<Unit> { continuation ->
        val endpoint = "api/Routine/status"
        val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

        Fuel.put(url, listOf("id" to routineId, "newStatus" to status))
            .header("Content-Type", "application/json")
            .response { _, response, result ->
                result.fold(
                    success = {
                        if (response.statusCode == 200) {
                            Log.d("UpdateRoutineStatus", "Routine status updated successfully")
                            continuation.resume(Unit)
                        } else {
                            Log.e("UpdateRoutineStatus", "Failed with status code: ${response.statusCode}")
                            continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                        }
                    },
                    failure = { error ->
                        Log.e("UpdateRoutineStatus", "Error: ${error.message}")
                        continuation.resumeWithException(error)
                    }
                )
            }
    }

    /*
     * Calling API to update a routine's scheduling on our database tied to specified user
     * Parameters -> routineID, newSchedule
     * SuccessConditions -> statusCode 200
     * see logs for request details
     */
    suspend fun UpdateRoutineSchedule(routineId: Int, newSchedule: String) = suspendCancellableCoroutine<Unit> { continuation ->
        val endpoint = "api/Routine/$routineId/scheduling"
        val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

        val fullUrl = "$url?newScheduling=$newSchedule"

        Fuel.put(fullUrl)
            .header("Content-Type", "application/json")
            .response { _, response, result ->
                result.fold(
                    success = {
                        if (response.statusCode == 200) {
                            Log.d("UpdateRoutineSchedule", "Routine $routineId schedule updated successfully")
                            continuation.resume(Unit)
                        } else {
                            Log.e("UpdateRoutineSchedule", "Failed with status code: ${response.statusCode}")
                            continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                        }
                    },
                    failure = { error ->
                        Log.e("UpdateRoutineSchedule", "Error: ${error.message}")
                        continuation.resumeWithException(error)
                    }
                )
            }
    }

    suspend fun syncRoutines(context: Context) {
        val routineDao = DaoRoutine(context)
        val unsyncedRoutines = routineDao.getUnsyncedRoutines()

        for (routine in unsyncedRoutines) {
            try {
                val workerID = AddRoutine(
                    context,
                    name = routine.name,
                    description = routine.description,
                    status = routine.status,
                    scheduling = routine.scheduling,
                    isActive = routine.isActive,
                    userId = routine.userId,
                    deviceIds = routine.deviceIds,
                    minutes = routine.minutes,
                    hours = routine.hours
                )
                val uniqueName = "${workerID}${routine.name}Worker"
                Log.d("INSURANCE", "INSURANCE: $uniqueName")
                ServiceScheduleWorker(context, routine.hours, routine.minutes, uniqueName, workerID!!)
                Log.d("AddRoutinePage", "Routine created with devices: ${routine.deviceIds}")
                routineDao.markRoutineAsSynced(routine.id)
            } catch (e: Exception) {
                Log.e("SyncRoutines", "Failed to sync routine ${routine.name}: ${e.message}")
            }
        }
    }

    fun UpdateRoutineScheduleOffline(context: Context, routineId: Int ,minutes: Int, hours: Int){
        val routineDao = DaoRoutine(context)
        routineDao.updateRoutineTime(routineId, minutes, hours)
        Log.d("UpdateRoutineScheduleOffline", "Offline routine rescheduled to: ${hours}:${minutes}")
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
