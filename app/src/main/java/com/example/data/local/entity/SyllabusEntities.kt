package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyllabusStatus(val label: String, val weight: Int, val colorHex: String) {
    NOT_STARTED("Not Started", 0, "#7E8B9B"),
    IN_PROGRESS("In Progress", 30, "#FFB300"),
    COMPLETED("Completed", 60, "#38E1FF"),
    REVISED_ONCE("Revised Once", 80, "#70B8FF"),
    REVISED_TWICE("Revised Twice", 90, "#B388FF"),
    MASTERED("Mastered", 100, "#00E676")
}

@Entity(
    tableName = "syllabus_units",
    indices = [
        Index(value = ["subjectCode"]),
        Index(value = ["orderIndex"])
    ]
)
data class SyllabusUnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectCode: String, // "PHYSICS", "CHEMISTRY", "BIOLOGY", "HINDI", "ENGLISH"
    val subjectName: String, // "Physics (Class XII)", etc.
    val unitNumber: Int,
    val unitTitle: String,
    val description: String = "",
    val totalTopicsCount: Int = 0,
    val completedTopicsCount: Int = 0,
    val completionPercentage: Int = 0,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "syllabus_chapters",
    foreignKeys = [
        ForeignKey(
            entity = SyllabusUnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["unitId"]),
        Index(value = ["subjectCode"]),
        Index(value = ["status"])
    ]
)
data class SyllabusChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val unitId: Long,
    val subjectCode: String,
    val chapterNumber: Int,
    val title: String,
    val description: String = "",
    val status: SyllabusStatus = SyllabusStatus.NOT_STARTED,
    val notesDone: Boolean = false,
    val pyqsDone: Boolean = false,
    val revisionCount: Int = 0,
    val totalTopicsCount: Int = 0,
    val completedTopicsCount: Int = 0,
    val completionPercentage: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "syllabus_topics",
    foreignKeys = [
        ForeignKey(
            entity = SyllabusChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chapterId"]),
        Index(value = ["unitId"]),
        Index(value = ["subjectCode"]),
        Index(value = ["status"])
    ]
)
data class SyllabusTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long,
    val unitId: Long,
    val subjectCode: String,
    val topicNumber: Int,
    val title: String,
    val status: SyllabusStatus = SyllabusStatus.NOT_STARTED,
    val notesDone: Boolean = false,
    val pyqsDone: Boolean = false,
    val revisionCount: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
