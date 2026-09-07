package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "syllabus_subtopics",
    foreignKeys = [
        ForeignKey(
            entity = SyllabusTopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["topicId"]),
        Index(value = ["subjectCode"]),
        Index(value = ["isCompleted"])
    ]
)
data class SyllabusSubtopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: Long,
    val subjectCode: String,
    val subtopicNumber: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val confidenceLevel: Int = 3, // 1 (Weak/Red) to 5 (Mastered/Green)
    val lastStudiedTimestamp: Long = System.currentTimeMillis()
)
