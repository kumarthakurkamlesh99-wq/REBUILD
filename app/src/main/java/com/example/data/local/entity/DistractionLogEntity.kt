package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "distraction_logs",
    indices = [
        Index(value = ["date"]),
        Index(value = ["sessionId"])
    ]
)
data class DistractionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long = 0L,
    val attemptedAppName: String,
    val appPackageName: String = "",
    val reasonOrUrge: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String // "yyyy-MM-dd"
)
