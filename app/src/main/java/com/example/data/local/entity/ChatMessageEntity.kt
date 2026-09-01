package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AiCoachPersona(val title: String, val subtitle: String, val badge: String, val iconName: String) {
    BOARD_EXAM_COACH("Board Exam Coach", "Syllabus, High-yield Topics, Numericals & Revision", "Academic", "school"),
    WINTER_ARC_COACH("Winter Arc Commander", "Monk Mode, 90-Day Discipline & Mindset", "Discipline", "trending_up"),
    PRODUCTIVITY_MENTOR("Productivity Mentor", "Schedule Optimization, Commute & Focus", "Efficiency", "timer"),
    ACCOUNTABILITY_PARTNER("Accountability Partner", "Commitment Checking, Slacking Callouts", "Strict", "shield")
}

@Entity(tableName = "ai_chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "model", "system"
    val content: String,
    val persona: AiCoachPersona = AiCoachPersona.BOARD_EXAM_COACH,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuggestedActionAvailable: Boolean = false,
    val suggestedActionTitle: String? = null,
    val suggestedActionRoute: String? = null
)
