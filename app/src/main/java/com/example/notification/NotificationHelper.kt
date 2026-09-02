package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_TIMETABLE = "rebuild_timetable_channel"
    const val CHANNEL_POMODORO = "rebuild_pomodoro_channel"
    const val CHANNEL_HABITS = "rebuild_habits_channel"
    const val CHANNEL_RINGING_ALARM = "rebuild_ringing_alarm_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ringingAlarmChannel = NotificationChannel(
                CHANNEL_RINGING_ALARM,
                "Apex Active Alarm Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen high-priority waking and mission-critical alarms"
                enableVibration(true)
                enableLights(true)
                lightColor = 0xFF38E1FF.toInt()
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            val timetableChannel = NotificationChannel(
                CHANNEL_TIMETABLE,
                "Daily Timetable & Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily school, study sessions, workouts, and wake-up notifications"
                enableVibration(true)
                enableLights(true)
                lightColor = 0xFF38E1FF.toInt() // Ice Cyan
            }

            val pomodoroChannel = NotificationChannel(
                CHANNEL_POMODORO,
                "Pomodoro Focus Engine",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Focus session completion and break alerts"
                enableVibration(true)
                enableLights(true)
                lightColor = 0xFF70B8FF.toInt()
            }

            val habitsChannel = NotificationChannel(
                CHANNEL_HABITS,
                "Habits & Discipline",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Habit check-ins and Winter Arc accountability"
            }

            notificationManager.createNotificationChannels(
                listOf(ringingAlarmChannel, timetableChannel, pomodoroChannel, habitsChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        priorityHigh: Boolean = true
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.rebuild_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (priorityHigh) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF38E1FF.toInt())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
