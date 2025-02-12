package com.adr.opsc7312_poe

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceReadings {

    suspend fun getSensorReadingsByDeviceName(deviceName: String): List<SensorReading> =
        suspendCancellableCoroutine { continuation ->
            val endpoint = "api/sensors/readings/$deviceName"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<JsonArray>()) { _, response, result ->
                    if (response.statusCode == 404) {
                        Log.d("getSensorReadings", "No sensor readings found for device: $deviceName")
                        continuation.resume(emptyList()) // Resume with an empty list
                    } else {
                        result.fold(
                            success = { jsonArray ->
                                val readings = parseMultipleSensorReadings(jsonArray)
                                Log.d("getSensorReadings", "Retrieved ${readings.size} readings for device: $deviceName")
                                continuation.resume(readings)
                            },
                            failure = { error ->
                                Log.e("getSensorReadings", "Error: ${error.message}")
                                continuation.resumeWithException(error)
                            }
                        )
                    }
                }
        }

    suspend fun getLatestSensorReading(deviceName: String): SensorReading? =
        suspendCancellableCoroutine { continuation ->
            val endpoint = "api/sensors/latestreading/$deviceName"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<JsonObject>()) { _, response, result ->
                    if (response.statusCode == 404) {
                        Log.d("getLatestSensorReading", "No sensor readings found for device: $deviceName")
                        continuation.resume(null)
                    } else {
                        result.fold(
                            success = { jsonObject ->
                                val reading = parseSingleSensorReading(jsonObject)
                                Log.d("getLatestSensorReading", "Retrieved latest reading for device: $deviceName")
                                continuation.resume(reading)
                            },
                            failure = { error ->
                                Log.e("getLatestSensorReading", "Error: ${error.message}")
                                continuation.resumeWithException(error)
                            }
                        )
                    }
                }
        }

    private fun parseSingleSensorReading(jsonObject: JsonObject): SensorReading {
        return SensorReading(
            sensorId = jsonObject["sensorId"].asInt,
            sensorName = jsonObject["sensorName"].asString,
            sensorReading1 = jsonObject["sensorReading1"].asDouble,
            sensorDate = jsonObject["sensorDate"].asString,
            sensorTime = jsonObject["sensorTime"].asString
        )
    }

    fun parseMultipleSensorReadings(jsonArray: JsonArray): List<SensorReading> {
        return jsonArray.map { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            SensorReading(
                sensorId = jsonObject["sensorId"].asInt,
                sensorName = jsonObject["sensorName"].asString,
                sensorReading1 = jsonObject["sensorReading1"].asDouble,
                sensorDate = jsonObject["sensorDate"].asString,
                sensorTime = jsonObject["sensorTime"].asString
            )
        }
    }
}

data class SensorReading(
    val sensorId: Int,
    val sensorName: String,
    val sensorReading1: Double,
    val sensorDate: String,
    val sensorTime: String
)