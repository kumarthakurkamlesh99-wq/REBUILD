package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val subjectTag: String = "General", // "Physics", "Chemistry", "Biology", "English", "Hindi", "Strategy", "General"
    val date: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorHex: String = "#7C8CFF"
)

@Entity(tableName = "daily_reflections")
data class DailyReflectionEntity(
    @PrimaryKey
    val date: String, // "yyyy-MM-dd"
    val dailyScore: Int = 8, // 1-10
    val whatWentWell: String = "",
    val whatHeldMeBack: String = "",
    val gratitude: String = "",
    val tomorrowGoal: String = "",
    val mood: String = "Focused", // "Focused", "Victorious", "Fatigued", "Unstoppable", "Recovering"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_plan_cache")
data class AiPlanCacheEntity(
    @PrimaryKey
    val planType: String, // "DAILY_SCHEDULE", "WEEKLY_PLAN", "REVISION_PLAN", "WORKOUT_PLAN", "RECOVERY_PLAN", "EXAM_STRATEGY", "TIME_BLOCKING", "FOCUS_SESSIONS", "PRIORITY_TASKS"
    val title: String,
    val content: String,
    val generatedDate: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)
