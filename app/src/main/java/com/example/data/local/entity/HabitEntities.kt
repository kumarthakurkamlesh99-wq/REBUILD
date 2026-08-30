package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "Wake Early", "Workout", "Study", "Reading", "No Porn", "No Reels", "Meditation", "Water Intake"
    val iconName: String = "check_circle",
    val colorHex: String = "#38E1FF",
    val weight: Int = 10, // weight towards discipline score
    val isNegativeHabit: Boolean = false, // e.g. No Porn, No Reels (true = abstinence habit)
    val targetUnit: String = "Completed", // e.g. "Glasses", "Pages", "Minutes"
    val targetNumeric: Int = 1,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val isDefault: Boolean = true,
    val isArchived: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["habitId"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = false,
    val numericValue: Int = 0,
    val note: String = ""
)
