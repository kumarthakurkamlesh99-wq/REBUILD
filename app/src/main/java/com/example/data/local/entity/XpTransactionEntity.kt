package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xp_transactions")
data class XpTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Study", "Workout", "Discipline", "Habit", "School", "Revision"
    val xp: Int,
    val timestamp: Long = System.currentTimeMillis()
)
