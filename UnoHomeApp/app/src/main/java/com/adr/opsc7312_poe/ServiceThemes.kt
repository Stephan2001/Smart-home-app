package com.adr.opsc7312_poe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.google.gson.Gson
import com.google.gson.JsonArray
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceThemes {

    /*
     * Calling API creating a new theme on our database tied to specified user
     * Parameters -> themeName, userID, ListOf DeviceID's
     * SuccessConditions -> statusCode 200
     * see logs for request details
     */
    suspend fun CreateTheme(context: Context ,name: String, userId:Int, deviceIds:Array<Int> ) = suspendCancellableCoroutine<Unit> { continuation ->

        if (!isConnected(context)) {
            // Device is not connected
            val themeDao = DaoThemes(context)
            themeDao.addTheme(name, userId, deviceIds)
            Log.d("CreateTheme", "Theme $name saved locally")
            continuation.resume(Unit)
        } else {
            // Device is connected
            val endpoint = "api/Themes/Create"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            val jsonBody = """
        {
            "Name": "$name",
            "UserId": $userId,
            "DeviceIds": ${deviceIds.joinToString(prefix = "[", postfix = "]")}
        }
        """.trimIndent()

            Fuel.post(url)
                .body(jsonBody)
                .header("Content-Type", "application/json")
                .response { _, response, result ->
                    result.fold(
                        success = {
                            if (response.statusCode == 200) {
                                Log.d("CreateTheme", "Theme $name added to User $userId profile successfully")
                                val daoEvents = DaoEvents(context)
                                daoEvents.addEvent("Theme $name was created")
                                continuation.resume(Unit)
                            } else {
                                Log.e("CreateTheme", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("CreateTheme", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API removing a theme on our database tied to specified user
     * Parameters -> themeID
     * SuccessConditions -> statusCode 204
     * see logs for request details
     */
    suspend fun RemoveTheme(context:Context, themeId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isConnected(context)) {
            // Device is not connected
            val themeDao = DaoThemes(context)
            themeDao.deleteThemeById(themeId)
            continuation.resume(Unit)
        }
        else{
            val endpoint = "api/Themes/$themeId"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.delete(url)
                .response { _, response, result ->
                    result.fold(
                        success = {
                            if (response.statusCode == 204) {
                                Log.d("RemoveTheme", "Theme $themeId removed successfully")
                                continuation.resume(Unit)
                            } else {
                                Log.e("RemoveTheme", "Failed with status code: ${response.statusCode}")
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
     * Calling API to toggle a theme's active status on our database tied to specified user
     * Parameters -> themeID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Notes - This API call will switch the status of any devices linked to the theme to match the
     * active status of the theme itself
     */
    suspend fun ToggleThemeStatus(context:Context, themeId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isConnected(context)) {
            // Device is not connected
            Toast.makeText(context, "Unavailable while offline", Toast.LENGTH_SHORT).show()
            continuation.resume(Unit)
        }
        else{
            val endpoint = "api/Themes/ToggleStatus?ThemeID=$themeId"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.put(url)
                .header("Content-Type", "application/json")
                .responseString  { _, response, result ->
                    result.fold(
                        success = { responseBody ->
                            if (response.statusCode == 200) {
                                Log.d("ToggleThemeStatus", "Theme status updated successfully")

                                val jsonObject = JSONObject(responseBody)
                                val themeName = jsonObject.optString("themeName", "Empty Theme")
                                val status = jsonObject.optString("status", "off")
                                val daoEvents = DaoEvents(context)
                                daoEvents.addEvent("Theme $themeName was turned $status")

                                continuation.resume(Unit)
                            } else {
                                Log.e("ToggleThemeStatus", "Failed with status code: ${response.statusCode}")
                                continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                            }
                        },
                        failure = { error ->
                            Log.e("ToggleThemeStatus", "Error: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Calling API to return all themes on our database tied to specified user
     * Parameters -> userID
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns -> ParseTheme + ParseDevice data
     */
    suspend fun GetAllThemesByUserId(context:Context,id: Int): List<ParseTheme>? = suspendCancellableCoroutine { continuation ->
        if (!isConnected(context)) {
            // Device is not connected
            val themeDao = DaoThemes(context)
            val Themes = themeDao.getOfflineThemes()
            continuation.resume(Themes)
        }
        else{
            val endpoint = "api/Themes/User/$id"
            val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

            Fuel.get(url)
                .responseObject(gsonDeserializer<JsonArray>()) { _, response, result ->
                    result.fold(
                        success = { jsonArray ->
                            val themes = parseMultipleThemes(jsonArray)
                            Log.d("GetAllThemesByUserId", "Retrieved ${themes.size} themes for user $id")
                            continuation.resume(themes)
                        },
                        failure = { error ->
                            Log.e("GetAllThemesByUserId", "Error fetching themes for user $id: ${error.message}")
                            continuation.resumeWithException(error)
                        }
                    )
                }
        }
    }

    /*
     * Simple method used for parsing themes and devices
     */
    fun parseMultipleThemes(jsonArray: JsonArray): List<ParseTheme> {
        val gson = Gson()
        return jsonArray.map { jsonElement ->
            gson.fromJson(jsonElement, ParseTheme::class.java)
        }
    }

    suspend fun syncThemes(context: Context) {
        val themeDao = DaoThemes(context)
        val unsyncedThemes = themeDao.getUnsyncedThemes()

        for (theme in unsyncedThemes) {
            try {
                CreateTheme(context, theme.name, theme.userId, theme.deviceIds)
                themeDao.markThemeAsSynced(theme.id)  // Use the id to mark the theme as synced
            } catch (e: Exception) {
                Log.e("SyncThemes", "Failed to sync theme ${theme.name}: ${e.message}")
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}