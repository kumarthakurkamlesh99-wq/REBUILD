package com.example.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FutureSelfCallRecordEntity
import com.example.data.local.entity.FutureSelfCallState
import com.example.speech.FutureSelfSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object FutureSelfCallEngine {

    private const val TAG = "FutureSelfCallEngine"
    const val CALL_NOTIFICATION_ID_BASE = 20000
    const val EXTRA_CALL_OCCURRENCE_ID = "extra_call_occurrence_id"

    fun getCallNotificationId(taskId: Long): Int {
        return CALL_NOTIFICATION_ID_BASE + (taskId % 10000).toInt()
    }

    fun generateOccurrenceId(taskId: Long, date: String, hour: Int, minute: Int): String {
        return "task_${taskId}_${date}_${String.format(Locale.US, "%02d_%02d", hour, minute)}"
    }

    fun generateSnoozeOccurrenceId(taskId: Long, timestamp: Long): String {
        return "task_${taskId}_snooze_${timestamp}"
    }

    /**
     * Atomically validates if a call occurrence can be initiated.
     * Transitions SCHEDULED -> RINGING in DB.
     * Returns true if call should proceed, false if it is a duplicate / already handled / completed.
     */
    suspend fun canInitiateCall(
        context: Context,
        occurrenceId: String,
        taskId: Long,
        scheduledDate: String = "",
        scheduledTime: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
            val dao = db.futureSelfCallDao()

            // 1. If task is already completed, do not initiate call
            if (taskId > 0L) {
                val task = db.dailyPlanDao().getTaskById(taskId)
                if (task != null && task.isCompleted) {
                    Log.i(TAG, "Task $taskId is already completed. Cancelling call trigger $occurrenceId.")
                    dao.cancelPendingCallsForTask(taskId)
                    return@withContext false
                }
            }

            val existing = dao.getRecordByOccurrenceId(occurrenceId)
            if (existing != null) {
                when (existing.state) {
                    FutureSelfCallState.ANSWERED,
                    FutureSelfCallState.DISMISSED,
                    FutureSelfCallState.CANCELLED,
                    FutureSelfCallState.MISSED,
                    FutureSelfCallState.SNOOZED -> {
                        Log.i(TAG, "Occurrence $occurrenceId already ${existing.state}. Dropping duplicate trigger.")
                        return@withContext false
                    }
                    FutureSelfCallState.RINGING -> {
                        // Already actively ringing! Prevent duplicate ring / duplicate activity
                        val elapsed = System.currentTimeMillis() - (existing.ringStartedAt ?: 0L)
                        if (elapsed < 60_000L) {
                            Log.i(TAG, "Occurrence $occurrenceId is already ringing ($elapsed ms ago). Dropping duplicate.")
                            return@withContext false
                        }
                    }
                    FutureSelfCallState.SCHEDULED -> {
                        // Valid to transition to RINGING
                    }
                }
            }

            val now = System.currentTimeMillis()
            val recordToSave = (existing ?: FutureSelfCallRecordEntity(
                callOccurrenceId = occurrenceId,
                taskId = taskId,
                scheduledDate = scheduledDate.ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
                },
                scheduledTime = scheduledTime.ifBlank { "00:00" },
                scheduledTimestamp = now
            )).copy(
                state = FutureSelfCallState.RINGING,
                ringStartedAt = now
            )

            dao.insertOrUpdate(recordToSave)
            Log.d(TAG, "Occurrence $occurrenceId successfully transitioned to RINGING.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in canInitiateCall for $occurrenceId", e)
            true // Fail-open gracefully if DB error
        }
    }

    /**
     * Marks call as answered.
     * Stops ringtone and vibration.
     * Does NOT mark task as completed.
     */
    suspend fun markAnswered(context: Context, occurrenceId: String, taskId: Long) = withContext(Dispatchers.IO) {
        try {
            // Stop sound and vibration
            val speechManager = FutureSelfSpeechManager.getInstance(context)
            speechManager.stopRingtoneAndVibrate()

            // Dismiss notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (taskId > 0L) {
                notificationManager?.cancel(getCallNotificationId(taskId))
            }

            // Update database state
            if (occurrenceId.isNotBlank()) {
                val db = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
                db.futureSelfCallDao().updateCallState(
                    occurrenceId = occurrenceId,
                    newState = FutureSelfCallState.ANSWERED,
                    handledAt = System.currentTimeMillis(),
                    action = "ANSWERED"
                )
                Log.d(TAG, "Occurrence $occurrenceId marked as ANSWERED (task $taskId remains pending).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking call answered for $occurrenceId", e)
        }
    }

    /**
     * Marks call as dismissed.
     * Stops ringtone, vibration, and speech.
     * Does NOT mark task as completed.
     */
    suspend fun markDismissed(
        context: Context,
        occurrenceId: String,
        taskId: Long,
        reason: String = "USER_DISMISSED"
    ) = withContext(Dispatchers.IO) {
        try {
            // Stop sound and vibration and speech
            val speechManager = FutureSelfSpeechManager.getInstance(context)
            speechManager.stopRingtoneAndVibrate()
            speechManager.stopSpeaking()

            // Dismiss notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (taskId > 0L) {
                notificationManager?.cancel(getCallNotificationId(taskId))
            }

            // Update database state
            if (occurrenceId.isNotBlank()) {
                val db = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
                db.futureSelfCallDao().updateCallState(
                    occurrenceId = occurrenceId,
                    newState = FutureSelfCallState.DISMISSED,
                    handledAt = System.currentTimeMillis(),
                    action = reason
                )
                Log.d(TAG, "Occurrence $occurrenceId marked as DISMISSED ($reason).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking call dismissed for $occurrenceId", e)
        }
    }

    /**
     * Snoozes call:
     * - Marks current occurrence as SNOOZED.
     * - Creates exactly ONE new scheduled occurrence at (now + snoozeMinutes).
     * - Schedules the new alarm in AlarmManager.
     */
    suspend fun snoozeCall(
        context: Context,
        occurrenceId: String,
        taskId: Long,
        snoozeMinutes: Int,
        taskTitle: String = "Study Protocol",
        taskSubject: String = "General",
        durationMinutes: Int = 90,
        xpReward: Int = 150
    ) = withContext(Dispatchers.IO) {
        try {
            // 1. Stop audio & vibration
            val speechManager = FutureSelfSpeechManager.getInstance(context)
            speechManager.stopRingtoneAndVibrate()
            speechManager.stopSpeaking()

            // 2. Dismiss existing notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (taskId > 0L) {
                notificationManager?.cancel(getCallNotificationId(taskId))
            }

            val db = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
            val dao = db.futureSelfCallDao()

            val existing = if (occurrenceId.isNotBlank()) dao.getRecordByOccurrenceId(occurrenceId) else null
            val currentSnoozeCount = (existing?.snoozeCount ?: 0) + 1

            // 3. Mark old occurrence as SNOOZED
            if (occurrenceId.isNotBlank()) {
                dao.updateCallState(
                    occurrenceId = occurrenceId,
                    newState = FutureSelfCallState.SNOOZED,
                    handledAt = System.currentTimeMillis(),
                    action = "SNOOZED_${snoozeMinutes}MIN"
                )
            }

            // 4. Create new occurrence for snooze interval
            val snoozeTimestamp = System.currentTimeMillis() + (snoozeMinutes * 60_000L)
            val snoozeCal = Calendar.getInstance().apply { timeInMillis = snoozeTimestamp }
            val newDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(snoozeCal.time)
            val newTimeStr = SimpleDateFormat("HH:mm", Locale.US).format(snoozeCal.time)
            val newOccurrenceId = generateSnoozeOccurrenceId(taskId, snoozeTimestamp)

            val newRecord = FutureSelfCallRecordEntity(
                callOccurrenceId = newOccurrenceId,
                taskId = taskId,
                scheduledDate = newDateStr,
                scheduledTime = newTimeStr,
                scheduledTimestamp = snoozeTimestamp,
                state = FutureSelfCallState.SCHEDULED,
                snoozeCount = currentSnoozeCount,
                lastAction = "SCHEDULED_VIA_SNOOZE"
            )
            dao.insertOrUpdate(newRecord)

            // 5. Schedule exactly ONE new alarm in AlarmManager
            scheduleSingleCallAlarm(
                context = context,
                taskId = taskId,
                occurrenceId = newOccurrenceId,
                triggerTimeMillis = snoozeTimestamp,
                taskTitle = taskTitle,
                taskSubject = taskSubject,
                durationMinutes = durationMinutes,
                xpReward = xpReward
            )

            Log.d(TAG, "Scheduled exactly ONE snoozed call ($newOccurrenceId) for +$snoozeMinutes mins ($newTimeStr).")
        } catch (e: Exception) {
            Log.e(TAG, "Error snoozing call $occurrenceId", e)
        }
    }

    /**
     * Helper to schedule an alarm in AlarmManager for a specific occurrence
     */
    fun scheduleSingleCallAlarm(
        context: Context,
        taskId: Long,
        occurrenceId: String,
        triggerTimeMillis: Long,
        taskTitle: String,
        taskSubject: String,
        durationMinutes: Int,
        xpReward: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val alarmId = (AlarmScheduler.TASK_ALARM_ID_BASE + (taskId % 9999)).toInt()

        val intent = Intent(context, FutureSelfCallReceiver::class.java).apply {
            action = FutureSelfCallReceiver.ACTION_FUTURE_SELF_CALL
            putExtra(FutureSelfCallActivity.EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_CALL_OCCURRENCE_ID, occurrenceId)
            putExtra(FutureSelfCallActivity.EXTRA_TASK_TITLE, taskTitle)
            putExtra(FutureSelfCallActivity.EXTRA_TASK_SUBJECT, taskSubject)
            putExtra(FutureSelfCallActivity.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(FutureSelfCallActivity.EXTRA_XP_REWARD, xpReward)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarm registered for $occurrenceId at $triggerTimeMillis (alarmId=$alarmId)")
        } catch (e: Exception) {
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm for $occurrenceId", ex)
            }
        }
    }

    /**
     * Handles task completion:
     * - Cancels all pending/scheduled call occurrences for this task.
     * - Cancels AlarmManager alarms.
     * - Dismisses notification if present.
     */
    suspend fun onTaskCompleted(context: Context, taskId: Long) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
            db.futureSelfCallDao().cancelPendingCallsForTask(taskId)

            AlarmScheduler.cancelTaskAlarm(context, taskId)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(getCallNotificationId(taskId))

            Log.d(TAG, "All pending call triggers cancelled for completed task $taskId.")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling task completion for task $taskId", e)
        }
    }
}
