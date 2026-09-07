package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcard_decks",
    indices = [
        Index(value = ["subjectCode"]),
        Index(value = ["chapterTitle"])
    ]
)
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subjectCode: String,
    val chapterTitle: String = "",
    val totalCards: Int = 0,
    val masteredCards: Int = 0,
    val lastReviewedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flashcards",
    indices = [
        Index(value = ["deckId"]),
        Index(value = ["subjectCode"]),
        Index(value = ["nextReviewTimestamp"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val subjectCode: String,
    val chapterTitle: String = "",
    val topicTitle: String = "",
    val question: String,
    val answer: String,
    val explanation: String = "",
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitionNumber: Int = 0,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val isMastered: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
