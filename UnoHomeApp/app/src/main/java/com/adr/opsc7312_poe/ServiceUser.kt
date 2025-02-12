package com.adr.opsc7312_poe

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceUser {

    /*
     * Calling API registering a new user on our database
     * Parameters -> userEmail
     * SuccessConditions -> statusCode 200
     * see logs for request details
     */
    suspend fun CreateUser(email:String) = suspendCancellableCoroutine<Unit> { continuation ->

        val endpoint = "api/User/create"
        val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

        val jsonBody = """
        {
            "email": "$email"
        }
        """.trimIndent()

        Fuel.post(url)
            .body(jsonBody)
            .header("Content-Type", "application/json")
            .response { _, response, result ->
                result.fold(
                    success = {
                        if (response.statusCode == 200) {
                            Log.d("CreateUser", "User $email profile created successfully")
                            continuation.resume(Unit)
                        } else {
                            Log.e("CreateUser", "Failed with status code: ${response.statusCode}")
                            continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                        }
                    },
                    failure = { error ->
                        Log.e("CreateUser", "Error: ${error.message}")
                        continuation.resumeWithException(error)
                    }
                )
            }
    }

    /*
     * calling API validating that a user exists in our database
     * Parameters -> userEmail
     * SuccessConditions -> statusCode 200
     * see logs for request details
     * Returns -> userID
     */
    suspend fun validateUser(email: String): Int? = suspendCancellableCoroutine { continuation ->
        val endpoint = "api/User/validate?email=$email"
        val url = "https://unohomewebapiappservice.azurewebsites.net/$endpoint"

        Fuel.get(url)
            .header("Content-Type", "application/json")
            .responseString { _, response, result ->
                result.fold(
                    success = { data ->
                        if (response.statusCode == 200) {
                            try {
                                val userId = data.trim().toInt() // Trim in case there are extra spaces
                                Log.d("ValidateUser", "User ID: $userId")
                                continuation.resume(userId)  // Return userId
                            } catch (e: NumberFormatException) {
                                Log.e("ValidateUser", "Failed to parse user ID: ${e.message}")
                                continuation.resumeWithException(e)
                            }
                        } else {
                            Log.e("ValidateUser", "Failed with status code: ${response.statusCode}")
                            continuation.resumeWithException(Exception("Failed with status code: ${response.statusCode}"))
                        }
                    },
                    failure = { error ->
                        Log.e("ValidateUser", "Error: ${error.message}")
                        continuation.resumeWithException(error)
                    }
                )
            }
    }

}