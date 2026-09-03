package com.example.data.model

data class DailyStudyPoint(
    val date: String,
    val dayLabel: String,
    val hours: Float,
    val minutes: Int
)

data class WeeklyStudyPoint(
    val weekLabel: String,
    val hours: Float
)

data class MonthlyStudyPoint(
    val monthLabel: String,
    val hours: Float
)

data class HabitHeatmapDay(
    val date: String,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val completedCount: Int,
    val totalCount: Int,
    val completionRate: Float
)

data class SubjectProgressItem(
    val subjectId: Long,
    val name: String,
    val colorHex: String,
    val studyHours: Float,
    val totalChapters: Int,
    val completedChapters: Int,
    val percentage: Int
)

data class XpGrowthPoint(
    val date: String,
    val dayLabel: String,
    val xpGained: Int,
    val cumulativeXp: Int
)

data class StreakTrendPoint(
    val date: String,
    val dayLabel: String,
    val streak: Int
)

data class ProtocolCompletionPoint(
    val date: String,
    val dayLabel: String,
    val completedCount: Int,
    val totalCount: Int,
    val completionRate: Float
)

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class SubjectReportBreakdown(
    val subjectName: String,
    val studyHours: Float,
    val completedTasks: Int,
    val totalChapters: Int,
    val completedChapters: Int
)

data class ExecutiveReportData(
    val period: ReportPeriod,
    val title: String,
    val dateRange: String,
    val studyHours: Float,
    val tasksCompleted: Int,
    val totalTasks: Int,
    val completionPercentage: Int,
    val currentStreak: Int,
    val xpEarned: Int,
    val missedTasksCount: Int,
    val subjectBreakdown: List<SubjectReportBreakdown>,
    val hasData: Boolean
)
