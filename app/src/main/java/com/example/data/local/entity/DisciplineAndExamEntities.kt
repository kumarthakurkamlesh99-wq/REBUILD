package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WorkoutLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class ExerciseType {
    RUNNING,
    WALKING,
    PUSHUPS,
    SQUATS,
    PLANK,
    PULLUPS,
    CUSTOM
}

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val exerciseName: String, // "Running", "Pushups", "Squats", "Walking"
    val exerciseType: ExerciseType = ExerciseType.PUSHUPS,
    val level: WorkoutLevel = WorkoutLevel.INTERMEDIATE,
    val sets: Int = 3,
    val reps: Int = 15,
    val durationMinutes: Int = 20,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // "Holi", "Diwali", "Chhath", "Durga Puja", "Raksha Bandhan", "Independence Day", "Republic Day"
    val date: String, // "yyyy-MM-dd" or recurrent month-day "MM-dd"
    val isRecurring: Boolean = true,
    val isIndianFestival: Boolean = true,
    val workloadReductionPercent: Int = 50, // reduces daily study target by 50% or 100%
    val note: String = ""
)

@Entity(tableName = "daily_discipline_scores")
data class DailyDisciplineEntity(
    @PrimaryKey
    val date: String, // "yyyy-MM-dd"
    val studyScore: Int = 0, // max 40
    val workoutScore: Int = 0, // max 20
    val noPornScore: Int = 0, // max 15
    val sleepScore: Int = 0, // max 15
    val readingScore: Int = 0, // max 10
    val otherHabitsScore: Int = 0,
    val totalScore: Int = 0, // max 100
    val xpEarned: Int = 0,
    val isEvaluated: Boolean = true
)

@Entity(tableName = "winter_arc_state")
data class WinterArcStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val startDate: String = "2026-08-01", // "yyyy-MM-dd"
    val targetDays: Int = 90,
    val currentDay: Int = 27,
    val xp: Int = 5420,
    val level: Int = 14,
    val streak: Int = 11,
    val bestStreak: Int = 21,
    val transformationScore: Int = 86,
    val targetDailyDeepWorkHours: Float = 6.0f
)

@Entity(tableName = "board_exam_config")
data class BoardExamConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val examName: String = "Class 12 CBSE Board Exam",
    val examDate: String = "2027-02-15", // "yyyy-MM-dd"
    val totalSyllabusChapters: Int = 70,
    val completedChapters: Int = 38,
    val targetPercentage: Int = 95,
    val dailyTargetLectures: Int = 2,
    val dailyTargetRevisions: Int = 1
)
