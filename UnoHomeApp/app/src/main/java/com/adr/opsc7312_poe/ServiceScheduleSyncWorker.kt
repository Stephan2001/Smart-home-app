package com.adr.opsc7312_poe

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class ServiceScheduleSyncWorker(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    init {
        scheduleSyncWork()
    }

    private fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<ServiceSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncWorkRequest)
        Log.d("scheduleSyncWork", "Worker scheduled")
    }
}