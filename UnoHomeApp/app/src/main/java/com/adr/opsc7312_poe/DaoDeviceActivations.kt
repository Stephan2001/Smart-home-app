package com.adr.opsc7312_poe

import android.content.ContentValues
import android.content.Context
import android.util.Log

class DaoDeviceActivations(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Add a device activation record offline
    fun addActivationOffline(deviceId: Int, synced: Boolean = false) {
        val db = dbHelper.writableDatabase

        // Check if a record for the deviceId already exists
        val cursor = db.query(
            DatabaseHelper.TABLE_NAME_ACTIVATIONS,
            arrayOf("id"),
            "deviceId = ?",
            arrayOf(deviceId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            // If record exists, update the synced status
            val values = ContentValues().apply {
                put("synced", if (synced) 1 else 0)
            }
            db.update(
                DatabaseHelper.TABLE_NAME_ACTIVATIONS,
                values,
                "deviceId = ?",
                arrayOf(deviceId.toString())
            )
            Log.d("DaoDeviceActivations", "Device $deviceId updated offline.")
        } else {
            // If no record exists, insert a new one
            val values = ContentValues().apply {
                put("deviceId", deviceId)
                put("synced", if (synced) 1 else 0)
            }
            db.insert(DatabaseHelper.TABLE_NAME_ACTIVATIONS, null, values)
            Log.d("DaoDeviceActivations", "Device $deviceId added offline.")
        }
        cursor.close()
        db.close()
    }

    // Get all unsynced activations
    fun getUnsyncedActivations(): List<DeviceActivation> {
        val activations = mutableListOf<DeviceActivation>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME_ACTIVATIONS, null, "synced = 0", null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val deviceId = cursor.getInt(cursor.getColumnIndexOrThrow("deviceId"))
            activations.add(DeviceActivation(id, deviceId))
        }
        cursor.close()
        db.close()
        return activations
    }

    // Mark an activation as synced
    fun markActivationAsSynced(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("synced", 1) }
        db.update(DatabaseHelper.TABLE_NAME_ACTIVATIONS, values, "id = ?", arrayOf(id.toString()))
        Log.d("DaoDeviceActivations", "Activation $id marked as synced")
        db.close()
    }

    fun clearAllActivations() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_NAME_ACTIVATIONS, null, null)
        Log.d("DaoDeviceActivations", "All activation records cleared after sync.")
        db.close()
    }

}

data class DeviceActivation(
    val id: Int,
    val deviceId: Int
)