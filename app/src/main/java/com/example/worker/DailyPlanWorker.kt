package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.RebuildRepository
import com.example.notification.AlarmScheduler
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

            // 1. Rollover incomplete missed tasks to today
            repository.generateSmartDailyPlan(today)

            // 2. Dynamically schedule upcoming alarms for today & tomorrow (rolling window)
            AlarmScheduler.rescheduleUpcomingTaskAlarms(applicationContext)

            Log.d("DailyPlanWorker", "DailyPlanWorker executed successfully for $today")
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyPlanWorker", "DailyPlanWorker execution failed", e)
            Result.retry()
        }
    }
}
