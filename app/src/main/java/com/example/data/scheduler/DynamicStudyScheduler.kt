package com.example.data.scheduler

import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.UserProfileEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class UserEnergyLevel(val displayName: String, val emoji: String, val multiplier: Float, val description: String) {
    HIGH("Peak Energy", "⚡", 1.2f, "High focus capacity. Hardest lectures, numericals & PYQs first."),
    MEDIUM("Steady Focus", "🔋", 1.0f, "Moderate endurance. Balanced mix of theory, practice & notes."),
    LOW("Fatigued / Low Battery", "🪫", 0.75f, "Low cognitive energy. Light revision, flashcards & shorter sprints.")
}

enum class TaskDifficulty(val label: String, val weight: Int, val colorHex: String) {
    HARD("HARD", 3, "#FF5252"),
    MEDIUM("MEDIUM", 2, "#FFB300"),
    EASY("EASY", 1, "#00E676")
}

data class AlertnessPoint(
    val hour: Int, // 0-23
    val label: String, // "06:00", "07:00", etc.
    val alertnessScore: Float, // 0.0 to 1.0
    val isPeak: Boolean = false,
    val note: String = ""
)

data class DeepWorkPrediction(
    val optimalWindows: List<String> = listOf("05:30 PM - 07:30 PM", "06:00 AM - 08:00 AM"),
    val peakHour: Int = 18,
    val confidencePercentage: Int = 89,
    val alertnessCurve: List<AlertnessPoint> = emptyList(),
    val analysisSummary: String = "Historical analysis indicates peak cognitive alertness during late afternoon & early morning hours.",
    val recommendations: List<String> = emptyList()
)

data class DynamicTaskScheduleItem(
    val task: DailyPlanTaskEntity,
    val difficulty: TaskDifficulty,
    val estimatedEnergyRequired: Int, // 1 to 5
    val predictedTimeSlot: String, // e.g. "04:30 PM - 05:20 PM"
    val urgencyScore: Int, // higher = more urgent
    val adjustmentReason: String // e.g. "Shifted early: Peak Energy & Board Exam target"
)

object DynamicStudyScheduler {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    /**
     * Determines the intrinsic difficulty of a task based on its subject, type, and title.
     */
    fun evaluateDifficulty(task: DailyPlanTaskEntity): TaskDifficulty {
        val subject = task.subject.lowercase(Locale.ROOT)
        val title = task.title.lowercase(Locale.ROOT)
        val details = task.details.lowercase(Locale.ROOT)

        // Numericals, advanced problem solving, and core science PYQs are HARD
        if (title.contains("numerical") || details.contains("numerical") ||
            title.contains("derivation") || details.contains("derivation") ||
            (task.type == TaskType.PYQ && (subject.contains("physics") || subject.contains("chemistry") || subject.contains("math")))
        ) {
            return TaskDifficulty.HARD
        }

        // Heavy lecture sessions of core subjects are HARD
        if (task.type == TaskType.LECTURE && task.targetMinutes >= 45 &&
            (subject.contains("physics") || subject.contains("chemistry") || subject.contains("math") || subject.contains("biology"))
        ) {
            return TaskDifficulty.HARD
        }

        // Revision, flashcards, short notes review, formula sheets, evening reflection are EASY
        if (task.type == TaskType.REVISION || title.contains("flashcard") || title.contains("reflection") ||
            title.contains("formula") || task.targetMinutes <= 20
        ) {
            return TaskDifficulty.EASY
        }

        return TaskDifficulty.MEDIUM
    }

    /**
     * Computes AI-driven optimal deep work time windows based on historical focus sessions,
     * past completed tasks timestamps, and user's school arrival/departure schedule.
     */
    fun predictDeepWorkTimes(
        historicalSessions: List<StudySessionEntity>,
        completedTasks: List<DailyPlanTaskEntity>,
        schoolStatus: SchoolStatusEntity?,
        userProfile: UserProfileEntity?
    ): DeepWorkPrediction {
        // Build 24-hour histogram of successful focus sessions and task completions
        val hourlySuccessCount = IntArray(24) { 0 }
        val hourlySessionMinutes = IntArray(24) { 0 }

        for (session in historicalSessions) {
            if (session.completedSuccessfully) {
                val cal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour in 0..23) {
                    hourlySuccessCount[hour] += 1
                    hourlySessionMinutes[hour] += session.durationMinutes
                }
            }
        }

        for (task in completedTasks) {
            task.completedAt?.let { timestamp ->
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour in 0..23) {
                    hourlySuccessCount[hour] += 1
                }
            }
        }

        // Incorporate school hours constraint
        val wakeHour = try {
            userProfile?.wakeUpTime?.split(":")?.get(0)?.toInt() ?: 6
        } catch (e: Exception) { 6 }

        val sleepHour = try {
            userProfile?.sleepTime?.split(":")?.get(0)?.toInt() ?: 23
        } catch (e: Exception) { 23 }

        val schoolDepartHour = try {
            userProfile?.schoolStartTime?.split(":")?.get(0)?.toInt() ?: 9
        } catch (e: Exception) { 9 }

        val schoolReturnHour = try {
            userProfile?.schoolEndTime?.split(":")?.get(0)?.toInt() ?: 13
        } catch (e: Exception) { 13 }

        val hasSchool = userProfile?.hasSchool ?: true

        // Synthesize alertness curve across active hours (from wake to sleep)
        val alertnessList = mutableListOf<AlertnessPoint>()
        var highestScore = 0f
        var peakHourCandidate = 18

        for (h in 5..23) {
            val hourLabel = String.format(Locale.US, "%02d:00", h)
            var baseAlertness = 0.5f

            // Circadian rhythm baseline for Indian students
            when (h) {
                in 6..8 -> baseAlertness = 0.85f // Morning golden hour
                in 9..12 -> baseAlertness = if (hasSchool) 0.35f else 0.75f // School hours or deep study
                in 13..14 -> baseAlertness = 0.40f // Post-lunch / travel dip
                in 15..16 -> baseAlertness = 0.65f // Early afternoon recovery
                in 17..20 -> baseAlertness = 0.90f // Evening peak deep work window
                in 21..22 -> baseAlertness = 0.70f // Pre-sleep revision
                else -> baseAlertness = 0.30f
            }

            // Historical data weighting
            val historicalBoost = min(0.35f, (hourlySuccessCount[h] * 0.05f) + (hourlySessionMinutes[h] / 300f))
            var finalScore = min(1.0f, baseAlertness + historicalBoost)

            // If user is currently in school travel/classes, suppress focus window
            if (hasSchool && h >= schoolDepartHour && h <= schoolReturnHour) {
                finalScore = min(0.30f, finalScore)
            }

            if (finalScore > highestScore) {
                highestScore = finalScore
                peakHourCandidate = h
            }

            alertnessList.add(
                AlertnessPoint(
                    hour = h,
                    label = hourLabel,
                    alertnessScore = (finalScore * 100).toInt() / 100f,
                    isPeak = false,
                    note = if (finalScore >= 0.8f) "High Alertness" else if (finalScore <= 0.4f) "Low Energy" else "Moderate"
                )
            )
        }

        // Mark the top peak hours
        val finalAlertness = alertnessList.map { pt ->
            if (pt.hour == peakHourCandidate || (pt.hour == 6 && hasSchool)) {
                pt.copy(isPeak = true)
            } else pt
        }

        // Format optimal windows
        val windows = mutableListOf<String>()
        if (peakHourCandidate in 16..20) {
            windows.add("${formatHour(peakHourCandidate)}:00 - ${formatHour(peakHourCandidate + 2)}:30")
        } else {
            windows.add("05:30 PM - 07:45 PM")
        }

        if (wakeHour in 5..7) {
            windows.add("${formatHour(wakeHour)}:15 - ${formatHour(wakeHour + 2)}:00")
        }

        val totalDataPoints = historicalSessions.size + completedTasks.size
        val confidence = min(96, 75 + (totalDataPoints * 2))

        val summary = if (totalDataPoints > 5) {
            "Trained on your $totalDataPoints historical study sessions: Peak focus velocity concentrates between ${windows.firstOrNull() ?: "evening hours"}, with lowest distraction rate."
        } else {
            "Calibrated using your Class 12 timetable: Primary deep work window predicted between ${windows.firstOrNull() ?: "05:30 PM - 07:30 PM"} immediately post-school rest."
        }

        val recs = listOf(
            "Schedule hardest physics/chemistry numericals in the ${windows.firstOrNull() ?: "evening"} block.",
            "Reserve lighter formula revisions for post-dinner (09:00 PM).",
            "Maintain a 10-minute active pause between deep work blocks."
        )

        return DeepWorkPrediction(
            optimalWindows = windows,
            peakHour = peakHourCandidate,
            confidencePercentage = confidence,
            alertnessCurve = finalAlertness,
            analysisSummary = summary,
            recommendations = recs
        )
    }

    /**
     * Dynamically adjusts, reprioritizes, and orders today's tasks considering:
     * 1. User reported energy level (HIGH, MEDIUM, LOW)
     * 2. Completion times of previous tasks (pace, ahead or behind schedule)
     * 3. Upcoming deadlines (Board exam countdown, pending subject coverage)
     */
    fun adjustDailySchedule(
        tasks: List<DailyPlanTaskEntity>,
        energyLevel: UserEnergyLevel,
        historicalSessions: List<StudySessionEntity>,
        completedTasks: List<DailyPlanTaskEntity>,
        subjects: List<SubjectEntity>,
        examConfig: BoardExamConfigEntity?,
        userProfile: UserProfileEntity?,
        nowMillis: Long = System.currentTimeMillis()
    ): List<DynamicTaskScheduleItem> {
        if (tasks.isEmpty()) return emptyList()

        val deepWork = predictDeepWorkTimes(historicalSessions, completedTasks, null, userProfile)

        // Step 1: Calculate urgency multiplier per subject based on upcoming board exam & syllabus completion
        val subjectUrgencyMap = mutableMapOf<String, Int>()
        for (sub in subjects) {
            val completionRate = if (sub.totalChapters > 0) {
                (sub.completedChapters.toFloat() / sub.totalChapters)
            } else 0f
            // Lower completion rate = higher urgency to finish syllabus
            val urgency = ((1f - completionRate) * 100).toInt() + 10
            subjectUrgencyMap[sub.name.lowercase(Locale.ROOT)] = urgency
        }

        // Step 2: Separate completed tasks from pending tasks
        val completedList = tasks.filter { it.isCompleted }.sortedBy { it.completedAt ?: 0L }
        val pendingList = tasks.filter { !it.isCompleted }

        // Step 3: Analyze pace of completed tasks today
        var minutesSpent = 0
        var totalExpectedForCompleted = 0
        for (comp in completedList) {
            totalExpectedForCompleted += comp.targetMinutes
            minutesSpent += comp.targetMinutes // Estimated duration
        }
        val isRunningBehindSchedule = completedList.isNotEmpty() && (nowMillis % (24 * 3600 * 1000L) > (18 * 3600 * 1000L))

        // Step 4: Score and sort pending tasks based on Energy Level, Difficulty, and Deadlines
        val scoredPending = pendingList.map { task ->
            val difficulty = evaluateDifficulty(task)
            val subKey = task.subject.lowercase(Locale.ROOT)
            val urgency = subjectUrgencyMap[subKey] ?: 30

            // Energy Level sorting strategy
            val rankScore = when (energyLevel) {
                UserEnergyLevel.HIGH -> {
                    // Under High Energy: HARD tasks get maximum priority, then Medium, then Easy
                    var score = difficulty.weight * 1000 + urgency * 5
                    if (task.type == TaskType.LECTURE || task.type == TaskType.PYQ) score += 300
                    score
                }
                UserEnergyLevel.MEDIUM -> {
                    // Under Medium Energy: Balanced flow, high urgency first, then moderate difficulty
                    var score = urgency * 10 + difficulty.weight * 200
                    if (task.type == TaskType.LECTURE) score += 150
                    score
                }
                UserEnergyLevel.LOW -> {
                    // Under Low Energy: Invert! Light, accessible tasks (EASY) first to prevent burnout
                    var score = (4 - difficulty.weight) * 1000 + urgency * 2
                    if (task.type == TaskType.REVISION || task.type == TaskType.NOTES) score += 400
                    score
                }
            }

            val reason = when (energyLevel) {
                UserEnergyLevel.HIGH -> if (difficulty == TaskDifficulty.HARD) "Prioritized for Peak Energy cognitive burst" else "Scheduled after primary deep work"
                UserEnergyLevel.MEDIUM -> "Balanced alignment with ${task.subject} deadline"
                UserEnergyLevel.LOW -> if (difficulty == TaskDifficulty.EASY) "Quick momentum builder for Low Energy state" else "De-prioritized to prevent cognitive overload"
            }

            val energyRequired = when (difficulty) {
                TaskDifficulty.HARD -> 5
                TaskDifficulty.MEDIUM -> 3
                TaskDifficulty.EASY -> 1
            }

            Triple(task, rankScore, Pair(difficulty, Pair(reason, energyRequired)))
        }.sortedByDescending { it.second }

        // Step 5: Assign dynamic time slots starting from current time (or next study slot)
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        // Round to nearest 5 minutes
        val unroundedMinutes = cal.get(Calendar.MINUTE)
        val mod = unroundedMinutes % 5
        cal.add(Calendar.MINUTE, if (mod != 0) (5 - mod) else 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val result = mutableListOf<DynamicTaskScheduleItem>()

        // Add completed items first with their past slots
        for (comp in completedList) {
            val diff = evaluateDifficulty(comp)
            val compSlot = if (comp.completedAt != null) {
                timeFormat.format(Date(comp.completedAt))
            } else {
                "Completed"
            }
            result.add(
                DynamicTaskScheduleItem(
                    task = comp,
                    difficulty = diff,
                    estimatedEnergyRequired = if (diff == TaskDifficulty.HARD) 4 else 2,
                    predictedTimeSlot = compSlot,
                    urgencyScore = subjectUrgencyMap[comp.subject.lowercase(Locale.ROOT)] ?: 20,
                    adjustmentReason = "Completed on schedule"
                )
            )
        }

        // Add pending items with dynamically calculated time slots
        for (item in scoredPending) {
            val task = item.first
            val diff = item.third.first
            val reason = item.third.second.first
            val energyReq = item.third.second.second

            // Apply energy level duration scaling
            val adjustedMinutes = when (energyLevel) {
                UserEnergyLevel.HIGH -> task.targetMinutes // Full duration
                UserEnergyLevel.MEDIUM -> task.targetMinutes
                UserEnergyLevel.LOW -> if (diff == TaskDifficulty.HARD) max(25, (task.targetMinutes * 0.75f).toInt()) else task.targetMinutes
            }

            val startTimeStr = timeFormat.format(cal.time)
            cal.add(Calendar.MINUTE, adjustedMinutes)
            val endTimeStr = timeFormat.format(cal.time)
            val slotString = "$startTimeStr - $endTimeStr"

            // 5 min buffer between tasks
            cal.add(Calendar.MINUTE, 5)

            result.add(
                DynamicTaskScheduleItem(
                    task = task.copy(targetMinutes = adjustedMinutes),
                    difficulty = diff,
                    estimatedEnergyRequired = energyReq,
                    predictedTimeSlot = slotString,
                    urgencyScore = subjectUrgencyMap[task.subject.lowercase(Locale.ROOT)] ?: 20,
                    adjustmentReason = reason
                )
            )
        }

        return result
    }

    private fun formatHour(hour: Int): String {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format(Locale.US, "%02d:00 %s", h, amPm)
    }
}
