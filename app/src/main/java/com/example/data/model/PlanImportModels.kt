package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanImport(
    @Json(name = "plan_name") val planName: String? = null,
    val version: Int? = 1,
    @Json(name = "exam_date") val examDate: String? = null,
    val schedule: List<PlanScheduleItem>? = emptyList(),
    val goals: List<PlanGoal>? = emptyList(),
    val tasks: List<PlanTask>? = emptyList(),
    val habits: List<PlanHabit>? = emptyList(),
    val alarms: List<PlanAlarm>? = emptyList(),
    @Json(name = "focus_sessions") val focusSessions: List<PlanFocusSession>? = emptyList(),
    val milestones: List<PlanMilestone>? = emptyList(),
    @Json(name = "xp_rules") val xpRules: PlanXpRules? = null
)

@JsonClass(generateAdapter = true)
data class PlanScheduleItem(
    val time: String, // HH:mm or e.g. "04:00"
    val title: String, // e.g. "Running", "School", "Physics"
    val category: String? = "Study", // "Workout", "School", "Study", "Revision", "Routine"
    @Json(name = "target_minutes") val targetMinutes: Int? = 45,
    val type: String? = "LECTURE",
    val details: String? = ""
)

@JsonClass(generateAdapter = true)
data class PlanGoal(
    val title: String,
    val description: String? = "",
    val category: String? = "ACADEMIC", // ACADEMIC, FITNESS, PERSONAL, DISCIPLINE
    @Json(name = "target_date") val targetDate: String? = null // yyyy-MM-dd
)

@JsonClass(generateAdapter = true)
data class PlanTask(
    val title: String,
    val subject: String? = "General",
    val type: String? = "LECTURE",
    val details: String? = "",
    @Json(name = "target_minutes") val targetMinutes: Int? = 45,
    val date: String? = null, // yyyy-MM-dd
    val xp: Int? = 0,
    val time: String? = null // HH:mm or "15:00"
)

@JsonClass(generateAdapter = true)
data class PlanHabit(
    val name: String,
    @Json(name = "habit_type") val habitType: String? = "CUSTOM",
    @Json(name = "is_negative") val isNegative: Boolean? = false,
    @Json(name = "target_unit") val targetUnit: String? = "Completed",
    @Json(name = "target_numeric") val targetNumeric: Int? = 1
)

@JsonClass(generateAdapter = true)
data class PlanAlarm(
    val title: String,
    val time: String, // HH:mm
    @Json(name = "challenge_type") val challengeType: String? = "MATH",
    @Json(name = "difficulty") val difficulty: String? = "MEDIUM"
)

@JsonClass(generateAdapter = true)
data class PlanFocusSession(
    val title: String,
    val duration: Int? = 25
)

@JsonClass(generateAdapter = true)
data class PlanMilestone(
    val title: String,
    val date: String? = null // yyyy-MM-dd
)

@JsonClass(generateAdapter = true)
data class PlanXpRules(
    @Json(name = "task_xp") val taskXp: Int? = 50,
    @Json(name = "habit_xp") val habitXp: Int? = 10,
    @Json(name = "focus_xp") val focusXp: Int? = 20
)

data class PlanValidationError(
    val itemType: String, // e.g. "Habit", "Task", "Goal", "Alarm"
    val index: Int, // 1-based index
    val field: String, // e.g. "Name", "Title", "Time"
    val summaryBullet: String, // e.g. "Habit #2 is missing Name"
    val friendlyMessage: String, // Full friendly message
    val technicalDetails: String // Developer json path and info
)

data class PlanValidationResult(
    val isValid: Boolean,
    val plan: PlanImport?,
    val errors: List<PlanValidationError> = emptyList(),
    val technicalSummary: String = ""
)

data class ImportReport(
    val planName: String,
    val goalsCount: Int,
    val tasksCount: Int,
    val scheduleCount: Int = 0,
    val habitsCount: Int,
    val alarmsCount: Int
)
