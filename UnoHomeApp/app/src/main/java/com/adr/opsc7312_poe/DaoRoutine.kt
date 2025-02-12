package com.adr.opsc7312_poe

import android.content.ContentValues
import android.content.Context
import org.checkerframework.checker.units.qual.m

class DaoRoutine(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addRoutine(name: String, description: String, status: Boolean, scheduling: String, isActive: Boolean, userId: Int, deviceIds: Array<Int>, minutes:Int, hours:Int, synced: Boolean = false) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("status", if (status) 1 else 0)
            put("scheduling", scheduling)
            put("isActive", if (isActive) 1 else 0)
            put("userId", userId)
            put("deviceIds", deviceIds.joinToString(","))
            put("minutes", minutes)
            put("hours", hours)
            put("synced", if (synced) 1 else 0)
        }
        db.insert(DatabaseHelper.TABLE_NAME_ROUTINES, null, values)
        db.close()
    }

    fun getUnsyncedRoutines(): List<dbRoutine> {
        val routines = mutableListOf<dbRoutine>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME_ROUTINES, null, "synced = 0", null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val status = cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1
            val scheduling = cursor.getString(cursor.getColumnIndexOrThrow("scheduling"))
            val isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"))
            val deviceIds = cursor.getString(cursor.getColumnIndexOrThrow("deviceIds")).split(",").map { it.toInt() }.toTypedArray()
            val minutes = cursor.getInt(cursor.getColumnIndexOrThrow("minutes"))
            val hours = cursor.getInt(cursor.getColumnIndexOrThrow("hours"))

            routines.add(dbRoutine(id, name, description, status, scheduling, isActive, userId, deviceIds, minutes, hours))
        }
        cursor.close()
        return routines
    }

    fun updateRoutineTime(id: Int, newMinutes: Int, newHours: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("minutes", newMinutes)
            put("hours", newHours)
        }
        db.update(DatabaseHelper.TABLE_NAME_ROUTINES, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getAllOfflineRoutines(): List<ParseRoutine> {
        val routines = mutableListOf<ParseRoutine>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME_ROUTINES, null, "synced = 0", null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val status = cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1
            val scheduling = cursor.getString(cursor.getColumnIndexOrThrow("scheduling"))
            val isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1
            val deviceIds = cursor.getString(cursor.getColumnIndexOrThrow("deviceIds")).split(",").map { it.toInt() }

            // Create DeviceDto instances with temporary values
            val devices = deviceIds.map { deviceId ->
                DeviceDto(
                    DeviceId = deviceId,
                    Name = "TempName",
                    Status = false
                )
            }

            routines.add(ParseRoutine(id, name, description, status, scheduling, isActive, devices))
        }
        cursor.close()
        db.close()
        return routines
    }

    fun deleteRoutineById(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_NAME_ROUTINES, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun markRoutineAsSynced(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("synced", 1) }
        db.update(DatabaseHelper.TABLE_NAME_ROUTINES, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }
}

data class dbRoutine(
    val id: Int,
    val name: String,
    val description: String,
    val status: Boolean,
    val scheduling: String,
    val isActive: Boolean,
    val userId: Int,
    val deviceIds: Array<Int>,
    val minutes:Int,
    val hours:Int
)
