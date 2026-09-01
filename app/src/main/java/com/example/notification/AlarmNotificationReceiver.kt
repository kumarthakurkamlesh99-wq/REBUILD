package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CATEGORY = "extra_category"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, 100)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "REBUILD Protocol Alert"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Stay focused and disciplined on your Winter Arc."
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Protocol"

        val channelId = when (category) {
            "Study Session" -> NotificationHelper.CHANNEL_POMODORO
            "Workout", "Reflection", "Wake Up", "Sleep" -> NotificationHelper.CHANNEL_HABITS
            else -> NotificationHelper.CHANNEL_TIMETABLE
        }

        val isFullAlarm = intent.getBooleanExtra("is_full_alarm", false) || category == "Wake Up"
        val customAlarmId = intent.getLongExtra("custom_alarm_id", 0L)
        val challengeType = intent.getStringExtra(AlarmDismissActivity.EXTRA_CHALLENGE_TYPE) ?: "MATH"
        val difficulty = intent.getStringExtra(AlarmDismissActivity.EXTRA_DIFFICULTY) ?: "MEDIUM"
        val volume = intent.getIntExtra(AlarmDismissActivity.EXTRA_VOLUME, 90)
        val isVibrate = intent.getBooleanExtra(AlarmDismissActivity.EXTRA_VIBRATE, true)
        val maxSnoozes = intent.getIntExtra(AlarmDismissActivity.EXTRA_MAX_SNOOZES, 3)
        val snoozeDuration = intent.getIntExtra(AlarmDismissActivity.EXTRA_SNOOZE_DURATION, 5)

        if (isFullAlarm) {
            try {
                val dismissIntent = Intent(context, AlarmDismissActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(AlarmDismissActivity.EXTRA_ALARM_ID, customAlarmId)
                    putExtra(AlarmDismissActivity.EXTRA_ALARM_TITLE, title)
                    putExtra(AlarmDismissActivity.EXTRA_CHALLENGE_TYPE, challengeType)
                    putExtra(AlarmDismissActivity.EXTRA_DIFFICULTY, difficulty)
                    putExtra(AlarmDismissActivity.EXTRA_VOLUME, volume)
                    putExtra(AlarmDismissActivity.EXTRA_VIBRATE, isVibrate)
                    putExtra(AlarmDismissActivity.EXTRA_MAX_SNOOZES, maxSnoozes)
                    putExtra(AlarmDismissActivity.EXTRA_SNOOZE_DURATION, snoozeDuration)
                }
                context.startActivity(dismissIntent)
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Could not start AlarmDismissActivity directly", e)
            }
        }

        NotificationHelper.showNotification(
            context = context,
            notificationId = alarmId,
            channelId = channelId,
            title = title,
            message = message,
            priorityHigh = true
        )

        // Reschedule for next day using the user's profile
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context, this)
                val profile = db.userProfileDao().getUserProfileDirect()
                if (profile != null && profile.isCompleted) {
                    val matching = AlarmScheduler.getProfileAlarmsList(profile).find { it.id == alarmId }
                    if (matching != null && matching.isEnabled) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        // Re-trigger schedule for tomorrow
                        AlarmScheduler.scheduleProfileAlarms(context, profile)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to re-schedule alarm after firing", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
