package com.adr.opsc7312_poe

import android.content.ContentValues
import android.content.Context
import android.util.Log

class DaoDevices (context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addDeviceOffline(userId: Int, deviceId: Int, synced: Boolean = false) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("userId", userId)
            put("deviceId", deviceId)
            put("synced", if (synced) 1 else 0)
        }
        db.insert(DatabaseHelper.TABLE_NAME_DEVICES, null, values)
        Log.d("DaoDevices", "Device $deviceId added offline for User $userId")
        db.close()
    }

    fun getUnsyncedDevices(): List<dbDevice> {
        val devices = mutableListOf<dbDevice>()
        val db = dbHelper.readableDatabase
        val cursor =
            db.query(DatabaseHelper.TABLE_NAME_DEVICES, null, "synced = 0", null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"))
            val deviceId = cursor.getInt(cursor.getColumnIndexOrThrow("deviceId"))
            devices.add(dbDevice(id, userId, deviceId))
        }
        cursor.close()
        db.close()
        return devices
    }

    fun removeDeviceById(deviceId: Int) {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(DatabaseHelper.TABLE_NAME_DEVICES, "deviceId = ?", arrayOf(deviceId.toString()))
        if (rowsDeleted > 0) {
            Log.d("removeDeviceById", "Device $deviceId removed successfully.")
        } else {
            Log.d("removeDeviceById", "No device found with deviceId $deviceId.")
        }
        db.close()
    }

    fun getOfflineDevices(): List<ParseDevice> {
        val allDevices = ServiceDevice().OfflineDevices() ?: return emptyList()

        val userDevices = getUnsyncedDevices().map { it.deviceId }.toSet()

        val offlineDevices = allDevices.filter { it.DeviceId in userDevices }

        return offlineDevices
    }

    fun markDeviceAsSynced(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("synced", 1) }
        db.update(DatabaseHelper.TABLE_NAME_DEVICES, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }
}
data class dbDevice(
    val id: Int,
    val userId: Int,
    val deviceId: Int
)