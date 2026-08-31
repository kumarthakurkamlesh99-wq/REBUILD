package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalCategory {
    ACADEMIC,
    FITNESS,
    PERSONAL,
    DISCIPLINE
}

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: GoalCategory = GoalCategory.ACADEMIC,
    val targetDate: String? = null, // "yyyy-MM-dd", nullable for open-ended goals
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val progressPercentage: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
)
