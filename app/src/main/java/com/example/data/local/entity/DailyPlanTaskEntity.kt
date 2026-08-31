package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskType {
    LECTURE,
    NOTES,
    REVISION,
    PYQ,
    WORKOUT,
    CUSTOM
}

@Entity(tableName = "daily_plan_tasks")
data class DailyPlanTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val subject: String, // "Physics", "Chemistry", "Biology", "English", "Hindi", "Workout", "General"
    val title: String, // e.g. "Nuclei Lecture", "P Block Revision", "Pushups 3 x 15"
    val type: TaskType = TaskType.LECTURE,
    val details: String = "",
    val targetMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val orderIndex: Int = 0,
    val movedFromDate: String? = null,
    val xpReward: Int = 50,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
)
