package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val isCompleted: Boolean = false,

    // 1. Personal Details
    val name: String = "",
    val studentClass: String = "", // "Class 10", "Class 11", "Class 12", "Dropper / JEE / NEET", "College"
    val board: String = "", // "Bihar Board", "CBSE", "ICSE", "State Board", "Other"
    val stream: String = "", // "Science (PCM)", "Science (PCB)", "Science (PCMB)", "Commerce", "Arts / Humanities", "General"
    val targetPercentage: Int = 0,
    val targetExamName: String = "",
    val targetExamDate: String = "", // "yyyy-MM-dd"
    val avatarUri: String = "",
    val winterArcStartDate: String = "", // "yyyy-MM-dd"
    val goal: String = "",
    val primaryGoalsJson: String = "[]", // Universal goals e.g. ["Coding", "Fitness", "NEET", "UPSC"]
    val personaType: String = "Student", // "Student", "College", "Aspirant", "Coder", "Fitness", "Professional", "Entrepreneur"
    val customGoalStatement: String = "",

    // 2. Academic Details
    val selectedSubjectsJson: String = "[]",
    val strongSubjectsJson: String = "[]",
    val weakSubjectsJson: String = "[]",
    val preparationLevel: String = "Beginner (<30%)", // "Beginner (<30%)", "Intermediate (30-70%)", "Advanced (>70%)"

    // 3. School Routine
    val hasSchool: Boolean = false,
    val schoolStartTime: String = "", // "HH:mm" (Departure/Start)
    val schoolEndTime: String = "", // "HH:mm" (Return/Dispersal)
    val travelTimeMinutes: Int = 0,
    val weeklyOffDaysJson: String = "[]", // List of weekly off days e.g. ["Sunday"]

    // 4. Study Preferences
    val wakeUpTime: String = "06:00", // "HH:mm"
    val sleepTime: String = "22:30", // "HH:mm"
    val dailyStudyGoalHours: Float = 6.0f,
    val preferredSessionDurationMinutes: Int = 50, // 25, 50, 90

    // 5. Fitness & Calisthenics
    val workoutGoal: String = "Calisthenics & Strength",
    val workoutType: String = "Calisthenics", // "Calisthenics", "Running / Cardio", "Gym / Weights", "Home Workout"
    val workoutTime: String = "17:00", // "HH:mm"
    val workoutDurationMinutes: Int = 30,

    // 6. AI Coach Setup
    val coachingStyle: String = "Monk Mode (Strict Discipline)", // "Monk Mode (Strict Discipline)", "Balanced Mentor", "Encouraging Guide"
    val geminiApiKey: String = "",

    // 7. Notification Preferences
    val notifyWakeUp: Boolean = true,
    val notifySchoolDeparture: Boolean = true,
    val notifySchoolArrival: Boolean = true,
    val notifyReturnHome: Boolean = true,
    val notifyStudySessions: Boolean = true,
    val notifyWorkout: Boolean = true,
    val notifyRevision: Boolean = true,
    val notifyReflection: Boolean = true,
    val notifySleep: Boolean = true,

    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val updatedAtTimestamp: Long = System.currentTimeMillis()
)
