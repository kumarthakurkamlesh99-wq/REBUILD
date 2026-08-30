package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // "Physics", "Chemistry", "Biology", "English", "Hindi"
    val code: String = "",
    val iconName: String = "menu_book",
    val colorHex: String = "#38E1FF",
    val totalChapters: Int = 14,
    val completedChapters: Int = 0,
    val targetHours: Int = 100,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val chapterNumber: Int,
    val title: String,
    val totalLectures: Int = 5,
    val completedLectures: Int = 0,
    val notesDone: Boolean = false,
    val pyqsDone: Boolean = false,
    val revisionCount: Int = 0,
    val lastRevisionDate: String? = null,
    val isCompleted: Boolean = false,
    val completionPercentage: Int = 0
)

enum class SessionType {
    POMODORO_25_5,
    POMODORO_50_10,
    CUSTOM_FOCUS,
    DEEP_WORK,
    REVISION,
    PYQ_PRACTICE
}

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val chapterName: String = "",
    val sessionType: SessionType = SessionType.POMODORO_25_5,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // "yyyy-MM-dd"
    val xpEarned: Int = 50,
    val completedSuccessfully: Boolean = true
)
