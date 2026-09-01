package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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

    const val ID_WAKE_UP = 101
    const val ID_SCHOOL_DEPARTURE = 102
    const val ID_SCHOOL_ARRIVAL = 103
    const val ID_RETURN_HOME = 104
    const val ID_STUDY_SESSION = 105
    const val ID_WORKOUT = 106
    const val ID_REVISION = 107
    const val ID_REFLECTION = 108
    const val ID_SLEEP = 109

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
            Log.d("AlarmScheduler", "Scheduled custom alarm ${alarm.id} (${alarm.title}) for ${alarm.hour}:${alarm.minute}")
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
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

    fun getProfileAlarmsList(profile: UserProfileEntity): List<ScheduledAlarmInfo> {
        val (wakeH, wakeM) = parseTime(profile.wakeUpTime, 6, 0)
        val (sleepH, sleepM) = parseTime(profile.sleepTime, 22, 30)
        val (schoolStartH, schoolStartM) = parseTime(profile.schoolStartTime, 9, 45)
        val (schoolEndH, schoolEndM) = parseTime(profile.schoolEndTime, 13, 0)
        val (workoutH, workoutM) = parseTime(profile.workoutTime, 17, 0)

        // Calculate departure: schoolStart minus travel time
        var depMinutes = schoolStartH * 60 + schoolStartM - profile.travelTimeMinutes
        if (depMinutes < 0) depMinutes += 24 * 60
        val depH = depMinutes / 60
        val depM = depMinutes % 60

        // Study session 1: 1 hour after return home or 2 hours after wake up if no school
        val (studyH, studyM) = if (profile.hasSchool) {
            var sMin = schoolEndH * 60 + schoolEndM + 60
            if (sMin >= 24 * 60) sMin %= 24 * 60
            Pair(sMin / 60, sMin % 60)
        } else {
            Pair((wakeH + 2) % 24, wakeM)
        }

        // Revision session: 1 hour before sleep
        var revMin = sleepH * 60 + sleepM - 90
        if (revMin < 0) revMin += 24 * 60
        val revH = revMin / 60
        val revM = revMin % 60

        // Reflection session: 30 min before sleep
        var refMin = sleepH * 60 + sleepM - 30
        if (refMin < 0) refMin += 24 * 60
        val refH = refMin / 60
        val refM = refMin % 60

        val list = mutableListOf<ScheduledAlarmInfo>()

        list.add(
            ScheduledAlarmInfo(
                id = ID_WAKE_UP,
                hour = wakeH,
                minute = wakeM,
                title = "Wake Up • Day Underway",
                message = "${profile.name}, Winter Arc discipline starts now. Stand up, hydrate (500ml cold water) and claim the morning.",
                category = "Wake Up",
                isEnabled = profile.notifyWakeUp
            )
        )

        if (profile.hasSchool) {
            list.add(
                ScheduledAlarmInfo(
                    id = ID_SCHOOL_DEPARTURE,
                    hour = depH,
                    minute = depM,
                    title = "School Dispatch Reminder",
                    message = "Time to depart for school. Carry formulas and maintain active recall on the way.",
                    category = "School Departure",
                    isEnabled = profile.notifySchoolDeparture
                )
            )
            list.add(
                ScheduledAlarmInfo(
                    id = ID_SCHOOL_ARRIVAL,
                    hour = schoolStartH,
                    minute = schoolStartM,
                    title = "Arrived at School",
                    message = "School session started. Tap [ARRIVED SCHOOL] to track attendance and travel analytics.",
                    category = "School Arrival",
                    isEnabled = profile.notifySchoolArrival
                )
            )
            list.add(
                ScheduledAlarmInfo(
                    id = ID_RETURN_HOME,
                    hour = schoolEndH,
                    minute = schoolEndM,
                    title = "School Dispersal & Return",
                    message = "Commute back home. Tap [ARRIVED HOME] to automatically generate your smart daily study plan.",
                    category = "Return Home",
                    isEnabled = profile.notifyReturnHome
                )
            )
        }

        list.add(
            ScheduledAlarmInfo(
                id = ID_STUDY_SESSION,
                hour = studyH,
                minute = studyM,
                title = "Deep Study Block • Focus Time",
                message = "Deep work window active. Put phone in Monk Mode and begin ${profile.preferredSessionDurationMinutes}-min Pomodoro.",
                category = "Study Session",
                isEnabled = profile.notifyStudySessions
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = ID_WORKOUT,
                hour = workoutH,
                minute = workoutM,
                title = "Workout Engine • ${profile.workoutType}",
                message = "${profile.workoutDurationMinutes} minutes of physical power. Release endorphins and build mental grit.",
                category = "Workout",
                isEnabled = profile.notifyWorkout
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = ID_REVISION,
                hour = revH,
                minute = revM,
                title = "Spaced Repetition & Revision",
                message = "Active recall & formula flashcards sweep across core subjects.",
                category = "Revision",
                isEnabled = profile.notifyRevision
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = ID_REFLECTION,
                hour = refH,
                minute = refM,
                title = "Daily Score & Reflection",
                message = "Audit today's wins, discipline matrix, and log your thoughts in REBUILD Notes.",
                category = "Reflection",
                isEnabled = profile.notifyReflection
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = ID_SLEEP,
                hour = sleepH,
                minute = sleepM,
                title = "Sleep & Recovery Protocol",
                message = "Zero screens. Pitch black room. 7.5 hours rest for brain memory consolidation.",
                category = "Sleep",
                isEnabled = profile.notifySleep
            )
        )

        return list
    }

    fun scheduleProfileAlarms(context: Context, profile: UserProfileEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarms = getProfileAlarmsList(profile)

        for (alarm in alarms) {
            if (alarm.isEnabled) {
                scheduleAlarmInternal(context, alarmManager, alarm)
            } else {
                cancelAlarm(context, alarm.id)
            }
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

    fun scheduleAllDefaultAlarms(context: Context) {
        scheduleProfileAlarms(context, UserProfileEntity())
    }

    fun cancelAllAlarms(context: Context) {
        val alarms = getProfileAlarmsList(UserProfileEntity())
        alarms.forEach { cancelAlarm(context, it.id) }
    }
}
