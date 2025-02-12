package com.adr.opsc7312_poe

import android.content.ContentValues
import android.content.Context

class DaoThemes(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addTheme(name: String, userId: Int, deviceIds: Array<Int>, synced: Boolean = false) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("userId", userId)
            put("deviceIds", deviceIds.joinToString(","))
            put("synced", if (synced) 1 else 0)
        }
        db.insert(DatabaseHelper.TABLE_NAME_THEMES, null, values)
        db.close()
    }

    fun getUnsyncedThemes(): List<dbTheme> {
        val themes = mutableListOf<dbTheme>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME_THEMES, null, "synced = 0", null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))  // Fetching the id
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"))
            val deviceIds = cursor.getString(cursor.getColumnIndexOrThrow("deviceIds")).split(",").map { it.toInt() }.toTypedArray()
            themes.add(dbTheme(id, name, userId, deviceIds))  // Pass the id to the dbTheme constructor
        }
        cursor.close()
        db.close()
        return themes
    }

    fun getOfflineThemes(): List<ParseTheme> {
        val themes = mutableListOf<ParseTheme>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME_THEMES, null, "synced = 0", null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val deviceIds = cursor.getString(cursor.getColumnIndexOrThrow("deviceIds")).split(",").map { it.toInt() }.toTypedArray()

            // Create DeviceDto instances in memory
            val devices = deviceIds.map { deviceId ->
                DeviceDto(
                    DeviceId = deviceId,
                    Name = "TempName", // Temporary name for the device
                    Status = false // Default status
                )
            }

            // Add to the themes list as ParseTheme
            themes.add(ParseTheme(id, name, false, devices))
        }
        cursor.close()
        db.close()
        return themes
    }

    fun deleteThemeById(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_NAME_THEMES, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun markThemeAsSynced(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("synced", 1) }
        db.update(DatabaseHelper.TABLE_NAME_THEMES, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }
}

data class dbTheme(
    val id: Int,
    val name: String,
    val userId: Int,
    val deviceIds: Array<Int>
)