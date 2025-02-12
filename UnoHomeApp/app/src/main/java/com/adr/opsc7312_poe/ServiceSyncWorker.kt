package com.adr.opsc7312_poe

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ServiceSyncWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val serviceThemes = ServiceThemes()
        val serviceRoutine = ServiceRoutine()
        val serviceDevices = ServiceDevice()
        return try {
            serviceThemes.syncThemes(applicationContext)
            serviceRoutine.syncRoutines(applicationContext)
            serviceDevices.syncDevices(applicationContext)
            serviceDevices.syncActivations(applicationContext)
            Log.d("SyncWorker", "Data synced successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Failed to sync themes: ${e.message}")
            Result.retry() // Retry if there was a failure
        }
    }
}