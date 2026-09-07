package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MistakeSeverity(val label: String, val colorHex: String) {
    MINOR("Minor Slip", "#38E1FF"),
    MODERATE("Concept Gap", "#FFB300"),
    CRITICAL("Fatal Flaw", "#FF5252")
}

@Entity(
    tableName = "mistake_entries",
    indices = [
        Index(value = ["subjectCode"]),
        Index(value = ["chapterTitle"]),
        Index(value = ["isResolved"])
    ]
)
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectCode: String,
    val chapterTitle: String,
    val topicTitle: String = "",
    val questionOrContext: String,
    val studentMistake: String,
    val correctSolution: String,
    val coreConcept: String,
    val severity: MistakeSeverity = MistakeSeverity.MODERATE,
    val whyMade: String = "", // e.g., "Calculation error", "Misread question", "Forgot formula"
    val isResolved: Boolean = false,
    val reviewCount: Int = 0,
    val dateCreated: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)
