package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AlarmChallengeType(val label: String, val description: String) {
    MATH("Math Challenge", "Solve math expressions to silence the alarm"),
    CAPTCHA("Captcha Verification", "Type random dynamic security code"),
    PHYSICAL_SHAKE("Physical Shake", "Shake your device vigorously 20 times"),
    PHYSICAL_STEPS("Walk Steps", "Walk 30 steps away from bed")
}

enum class AlarmDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

enum class AlarmVibrationPattern(val label: String) {
    HEAVY("Heavy Continuous"),
    PULSE("Rhythmic Pulse"),
    RAPID("Rapid Urgent"),
    STEADY("Steady Wave")
}

@Entity(
    tableName = "alarms",
    indices = [
        Index(value = ["isEnabled"]),
        Index(value = ["hour", "minute"])
    ]
)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Apex Protocol Wake-Up",
    val hour: Int = 5,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val repeatDaysJson: String = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
    val volumePercent: Int = 90,
    val isVibrationEnabled: Boolean = true,
    val vibrationPattern: AlarmVibrationPattern = AlarmVibrationPattern.PULSE,
    val ringtonePreset: String = "CYBER_SIREN", // "BELL", "CYBER_SIREN", "ZEN_CHIME", "APEX_HORNS", "SYSTEM_DEFAULT"
    val challengeType: AlarmChallengeType = AlarmChallengeType.MATH,
    val challengeDifficulty: AlarmDifficulty = AlarmDifficulty.MEDIUM,
    val isSmartSnoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 5,
    val snoozeRingtonePreset: String = "TICK_TOCK",
    val maxSnoozes: Int = 3,
    val currentSnoozeCount: Int = 0,
    val isEscalatingVolume: Boolean = true,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "alarm_logs",
    indices = [
        Index(value = ["alarmId"]),
        Index(value = ["date"]),
        Index(value = ["timestamp"])
    ]
)
data class AlarmLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val alarmTitle: String,
    val targetTime: String, // "05:00 AM"
    val triggeredTime: String,
    val dismissedTime: String? = null,
    val snoozesUsed: Int = 0,
    val challengeType: AlarmChallengeType = AlarmChallengeType.MATH,
    val solvedSuccessfully: Boolean = true,
    val isMissed: Boolean = false,
    val date: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)
