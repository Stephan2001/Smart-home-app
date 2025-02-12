package com.adr.opsc7312_poe

import ServiceRoutineWorker
import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

fun ServiceScheduleWorker(context: Context, targetHour: Int, targetMinute: Int, ProcessName: String, RoutineId: Int) {

    // Calculate initial delay
    val initialDelay = calculateInitialDelay(targetHour, targetMinute)

    // Create a Data object to pass userId and other parameters
    val inputData = Data.Builder()
        .putInt("ROUTINE_ID", RoutineId)
        .build()

    // Create the work request with inputData
    val workRequest = PeriodicWorkRequestBuilder<ServiceRoutineWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .setInputData(inputData)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    // Enqueue the work request with a unique name (for each unique routine)
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        ProcessName, // Unique process name
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
    Log.d("ServiceScheduleWorker", "Worker created for RoutineId: $RoutineId with unique name: $ProcessName and initial delay: $initialDelay")
}

// Cancel unique work (for each unique routine)
fun cancelUniqueRoutineWork(context: Context, uniqueName: String) {
    val workManager = WorkManager.getInstance(context)

    // Cancel the worker with the unique name
    workManager.cancelUniqueWork(uniqueName)

    // Check the worker state directlyWorkerStatus
    workManager.getWorkInfosForUniqueWork(uniqueName).get().let { workInfos ->
        if (workInfos.isNotEmpty()) {
            val workInfo = workInfos[0]
            if (workInfo.state == WorkInfo.State.CANCELLED) {
                Log.d("WorkerStatus", "Worker has been successfully canceled")
            } else {
                Log.d("WorkerStatus", "Worker state is: ${workInfo.state}")
            }
        } else {
            Log.d("WorkerStatus", "No worker found with the given unique name.")
        }
    }
}

// simple method for calculating time delay
fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
    val currentTime = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, targetHour)
        set(Calendar.MINUTE, targetMinute)
        set(Calendar.SECOND, 0)
    }

    if (targetTime.before(currentTime)) {
        targetTime.add(Calendar.DAY_OF_MONTH, 1)
    }

    return targetTime.timeInMillis - currentTime.timeInMillis
}



