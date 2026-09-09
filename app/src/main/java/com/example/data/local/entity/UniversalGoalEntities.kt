package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Universal Goal Category:
 * Covers academic, professional, technical, fitness, and creative pursuits.
 */
enum class UniversalGoalCategory(val displayName: String) {
    BOARD_EXAMS("Board Exams"),
    NEET("NEET"),
    JEE("JEE"),
    UPSC("UPSC"),
    CA("CA (Chartered Accountancy)"),
    CODING("Coding & Tech"),
    FITNESS("Fitness & Health"),
    WEIGHT_LOSS("Weight Loss"),
    READING("Reading & Learning"),
    LANGUAGE("Language Learning"),
    BUSINESS("Business & Startups"),
    CAREER("Career Growth"),
    CUSTOM("Custom Goal")
}

/**
 * Skill Tree Node for interactive visual progression trees
 */
@Entity(tableName = "skill_nodes")
data class SkillNodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // e.g. "CODING", "FITNESS", "UPSC", "EXAMS"
    val treeTitle: String, // e.g. "Web Development", "Strength Mastery"
    val nodeName: String, // e.g. "HTML Basics", "CSS Flexbox", "React Hooks"
    val tierLevel: Int = 1, // 1 to 5
    val parentNodeId: Long? = null,
    val isUnlocked: Boolean = false,
    val isMastered: Boolean = false,
    val masteryPercentage: Int = 0,
    val xpReward: Int = 50,
    val description: String = ""
)

/**
 * Dynamic Multi-Horizon Roadmap
 */
@Entity(tableName = "roadmaps")
data class RoadmapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalTitle: String,
    val category: String,
    val horizonDays: Int = 30, // 30, 60, 90
    val totalMilestones: Int = 0,
    val completedMilestones: Int = 0,
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Roadmap Milestones
 */
@Entity(tableName = "roadmap_milestones")
data class RoadmapMilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roadmapId: Long,
    val milestoneTitle: String,
    val weekNumber: Int,
    val description: String,
    val isCompleted: Boolean = false,
    val targetDate: String = "",
    val xpReward: Int = 100
)

/**
 * Universal Custom Tracker Metric
 */
@Entity(tableName = "custom_trackers")
data class CustomTrackerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // e.g. "DSA Problems", "Vocabulary Words", "Cardio KM"
    val category: String, // "CODING", "FITNESS", "LANGUAGE", "ACADEMIC"
    val currentCount: Int = 0,
    val targetCount: Int = 100,
    val unit: String = "items", // "problems", "words", "km", "pages"
    val lastUpdated: Long = System.currentTimeMillis()
)
