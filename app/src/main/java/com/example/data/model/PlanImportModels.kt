package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanImport(
    @Json(name = "plan_name") val planName: String? = null,
    val version: Int? = 1,
    val goals: List<PlanGoal>? = emptyList(),
    val tasks: List<PlanTask>? = emptyList(),
    val habits: List<PlanHabit>? = emptyList(),
    val alarms: List<PlanAlarm>? = emptyList(),
    @Json(name = "focus_sessions") val focusSessions: List<PlanFocusSession>? = emptyList(),
    val milestones: List<PlanMilestone>? = emptyList(),
    @Json(name = "xp_rules") val xpRules: PlanXpRules? = null
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
    val date: String? = null // yyyy-MM-dd
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
