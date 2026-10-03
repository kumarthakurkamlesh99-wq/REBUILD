package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.SyllabusStatus
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.repository.RebuildRepository
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean
)

data class DashboardScheduleItem(
    val id: String,
    val timeDisplay: String,
    val title: String,
    val subtitle: String = "",
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false,
    val category: String = "Study"
)

data class DashboardAiRecommendation(
    val headline: String,
    val recommendation: String,
    val subject: String? = null,
    val chapter: String? = null
)

data class HomeUiState(
    val userProfile: UserProfileEntity? = null,
    val winterArcState: WinterArcStateEntity = WinterArcStateEntity(),
    val boardExamConfig: BoardExamConfigEntity = BoardExamConfigEntity(),
    val disciplineScore: DailyDisciplineEntity = DailyDisciplineEntity(date = "", totalScore = 0),
    val schoolStatus: SchoolStatusEntity = SchoolStatusEntity(date = ""),
    val todayTasks: List<DailyPlanTaskEntity> = emptyList(),
    val daysUntilExam: Long = 0,
    val todayStudyMinutes: Int = 0,
    val targetStudyMinutes: Int = 480, // 8 hours default
    val completedTasksCount: Int = 0,
    val totalTasksCount: Int = 0,
    val progressPercentage: Int = 0,
    val realStreak: Int = 0,
    val winterArcCurrentDay: Int = 1,
    val winterArcTotalDays: Int = 90,
    val winterArcDaysRemaining: Int = 0,
    val readinessScore: Int = 0,
    val habits: List<HabitWithStatus> = emptyList(),
    val completedHabitsCount: Int = 0,
    val totalHabitsCount: Int = 0,
    val scheduleTimeline: List<DashboardScheduleItem> = emptyList(),
    val subjectSummaries: List<SyllabusSubjectSummary> = emptyList(),
    val overallSyllabusPercentage: Int = 0,
    val totalSyllabusChapters: Int = 0,
    val completedSyllabusChapters: Int = 0,
    val aiRecommendation: DashboardAiRecommendation? = null
)

class HomeViewModel(private val repository: RebuildRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.syncWinterArcCalculations()
            repository.initializeMasterSyllabusIfEmpty()
        }
    }

    private val baseProfileFlow = combine(
        repository.getUserProfile(),
        repository.getWinterArcState(),
        repository.getBoardExamConfig()
    ) { profile, arc, exam ->
        Triple(profile, arc ?: WinterArcStateEntity(), exam ?: BoardExamConfigEntity())
    }

    private val syllabusFlow = combine(
        repository.getAllSyllabusChapters(),
        repository.getAllSyllabusTopics()
    ) { allChapters, allTopics ->
        val subjects = listOf("PHYSICS", "CHEMISTRY", "BIOLOGY", "HINDI", "ENGLISH")
        val summaries = subjects.map { code ->
            val subChapters = allChapters.filter { it.subjectCode == code }
            val subTopics = allTopics.filter { it.subjectCode == code }
            val totalCh = subChapters.size
            val compCh = subChapters.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.MASTERED }
            val mastCh = subChapters.count { it.status == SyllabusStatus.MASTERED }
            val totalTop = subTopics.size
            val compTop = subTopics.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.REVISED_ONCE || it.status == SyllabusStatus.REVISED_TWICE || it.status == SyllabusStatus.MASTERED }
            val pct = if (totalTop > 0) (compTop * 100) / totalTop else 0

            val subName = when (code) {
                "PHYSICS" -> "Physics"
                "CHEMISTRY" -> "Chemistry"
                "BIOLOGY" -> "Biology"
                "HINDI" -> "Hindi Core"
                "ENGLISH" -> "English Core"
                else -> code
            }

            SyllabusSubjectSummary(
                code = code,
                name = subName,
                totalChapters = totalCh,
                completedChapters = compCh,
                masteredChapters = mastCh,
                totalTopics = totalTop,
                completedTopics = compTop,
                percentage = pct
            )
        }

        val totalAllTopics = allTopics.size
        val compAllTopics = allTopics.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.REVISED_ONCE || it.status == SyllabusStatus.REVISED_TWICE || it.status == SyllabusStatus.MASTERED }
        val overallPct = if (totalAllTopics > 0) (compAllTopics * 100) / totalAllTopics else 0
        val totalCh = allChapters.size
        val compCh = allChapters.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.MASTERED }

        SyllabusBundle(summaries, overallPct, totalCh, compCh)
    }

    private val habitsFlow = combine(
        repository.getAllHabits(),
        repository.getTodayHabitLogs()
    ) { habits, logs ->
        val logMap = logs.associateBy { it.habitId }
        val habitsWithStatus = habits.filter { !it.isArchived }.map { habit ->
            HabitWithStatus(
                habit = habit,
                isCompletedToday = logMap[habit.id]?.isCompleted ?: false
            )
        }
        val completedCount = habitsWithStatus.count { it.isCompletedToday }
        HabitsBundle(habitsWithStatus, completedCount, habitsWithStatus.size)
    }

    private data class DailyStatusBundle(
        val discipline: DailyDisciplineEntity?,
        val school: SchoolStatusEntity?,
        val tasks: List<DailyPlanTaskEntity>,
        val studyMins: Int
    )

    private data class SyllabusBundle(
        val summaries: List<SyllabusSubjectSummary>,
        val overallPct: Int,
        val totalCh: Int,
        val compCh: Int
    )

    private data class HabitsBundle(
        val habits: List<HabitWithStatus>,
        val completedCount: Int,
        val totalCount: Int
    )

    private val statusFlow = combine(
        repository.getTodayDiscipline(),
        repository.getTodaySchoolStatus(),
        repository.getTodayTasks(),
        repository.getTodayStudyMinutes()
    ) { discipline, school, tasks, studyMins ->
        DailyStatusBundle(discipline, school, tasks, studyMins)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseProfileFlow,
        statusFlow,
        syllabusFlow,
        habitsFlow
    ) { (profile, safeWinterArc, safeExamConfig), statusBundle, syllabusBundle, habitsBundle ->
        val safeDiscipline = statusBundle.discipline ?: DailyDisciplineEntity(
            date = repository.getTodayDateString(),
            totalScore = 0
        )
        val safeSchool = statusBundle.school ?: SchoolStatusEntity(date = repository.getTodayDateString())
        val daysLeft = repository.calculateDaysUntilBoardExam(safeExamConfig.examDate)
        val completedCount = statusBundle.tasks.count { it.isCompleted }
        val totalCount = statusBundle.tasks.size
        val progress = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

        val dayNum = repository.calculateWinterArcDayNumber(safeWinterArc.startDate, safeWinterArc.targetDays)
        val arcDaysLeft = repository.calculateWinterArcDaysRemaining(safeWinterArc.startDate, safeWinterArc.targetDays)
        val realStreak = repository.calculateRealStreak()
        val score = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else safeDiscipline.totalScore

        val dynamicArc = safeWinterArc.copy(
            currentDay = dayNum,
            streak = realStreak,
            transformationScore = score
        )

        // Calculate student readiness score:
        // 40% syllabus completion + 35% daily task completion + 25% streak consistency
        val syllabusPart = (syllabusBundle.overallPct * 0.40f).toInt()
        val taskPart = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 35).toInt() else (score * 0.35f).toInt()
        val streakPart = (min(realStreak, 30) * (25f / 30f)).toInt()
        val readinessScore = (syllabusPart + taskPart + streakPart).coerceIn(10, 100)

        // Build dynamic schedule timeline
        val timeline = mutableListOf<DashboardScheduleItem>()

        val wakeTime = DateTimeUtils.formatTo12Hour(profile?.wakeUpTime?.ifBlank { "05:00 AM" } ?: "05:00 AM")
        timeline.add(
            DashboardScheduleItem(
                id = "wake_routine",
                timeDisplay = wakeTime,
                title = "Morning Awakening & Routine",
                subtitle = "Hydration & Physical Readiness",
                isCompleted = habitsBundle.habits.firstOrNull { it.habit.name.contains("Running", true) || it.habit.name.contains("Workout", true) }?.isCompletedToday ?: (statusBundle.studyMins > 0),
                category = "Routine"
            )
        )

        if (!profile?.schoolStartTime.isNullOrBlank()) {
            val schoolStart12 = DateTimeUtils.formatTo12Hour(profile!!.schoolStartTime)
            timeline.add(
                DashboardScheduleItem(
                    id = "school_transit",
                    timeDisplay = schoolStart12,
                    title = "Academic School Hours",
                    subtitle = "Classes & syllabus coverage",
                    isCompleted = safeSchool.currentState == SchoolState.ARRIVED_HOME || safeSchool.currentState == SchoolState.TRAVELLING_HOME,
                    category = "School"
                )
            )
        }

        if (statusBundle.tasks.isNotEmpty()) {
            statusBundle.tasks.sortedBy { it.orderIndex }.forEachIndexed { index, task ->
                val timeStr = DateTimeUtils.formatTaskTimeRange(
                    startTime = task.startTime,
                    endTime = task.endTime,
                    reminderHour = task.reminderHour,
                    reminderMinute = task.reminderMinute,
                    durationMinutes = task.targetMinutes,
                    delayMinutes = task.delayMinutes
                ).ifBlank {
                    val hour = (15 + (index * 1.5).toInt()) % 24
                    DateTimeUtils.formatHourMinuteTo12Hour(hour, 0)
                }
                timeline.add(
                    DashboardScheduleItem(
                        id = "task_${task.id}",
                        timeDisplay = timeStr,
                        title = "${task.subject}: ${task.title}",
                        subtitle = "${task.targetMinutes}m • ${task.type.name}" + (if (task.isDelayed) " • Delayed (+${task.delayMinutes}m)" else ""),
                        isCompleted = task.isCompleted,
                        category = "Study"
                    )
                )
            }
        }

        val sleepTime = DateTimeUtils.formatTo12Hour(profile?.sleepTime?.ifBlank { "10:30 PM" } ?: "10:30 PM")
        timeline.add(
            DashboardScheduleItem(
                id = "night_revision",
                timeDisplay = "09:00 PM",
                title = "Evening Formula & Concept Revision",
                subtitle = "Sleep prep before $sleepTime",
                isCompleted = habitsBundle.habits.firstOrNull { it.habit.name.contains("Revision", true) }?.isCompletedToday ?: false,
                category = "Revision"
            )
        )

        // Single high-priority AI Coach recommendation
        val lowestSubject = syllabusBundle.summaries.filter { it.percentage < 100 }.minByOrNull { it.percentage }
        val pendingTasks = statusBundle.tasks.filter { !it.isCompleted }
        val aiRec = when {
            pendingTasks.isNotEmpty() -> {
                val nextTask = pendingTasks.first()
                DashboardAiRecommendation(
                    headline = "${nextTask.subject} is your immediate mission.",
                    recommendation = "Complete \"${nextTask.title}\" (${nextTask.targetMinutes} mins) now to lock in today's discipline.",
                    subject = nextTask.subject,
                    chapter = nextTask.title
                )
            }
            lowestSubject != null -> {
                DashboardAiRecommendation(
                    headline = "${lowestSubject.name} is behind schedule (${lowestSubject.percentage}% done).",
                    recommendation = "Recommended: Complete high-yield chapters & numerical revision today.",
                    subject = lowestSubject.name
                )
            }
            else -> {
                DashboardAiRecommendation(
                    headline = "Today's missions completed!",
                    recommendation = "Maintain your momentum with light 20-min formula recall or evening rest.",
                    subject = "Revision"
                )
            }
        }

        val targetStudy = (profile?.dailyStudyGoalHours?.times(60)?.toInt() ?: 480)

        HomeUiState(
            userProfile = profile,
            winterArcState = dynamicArc,
            boardExamConfig = safeExamConfig,
            disciplineScore = safeDiscipline.copy(totalScore = score),
            schoolStatus = safeSchool,
            todayTasks = statusBundle.tasks,
            daysUntilExam = daysLeft,
            todayStudyMinutes = statusBundle.studyMins,
            targetStudyMinutes = targetStudy,
            completedTasksCount = completedCount,
            totalTasksCount = totalCount,
            progressPercentage = progress,
            realStreak = realStreak,
            winterArcCurrentDay = dayNum,
            winterArcTotalDays = safeWinterArc.targetDays,
            winterArcDaysRemaining = arcDaysLeft,
            readinessScore = readinessScore,
            habits = habitsBundle.habits,
            completedHabitsCount = habitsBundle.completedCount,
            totalHabitsCount = habitsBundle.totalCount,
            scheduleTimeline = timeline,
            subjectSummaries = syllabusBundle.summaries,
            overallSyllabusPercentage = syllabusBundle.overallPct,
            totalSyllabusChapters = syllabusBundle.totalCh,
            completedSyllabusChapters = syllabusBundle.compCh,
            aiRecommendation = aiRec
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = Long.MAX_VALUE),
        initialValue = HomeUiState()
    )

    fun toggleTask(task: DailyPlanTaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun delayTask(task: DailyPlanTaskEntity, additionalMinutes: Int = 15) {
        viewModelScope.launch {
            repository.delayTask(task, additionalMinutes)
        }
    }

    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.toggleHabit(habit)
        }
    }

    fun generateTodayPlan() {
        viewModelScope.launch {
            repository.generateSmartDailyPlan(repository.getTodayDateString())
        }
    }

    fun addNewTask(title: String, subject: String, targetMins: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = DailyPlanTaskEntity(
                date = repository.getTodayDateString(),
                title = title.trim(),
                subject = subject.trim().ifBlank { "General" },
                targetMinutes = targetMins.coerceIn(15, 180),
                type = TaskType.LECTURE
            )
            repository.addTask(task)
        }
    }
}

class HomeViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
