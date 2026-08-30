package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

data class ScheduledAlarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val title: String,
    val message: String
)

object AlarmScheduler {

    val DEFAULT_DAILY_ALARMS = listOf(
        ScheduledAlarm(101, 6, 0, "Wake Up • Day Underway", "Winter Arc discipline starts now. Stand up, hydrate and prepare for the day."),
        ScheduledAlarm(102, 9, 45, "Leave For School", "Dispatch for school. Review today's key physics formulas on the way."),
        ScheduledAlarm(103, 13, 0, "Arrive Home • Smart Plan Ready", "Tap ARRIVED HOME to generate today's study & workout timetable."),
        ScheduledAlarm(104, 14, 0, "Physics Session • Deep Work", "Nuclei & Modern Physics study block starting. Minimum 45 min focus."),
        ScheduledAlarm(105, 17, 0, "Workout Time • Physical Power", "Running 20 min + Pushups & Squats. Forge physical discipline."),
        ScheduledAlarm(106, 20, 0, "Revision Block", "Chemistry P-Block and Biology active recall revision session."),
        ScheduledAlarm(107, 22, 30, "Sleep & Recovery", "Wind down. Zero screens. 7.5 hours rest for cellular and neural recovery.")
    )

    fun scheduleAllDefaultAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (alarm in DEFAULT_DAILY_ALARMS) {
            scheduleDailyAlarm(context, alarmManager, alarm)
        }
    }

    fun scheduleDailyAlarm(context: Context, alarmManager: AlarmManager, alarm: ScheduledAlarm) {
        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra(AlarmNotificationReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmNotificationReceiver.EXTRA_TITLE, alarm.title)
            putExtra(AlarmNotificationReceiver.EXTRA_MESSAGE, alarm.message)
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
        } catch (e: SecurityException) {
            // In case exact alarm permission is restricted on specific OEM
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleCustomAlarm(context: Context, id: Int, timeMs: Long, title: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra(AlarmNotificationReceiver.EXTRA_ALARM_ID, id)
            putExtra(AlarmNotificationReceiver.EXTRA_TITLE, title)
            putExtra(AlarmNotificationReceiver.EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
        }
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
}
