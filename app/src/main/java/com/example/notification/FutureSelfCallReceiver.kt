package com.example.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.FutureSelfSettingsRepository
import com.example.speech.FutureSelfMessageEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FutureSelfCallReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_FUTURE_SELF_CALL = "com.example.rebuild.ACTION_FUTURE_SELF_CALL"
        const val ACTION_DISMISS_CALL = "com.example.rebuild.ACTION_DISMISS_CALL"
        const val TAG = "FutureSelfCallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction = intent.action
        val taskId = intent.getLongExtra(FutureSelfCallActivity.EXTRA_TASK_ID, 0L)
        var occurrenceId = intent.getStringExtra(FutureSelfCallEngine.EXTRA_CALL_OCCURRENCE_ID) ?: ""

        if (receivedAction == ACTION_DISMISS_CALL) {
            Log.d(TAG, "ACTION_DISMISS_CALL received for task $taskId (occurrence=$occurrenceId)")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FutureSelfCallEngine.markDismissed(
                        context = context,
                        occurrenceId = occurrenceId,
                        taskId = taskId,
                        reason = "NOTIFICATION_DISMISSED"
                    )
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "rebuild:FutureSelfCallWakeLock"
        )
        wakeLock?.acquire(10_000L) // Safe 10 second timeout

        val taskTitle = intent.getStringExtra(FutureSelfCallActivity.EXTRA_TASK_TITLE) ?: "Deep Focus Protocol"
        val taskSubject = intent.getStringExtra(FutureSelfCallActivity.EXTRA_TASK_SUBJECT) ?: "Physics"
        val startTime = intent.getStringExtra(FutureSelfCallActivity.EXTRA_START_TIME) ?: "05:00 AM"
        val endTime = intent.getStringExtra(FutureSelfCallActivity.EXTRA_END_TIME) ?: "06:30 AM"
        val durationMinutes = intent.getIntExtra(FutureSelfCallActivity.EXTRA_DURATION_MINUTES, 90)
        val xpReward = intent.getIntExtra(FutureSelfCallActivity.EXTRA_XP_REWARD, 150)

        val settingsRepo = FutureSelfSettingsRepository.getInstance(context)
        val settings = settingsRepo.getSettingsSync()

        if (!settings.isEnabled) {
            // Future self call disabled, emit standard task notification
            NotificationHelper.showNotification(
                context = context,
                notificationId = (taskId % 10000).toInt() + 1000,
                channelId = NotificationHelper.CHANNEL_POMODORO,
                title = "Task Reminder • $taskSubject",
                message = "$taskTitle ($startTime - $endTime)",
                priorityHigh = true
            )
            try { wakeLock?.release() } catch (_: Exception) {}
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context, this)
                val profile = db.userProfileDao().getUserProfileDirect()
                val studentName = profile?.name?.ifBlank { "Rudra" } ?: "Rudra"

                val task = if (taskId > 0L) db.dailyPlanDao().getTaskById(taskId) else null

                // If occurrenceId was not passed in intent, generate deterministically
                if (occurrenceId.isBlank()) {
                    val dateStr = task?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
                    val hour = task?.reminderHour ?: 5
                    val minute = task?.reminderMinute ?: 0
                    occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, dateStr, hour, minute)
                }

                // IDEMPOTENCY GATE: Ensure call is not duplicate and hasn't been answered/dismissed/completed
                val canProceed = FutureSelfCallEngine.canInitiateCall(
                    context = context,
                    occurrenceId = occurrenceId,
                    taskId = taskId,
                    scheduledDate = task?.date ?: "",
                    scheduledTime = startTime
                )

                if (!canProceed) {
                    Log.i(TAG, "Idempotency check failed: Call occurrence $occurrenceId cannot be initiated. Dropping event.")
                    return@launch
                }

                // Pre-synthesize cloned voice speech in background during call alert dispatch
                if (settings.isVoiceCloningEnabled && settings.voiceCloneId.isNotBlank()) {
                    val preText = FutureSelfMessageEngine.buildIgnitionSpeechText(
                        subject = taskSubject,
                        title = taskTitle,
                        durationMinutes = durationMinutes,
                        language = settings.language
                    )
                    com.example.speech.VoiceCloningService.getInstance(context).preSynthesize(
                        text = preText,
                        voiceId = settings.voiceCloneId,
                        apiKey = settings.elevenLabsApiKey,
                        stability = settings.voiceCloneStability,
                        similarity = settings.voiceCloneSimilarity
                    )
                }

                val currentDelayMinutes = task?.delayMinutes ?: 0
                val isMaxDelay = currentDelayMinutes >= settings.maxAllowedDelayMinutes

                // Intent to start full-screen incoming call activity
                val callIntent = Intent(context, FutureSelfCallActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_ID, taskId)
                    putExtra(FutureSelfCallEngine.EXTRA_CALL_OCCURRENCE_ID, occurrenceId)
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_TITLE, taskTitle)
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_SUBJECT, taskSubject)
                    putExtra(FutureSelfCallActivity.EXTRA_START_TIME, startTime)
                    putExtra(FutureSelfCallActivity.EXTRA_END_TIME, endTime)
                    putExtra(FutureSelfCallActivity.EXTRA_DURATION_MINUTES, durationMinutes)
                    putExtra(FutureSelfCallActivity.EXTRA_XP_REWARD, xpReward)
                    putExtra(FutureSelfCallActivity.EXTRA_STUDENT_NAME, studentName)
                    putExtra(FutureSelfCallActivity.EXTRA_CUSTOM_QUOTE, settings.customFutureSelfQuote)
                    putExtra(FutureSelfCallActivity.EXTRA_DELAY_MINUTES, currentDelayMinutes)
                    putExtra(FutureSelfCallActivity.EXTRA_IS_MAX_DELAY, isMaxDelay)
                }

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    (taskId % 10000).toInt() + 50000,
                    callIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val dismissIntent = Intent(context, FutureSelfCallReceiver::class.java).apply {
                    action = ACTION_DISMISS_CALL
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_ID, taskId)
                    putExtra(FutureSelfCallEngine.EXTRA_CALL_OCCURRENCE_ID, occurrenceId)
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (taskId % 10000).toInt() + 60000,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // High priority notification with FullScreenIntent
                val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_FUTURE_SELF_CALL)
                    .setSmallIcon(R.drawable.rebuild_logo)
                    .setContentTitle("Incoming Transmission • $taskSubject")
                    .setContentText("$taskTitle • Future $studentName is Calling...")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setContentIntent(fullScreenPendingIntent)
                    .setDeleteIntent(dismissPendingIntent)
                    .addAction(R.drawable.rebuild_logo, "Answer", fullScreenPendingIntent)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", dismissPendingIntent)
                    .setAutoCancel(true)
                    .setColor(0xFF00F0FF.toInt())
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notifId = FutureSelfCallEngine.getCallNotificationId(taskId)
                notificationManager.notify(notifId, notification)

                // Also launch directly if in foreground / screen is on
                try {
                    context.startActivity(callIntent)
                } catch (e: Exception) {
                    Log.w(TAG, "Direct activity launch deferred to fullScreenIntent: ${e.message}")
                }

                // Advance dynamic rolling window to schedule next upcoming task alarm
                // Since this occurrence is now marked RINGING, it will NOT be rescheduled
                try {
                    AlarmScheduler.rescheduleUpcomingTaskAlarms(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to advance rolling alarm window", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed in FutureSelfCallReceiver async execution", e)
            } finally {
                try {
                    if (wakeLock?.isHeld == true) {
                        wakeLock.release()
                    }
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }
}
