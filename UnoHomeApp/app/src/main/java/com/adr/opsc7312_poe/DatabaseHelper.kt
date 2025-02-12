package com.adr.opsc7312_poe

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_NAME_THEMES (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, userId INTEGER, deviceIds TEXT, synced INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE $TABLE_NAME_ROUTINES (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, description TEXT, status INTEGER, scheduling TEXT, isActive INTEGER, userId INTEGER, deviceIds TEXT, minutes INTEGER, hours INTEGER, synced INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE $TABLE_NAME_DEVICES (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, deviceId INTEGER, synced INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE $TABLE_NAME_ACTIVATIONS (id INTEGER PRIMARY KEY AUTOINCREMENT, deviceId INTEGER, synced INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE $TABLE_NAME_EVENTS (id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, message TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_THEMES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ROUTINES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_DEVICES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ACTIVATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_EVENTS")
        onCreate(db)
    }

    fun removeTable(db: SQLiteDatabase?) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_THEMES")
    }

    fun clearDatabase(db: SQLiteDatabase?) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_THEMES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ROUTINES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_DEVICES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ACTIVATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_EVENTS")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "themes.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME_THEMES = "Themes"
        const val TABLE_NAME_ROUTINES = "Routines"
        const val TABLE_NAME_DEVICES = "Devices"
        const val TABLE_NAME_ACTIVATIONS = "Activations"
        const val TABLE_NAME_EVENTS = "Events"
    }
}