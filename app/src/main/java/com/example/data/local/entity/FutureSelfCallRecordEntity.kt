package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FutureSelfCallState {
    SCHEDULED,
    RINGING,
    ANSWERED,
    DISMISSED,
    SNOOZED,
    CANCELLED,
    MISSED
}

@Entity(
    tableName = "future_self_call_records",
    indices = [
        Index(value = ["callOccurrenceId"], unique = true),
        Index(value = ["taskId"]),
        Index(value = ["state"])
    ]
)
data class FutureSelfCallRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callOccurrenceId: String,
    val taskId: Long,
    val scheduledDate: String,
    val scheduledTime: String,
    val scheduledTimestamp: Long,
    val state: FutureSelfCallState = FutureSelfCallState.SCHEDULED,
    val ringStartedAt: Long? = null,
    val handledAt: Long? = null,
    val snoozeCount: Int = 0,
    val lastAction: String = ""
)
