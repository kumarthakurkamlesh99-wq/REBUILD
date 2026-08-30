package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class DailyPlanWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val scope = CoroutineScope(Dispatchers.IO)
            val db = AppDatabase.getDatabase(applicationContext, scope)
            val repository = RebuildRepository(db)
            val today = repository.getTodayDateString()

            // Rollover missed tasks to today
            repository.generateSmartDailyPlan(today)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
