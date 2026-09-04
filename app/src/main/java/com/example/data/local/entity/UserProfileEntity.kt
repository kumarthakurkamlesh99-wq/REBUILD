package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val isCompleted: Boolean = false,

    // 1. Personal Details
    val name: String = "Kamlesh Kumar Thakur",
    val studentClass: String = "Class 12", // "Class 10", "Class 11", "Class 12", "Dropper / JEE / NEET", "College"
    val board: String = "Bihar Board", // "Bihar Board", "CBSE", "ICSE", "State Board", "Other"
    val stream: String = "Science (PCM)", // "Science (PCM)", "Science (PCB)", "Science (PCMB)", "Commerce", "Arts / Humanities", "General"
    val targetPercentage: Int = 95,
    val targetExamName: String = "Class 12 Board Exam 2027",
    val targetExamDate: String = "2027-02-15", // "yyyy-MM-dd"
    val avatarUri: String = "",
    val winterArcStartDate: String = "2026-08-01", // "yyyy-MM-dd"
    val goal: String = "Crack Bihar Board Class 12 with 95%+ and build elite discipline",

    // 2. Academic Details
    val selectedSubjectsJson: String = "[\"Physics\", \"Chemistry\", \"Mathematics\", \"English\"]",
    val strongSubjectsJson: String = "[]",
    val weakSubjectsJson: String = "[]",
    val preparationLevel: String = "Intermediate (30-70%)", // "Beginner (<30%)", "Intermediate (30-70%)", "Advanced (>70%)"

    // 3. School Routine
    val hasSchool: Boolean = true,
    val schoolStartTime: String = "09:45", // "HH:mm" (Departure/Start)
    val schoolEndTime: String = "13:00", // "HH:mm" (Return/Dispersal)
    val travelTimeMinutes: Int = 25,
    val weeklyOffDaysJson: String = "[\"Sunday\"]", // List of weekly off days e.g. ["Sunday"]

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
