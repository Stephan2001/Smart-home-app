import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adr.opsc7312_poe.ServiceRoutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServiceRoutineWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val serviceRoutine = ServiceRoutine()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Retrieve RoutineId from inputData
                val RoutineId = inputData.getInt("ROUTINE_ID", -1)  // Default to -1 if not provided
                Log.d("ServiceRoutineWorker", "RoutineId: $RoutineId")
                // Check if the Id is valid
                if (RoutineId != -1) {
                    // Call the API
                    serviceRoutine.ToggleRoutineStatus(applicationContext, RoutineId)
                } else {
                    Log.e("ServiceRoutineWorker", "Invalid RoutineId")
                }

                Result.success()
            } catch (e: Exception) {
                Log.e("ServiceRoutineWorker", "Error: ${e.message}")
                Result.retry() // Retry the work in case of failure
            }
        }
    }
}
