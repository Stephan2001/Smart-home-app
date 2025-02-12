package com.adr.opsc7312_poe

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DaoEvents(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addEvent(message: String) {
        val db = dbHelper.writableDatabase

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("timestamp", timestamp)
            put("message", message)
        }
        db.insert(DatabaseHelper.TABLE_NAME_EVENTS, null, values)
        db.close()
    }

    fun getAllEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME_EVENTS} ORDER BY timestamp DESC", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
            val message = cursor.getString(cursor.getColumnIndexOrThrow("message"))
            events.add(Event(id, timestamp, message))
        }
        cursor.close()
        db.close()
        return events
    }

}

data class Event(
    val id: Int,
    val timestamp: String,
    val message: String
)