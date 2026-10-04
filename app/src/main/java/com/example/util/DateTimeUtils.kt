package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    /**
     * Converts any 24h ("06:00", "13:45", "09:45", "22:30") or 12h string ("9:00 AM")
     * into a consistent 12-hour formatted string: "6:00 AM", "1:45 PM", etc.
     */
    fun formatTo12Hour(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return ""
        val clean = timeStr.trim()
        if (clean.contains("AM", ignoreCase = true) || clean.contains("PM", ignoreCase = true)) {
            return clean
        }
        val parts = clean.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].trim().toIntOrNull() ?: return clean
            val minute = parts[1].trim().take(2).toIntOrNull() ?: return clean
            return formatHourMinuteTo12Hour(hour, minute)
        }
        return clean
    }

    /**
     * Formats integer hour (0-23) and minute (0-59) to 12-hour string with AM/PM.
     */
    fun formatHourMinuteTo12Hour(hour: Int, minute: Int): String {
        val ampm = if (hour >= 12) "PM" else "AM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        return String.format(Locale.US, "%d:%02d %s", h, minute, ampm)
    }

    /**
     * Parses a time string (24h or 12h) to a Pair of (hourOfDay, minute).
     */
    fun parseHourMinute(timeStr: String?): Pair<Int, Int>? {
        if (timeStr.isNullOrBlank()) return null
        val clean = timeStr.trim()

        val isPm = clean.contains("PM", ignoreCase = true)
        val isAm = clean.contains("AM", ignoreCase = true)

        val stripped = clean.replace("(?i)am|pm".toRegex(), "").trim()
        val parts = stripped.split(":")
        if (parts.size >= 2) {
            var hour = parts[0].trim().toIntOrNull() ?: return null
            val minute = parts[1].trim().take(2).toIntOrNull() ?: return null

            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        }
        return null
    }

    /**
     * Calculates the end time based on a start time string and duration in minutes.
     * Returns a 12-hour formatted string like "10:30 AM".
     */
    fun calculateEndTime(startTimeStr: String?, durationMinutes: Int, delayMins: Int = 0): String? {
        val parsed = parseHourMinute(startTimeStr) ?: return null
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parsed.first)
            set(Calendar.MINUTE, parsed.second)
            add(Calendar.MINUTE, delayMins)
            add(Calendar.MINUTE, durationMinutes)
        }
        return formatHourMinuteTo12Hour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    /**
     * Formats start and end times into a clean range string:
     * e.g. "9:00 AM – 10:30 AM" or "9:00 AM – 10:30 AM (+15m delay)"
     */
    fun formatTaskTimeRange(
        startTime: String?,
        endTime: String?,
        reminderHour: Int?,
        reminderMinute: Int?,
        durationMinutes: Int,
        delayMinutes: Int = 0
    ): String {
        // 1. If explicit startTime and endTime exist
        val s12 = if (!startTime.isNullOrBlank()) formatTo12Hour(startTime) else null
        val e12 = if (!endTime.isNullOrBlank()) formatTo12Hour(endTime) else null

        if (s12 != null && e12 != null) {
            val suffix = if (delayMinutes > 0) " (+${delayMinutes}m delay)" else ""
            return "$s12 – $e12$suffix"
        }

        // 2. If startTime exists, derive endTime from duration
        if (s12 != null) {
            val endDerived = calculateEndTime(startTime, durationMinutes, delayMinutes)
            if (endDerived != null) {
                val suffix = if (delayMinutes > 0) " (+${delayMinutes}m delay)" else ""
                return "$s12 – $endDerived$suffix"
            }
            return s12
        }

        // 3. If reminderHour and reminderMinute exist
        if (reminderHour != null && reminderMinute != null) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderHour)
                set(Calendar.MINUTE, reminderMinute)
                add(Calendar.MINUTE, delayMinutes)
            }
            val startFormatted = formatHourMinuteTo12Hour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            cal.add(Calendar.MINUTE, durationMinutes)
            val endFormatted = formatHourMinuteTo12Hour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            val suffix = if (delayMinutes > 0) " (+${delayMinutes}m delay)" else ""
            return "$startFormatted – $endFormatted$suffix"
        }

        return if (durationMinutes > 0) "$durationMinutes mins" else ""
    }

    /**
     * Checks if a scheduled task is overdue (past end time or start time by 15 mins).
     */
    fun isTaskOverdue(
        taskDate: String,
        startTime: String?,
        endTime: String?,
        reminderHour: Int?,
        reminderMinute: Int?,
        durationMinutes: Int,
        delayMinutes: Int = 0
    ): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (taskDate != todayStr) {
            return taskDate < todayStr
        }

        val parsed: Pair<Int, Int> = (if (!startTime.isNullOrBlank()) parseHourMinute(startTime)
        else if (reminderHour != null && reminderMinute != null) Pair(reminderHour, reminderMinute)
        else null) ?: return false

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parsed.first)
            set(Calendar.MINUTE, parsed.second)
            add(Calendar.MINUTE, delayMinutes)
            add(Calendar.MINUTE, durationMinutes)
        }

        return Calendar.getInstance().after(cal)
    }

    fun isTaskOverdue(task: com.example.data.local.entity.DailyPlanTaskEntity): Boolean {
        return isTaskOverdue(
            taskDate = task.date,
            startTime = task.startTime,
            endTime = task.endTime,
            reminderHour = task.reminderHour,
            reminderMinute = task.reminderMinute,
            durationMinutes = task.targetMinutes,
            delayMinutes = task.delayMinutes
        )
    }
}
