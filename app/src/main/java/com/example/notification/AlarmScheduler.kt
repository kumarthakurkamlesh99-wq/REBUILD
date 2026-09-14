package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.entity.UserProfileEntity
import java.util.Calendar

data class ScheduledAlarmInfo(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val title: String,
    val message: String,
    val category: String,
    val isEnabled: Boolean
)

object AlarmScheduler {

    const val TASK_ALARM_ID_BASE = 10000
    const val GOAL_ALARM_ID_BASE = 20000
    const val CUSTOM_ALARM_ID_BASE = 30000

    fun scheduleCustomAlarm(context: Context, alarm: com.example.data.local.entity.AlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = (CUSTOM_ALARM_ID_BASE + (alarm.id % 9999)).toInt()

        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra(AlarmNotificationReceiver.EXTRA_ALARM_ID, requestCode)
            putExtra(AlarmNotificationReceiver.EXTRA_TITLE, alarm.title)
            putExtra(AlarmNotificationReceiver.EXTRA_MESSAGE, "Wake-up challenge armed: ${alarm.challengeType.name}")
            putExtra(AlarmNotificationReceiver.EXTRA_CATEGORY, "Wake Up")
            putExtra("is_full_alarm", true)
            putExtra("custom_alarm_id", alarm.id)
            putExtra(AlarmDismissActivity.EXTRA_CHALLENGE_TYPE, alarm.challengeType.name)
            putExtra(AlarmDismissActivity.EXTRA_DIFFICULTY, alarm.challengeDifficulty.name)
            putExtra(AlarmDismissActivity.EXTRA_VOLUME, alarm.volumePercent)
            putExtra(AlarmDismissActivity.EXTRA_VIBRATE, alarm.isVibrationEnabled)
            putExtra(AlarmDismissActivity.EXTRA_MAX_SNOOZES, alarm.maxSnoozes)
            putExtra(AlarmDismissActivity.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMinutes)
            putExtra(AlarmDismissActivity.EXTRA_RINGTONE_PRESET, alarm.ringtonePreset)
            putExtra(AlarmDismissActivity.EXTRA_SNOOZE_RINGTONE_PRESET, alarm.snoozeRingtonePreset)
            putExtra("is_snooze_trigger", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val triggerTime = calendar.timeInMillis

        // Show Intent for Alarm Clock info (opens dismiss activity or main screen)
        val showIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmDismissActivity.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmDismissActivity.EXTRA_ALARM_TITLE, alarm.title)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled custom alarm ${alarm.id} (${alarm.title}) for ${alarm.hour}:${alarm.minute} at $triggerTime")
        } catch (e: SecurityException) {
            Log.w("AlarmScheduler", "Exact alarm permission missing, falling back to inexact alarm", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun scheduleSnooze(
        context: Context,
        alarmId: Long,
        title: String,
        challengeType: String = "MATH",
        difficulty: String = "MEDIUM",
        volume: Int = 90,
        isVibrate: Boolean = true,
        maxSnoozes: Int = 3,
        snoozeMinutes: Int = 5,
        snoozesUsedSoFar: Int = 1,
        ringtonePreset: String = "CYBER_SIREN",
        snoozeRingtonePreset: String = "TICK_TOCK"
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = (CUSTOM_ALARM_ID_BASE + 5000 + (alarmId % 4999)).toInt()

        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra(AlarmNotificationReceiver.EXTRA_ALARM_ID, requestCode)
            putExtra(AlarmNotificationReceiver.EXTRA_TITLE, "$title (Snooze $snoozesUsedSoFar/$maxSnoozes)")
            putExtra(AlarmNotificationReceiver.EXTRA_MESSAGE, "Snooze expired. Time to wake up and conquer.")
            putExtra(AlarmNotificationReceiver.EXTRA_CATEGORY, "Wake Up")
            putExtra("is_full_alarm", true)
            putExtra("custom_alarm_id", alarmId)
            putExtra(AlarmDismissActivity.EXTRA_CHALLENGE_TYPE, challengeType)
            putExtra(AlarmDismissActivity.EXTRA_DIFFICULTY, difficulty)
            putExtra(AlarmDismissActivity.EXTRA_VOLUME, volume)
            putExtra(AlarmDismissActivity.EXTRA_VIBRATE, isVibrate)
            putExtra(AlarmDismissActivity.EXTRA_MAX_SNOOZES, maxSnoozes)
            putExtra(AlarmDismissActivity.EXTRA_SNOOZE_DURATION, snoozeMinutes)
            putExtra(AlarmDismissActivity.EXTRA_RINGTONE_PRESET, ringtonePreset)
            putExtra(AlarmDismissActivity.EXTRA_SNOOZE_RINGTONE_PRESET, snoozeRingtonePreset)
            putExtra(AlarmDismissActivity.EXTRA_CURRENT_SNOOZES, snoozesUsedSoFar)
            putExtra("is_snooze_trigger", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val showIntent = Intent(context, AlarmDismissActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled snooze alarm for $alarmId in $snoozeMinutes minutes ($snoozesUsedSoFar/$maxSnoozes)")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule snooze alarm", e)
        }
    }

    fun cancelCustomAlarm(context: Context, alarmId: Long) {
        val requestCode = (CUSTOM_ALARM_ID_BASE + (alarmId % 9999)).toInt()
        cancelAlarm(context, requestCode)
    }

    fun parseTime(timeStr: String, defaultHour: Int, defaultMin: Int): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                Pair(parts[0].trim().toInt(), parts[1].trim().toInt())
            } else {
                Pair(defaultHour, defaultMin)
            }
        } catch (e: Exception) {
            Pair(defaultHour, defaultMin)
        }
    }

    private fun scheduleAlarmInternal(context: Context, alarmManager: AlarmManager, alarm: ScheduledAlarmInfo) {
        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra(AlarmNotificationReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmNotificationReceiver.EXTRA_TITLE, alarm.title)
            putExtra(AlarmNotificationReceiver.EXTRA_MESSAGE, alarm.message)
            putExtra(AlarmNotificationReceiver.EXTRA_CATEGORY, alarm.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} (${alarm.title}) for ${alarm.hour}:${alarm.minute}")
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.w("AlarmScheduler", "Exact alarm restricted, scheduled standard alarm ${alarm.id}")
        }
    }

    fun triggerTestNotification(context: Context, alarm: ScheduledAlarmInfo) {
        NotificationHelper.showNotification(
            context = context,
            notificationId = alarm.id + 99000,
            channelId = when (alarm.category) {
                "Study Session" -> NotificationHelper.CHANNEL_POMODORO
                "Workout", "Reflection", "Wake Up", "Sleep" -> NotificationHelper.CHANNEL_HABITS
                else -> NotificationHelper.CHANNEL_TIMETABLE
            },
            title = "[TEST] ${alarm.title}",
            message = alarm.message,
            priorityHigh = true
        )
    }

    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun scheduleTaskAlarm(context: Context, taskId: Long, hour: Int, minute: Int, title: String, subject: String) {
        val alarmId = (TASK_ALARM_ID_BASE + (taskId % 9999)).toInt()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val info = ScheduledAlarmInfo(
            id = alarmId,
            hour = hour,
            minute = minute,
            title = "Task Reminder • $subject",
            message = title,
            category = "Task",
            isEnabled = true
        )
        scheduleAlarmInternal(context, alarmManager, info)
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmId = (TASK_ALARM_ID_BASE + (taskId % 9999)).toInt()
        cancelAlarm(context, alarmId)
    }

    fun scheduleGoalAlarm(context: Context, goalId: Long, hour: Int, minute: Int, title: String) {
        val alarmId = (GOAL_ALARM_ID_BASE + (goalId % 9999)).toInt()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val info = ScheduledAlarmInfo(
            id = alarmId,
            hour = hour,
            minute = minute,
            title = "Apex Goal Reminder",
            message = title,
            category = "Goal",
            isEnabled = true
        )
        scheduleAlarmInternal(context, alarmManager, info)
    }

    fun cancelGoalAlarm(context: Context, goalId: Long) {
        val alarmId = (GOAL_ALARM_ID_BASE + (goalId % 9999)).toInt()
        cancelAlarm(context, alarmId)
    }

    /**
     * Checks if the app can schedule exact alarms (Android 12+ / API 31+)
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    /**
     * Checks if POST_NOTIFICATIONS permission is granted (Android 13+ / API 33+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Opens system settings to allow user to grant SCHEDULE_EXACT_ALARM permission
     */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    Log.e("AlarmScheduler", "Failed to open exact alarm settings", ex)
                }
            }
        }
    }

    /**
     * Opens system notification settings
     */
    fun openNotificationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to open notification settings", e)
        }
    }
}
