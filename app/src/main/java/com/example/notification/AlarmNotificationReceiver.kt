package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, 100)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "REBUILD Protocol Alert"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Stay focused and disciplined on your Winter Arc."

        NotificationHelper.showNotification(
            context = context,
            notificationId = alarmId,
            channelId = NotificationHelper.CHANNEL_TIMETABLE,
            title = title,
            message = message,
            priorityHigh = true
        )

        // Reschedule for next day if it's one of the recurring daily timetable alarms
        val matchingDefault = AlarmScheduler.DEFAULT_DAILY_ALARMS.find { it.id == alarmId }
        if (matchingDefault != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            AlarmScheduler.scheduleDailyAlarm(context, alarmManager, matchingDefault)
        }
    }
}
