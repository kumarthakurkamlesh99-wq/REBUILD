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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FutureSelfCallReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_FUTURE_SELF_CALL = "com.example.rebuild.ACTION_FUTURE_SELF_CALL"
        const val TAG = "FutureSelfCallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "rebuild:FutureSelfCallWakeLock"
        )
        wakeLock?.acquire(10_000L) // Safe 10 second timeout

        val taskId = intent.getLongExtra(FutureSelfCallActivity.EXTRA_TASK_ID, 0L)
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

                // Intent to start full-screen incoming call activity
                val callIntent = Intent(context, FutureSelfCallActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_ID, taskId)
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_TITLE, taskTitle)
                    putExtra(FutureSelfCallActivity.EXTRA_TASK_SUBJECT, taskSubject)
                    putExtra(FutureSelfCallActivity.EXTRA_START_TIME, startTime)
                    putExtra(FutureSelfCallActivity.EXTRA_END_TIME, endTime)
                    putExtra(FutureSelfCallActivity.EXTRA_DURATION_MINUTES, durationMinutes)
                    putExtra(FutureSelfCallActivity.EXTRA_XP_REWARD, xpReward)
                    putExtra(FutureSelfCallActivity.EXTRA_STUDENT_NAME, studentName)
                    putExtra(FutureSelfCallActivity.EXTRA_CUSTOM_QUOTE, settings.customFutureSelfQuote)
                }

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    (taskId % 10000).toInt() + 50000,
                    callIntent,
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
                    .setAutoCancel(true)
                    .setColor(0xFF00F0FF.toInt())
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify((taskId % 10000).toInt() + 20000, notification)

                // Also launch directly if in foreground / screen is on
                try {
                    context.startActivity(callIntent)
                } catch (e: Exception) {
                    Log.w(TAG, "Direct activity launch deferred to fullScreenIntent: ${e.message}")
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
