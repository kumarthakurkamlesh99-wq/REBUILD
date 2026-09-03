package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.model.DailyStudyPoint
import com.example.data.model.ExecutiveReportData
import com.example.data.model.ReportPeriod
import com.example.data.model.SubjectProgressItem
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AnalyticsUiState(
    val studySessions: List<StudySessionEntity> = emptyList(),
    val disciplineTrend: List<DailyDisciplineEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val workouts: List<WorkoutLogEntity> = emptyList(),
    val schoolLogs: List<SchoolStatusEntity> = emptyList(),
    val weeklyStudyHours: Float = 0f,
    val monthlyStudyHours: Float = 0f,
    val averageDisciplineScore: Int = 0,
    val habitCompletionRate: Int = 0,
    val attendanceRate: Int = 0,
    val selectedPeriod: ReportPeriod = ReportPeriod.WEEKLY,
    val executiveReport: ExecutiveReportData? = null,
    val dailyStudyGraph: List<DailyStudyPoint> = emptyList(),
    val subjectProgressList: List<SubjectProgressItem> = emptyList()
)

private data class AnalyticsMetrics(
    val sessions: List<StudySessionEntity>,
    val discipline: List<DailyDisciplineEntity>,
    val subjects: List<SubjectEntity>,
    val habits: List<HabitEntity>,
    val workouts: List<WorkoutLogEntity>,
    val schoolLogs: List<SchoolStatusEntity>,
    val weeklyStudyHours: Float,
    val monthlyStudyHours: Float,
    val averageDisciplineScore: Int,
    val habitCompletionRate: Int,
    val attendanceRate: Int
)

class AnalyticsViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val selectedPeriodFlow = MutableStateFlow(ReportPeriod.WEEKLY)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val executiveReportFlow = selectedPeriodFlow.flatMapLatest { period ->
        repository.getExecutiveReport(period)
    }

    private val studyAndDisciplineFlow = combine(
        repository.getAllStudySessions(),
        repository.getDisciplineTrends()
    ) { sessions, discipline -> Pair(sessions, discipline) }

    private val subjectsAndHabitsFlow = combine(
        repository.getAllSubjects(),
        repository.getAllHabits()
    ) { subjects, habits -> Pair(subjects, habits) }

    private val metricsFlow = combine(
        studyAndDisciplineFlow,
        subjectsAndHabitsFlow,
        repository.getAllWorkouts(),
        repository.getAllSchoolLogs(),
        repository.getAllHabitLogs()
    ) { (sessions, discipline), (subjects, habits), workouts, schoolLogs, habitLogs ->
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(cal.time)

        val weekCal = cal.clone() as Calendar
        weekCal.add(Calendar.DAY_OF_YEAR, -6)
        val weekStartStr = dateFormat.format(weekCal.time)

        val monthCal = cal.clone() as Calendar
        monthCal.add(Calendar.DAY_OF_YEAR, -29)
        val monthStartStr = dateFormat.format(monthCal.time)

        val weeklyMins = sessions.filter { it.date in weekStartStr..todayStr }.sumOf { it.durationMinutes }
        val weeklyHours = (weeklyMins / 60f * 10).toInt() / 10f

        val monthlyMins = sessions.filter { it.date in monthStartStr..todayStr }.sumOf { it.durationMinutes }
        val monthlyHours = (monthlyMins / 60f * 10).toInt() / 10f

        val avgDiscipline = if (discipline.isNotEmpty()) {
            discipline.map { it.totalScore }.average().toInt()
        } else {
            0
        }

        val recentHabitLogs = habitLogs.filter { it.date in weekStartStr..todayStr }
        val habitRate = if (recentHabitLogs.isNotEmpty()) {
            val done = recentHabitLogs.count { it.isCompleted }
            ((done.toFloat() / recentHabitLogs.size) * 100).toInt()
        } else {
            0
        }

        val presentDays = schoolLogs.count { it.isPresent }
        val totalSchoolDays = schoolLogs.size
        val attRate = if (totalSchoolDays > 0) ((presentDays.toFloat() / totalSchoolDays) * 100).toInt() else 0

        AnalyticsMetrics(
            sessions = sessions,
            discipline = discipline,
            subjects = subjects,
            habits = habits,
            workouts = workouts,
            schoolLogs = schoolLogs,
            weeklyStudyHours = weeklyHours,
            monthlyStudyHours = monthlyHours,
            averageDisciplineScore = avgDiscipline,
            habitCompletionRate = habitRate,
            attendanceRate = attRate
        )
    }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        metricsFlow,
        selectedPeriodFlow,
        executiveReportFlow,
        repository.getDailyStudyGraph(),
        repository.getSubjectProgressList()
    ) { metrics, period, report, dailyGraph, subjectProgress ->
        AnalyticsUiState(
            studySessions = metrics.sessions,
            disciplineTrend = metrics.discipline,
            subjects = metrics.subjects,
            habits = metrics.habits,
            workouts = metrics.workouts,
            schoolLogs = metrics.schoolLogs,
            weeklyStudyHours = metrics.weeklyStudyHours,
            monthlyStudyHours = metrics.monthlyStudyHours,
            averageDisciplineScore = metrics.averageDisciplineScore,
            habitCompletionRate = metrics.habitCompletionRate,
            attendanceRate = metrics.attendanceRate,
            selectedPeriod = period,
            executiveReport = report,
            dailyStudyGraph = dailyGraph,
            subjectProgressList = subjectProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun selectReportPeriod(period: ReportPeriod) {
        selectedPeriodFlow.value = period
    }
}

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val userName: String = "Disciple",
    val dailyGoalHours: String = "6"
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
) : ViewModel() {

    private val audioSettingsFlow = combine(
        userPreferencesRepository.isNotificationsEnabled,
        userPreferencesRepository.isSoundEnabled,
        userPreferencesRepository.isVibrationEnabled
    ) { notifs, sound, vib -> Triple(notifs, sound, vib) }

    private val profileSettingsFlow = combine(
        userPreferencesRepository.userName,
        userPreferencesRepository.dailyGoalHours
    ) { name, hours -> Pair(name, hours) }

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.isDarkTheme,
        audioSettingsFlow,
        profileSettingsFlow
    ) { dark, (notifs, sound, vib), (name, hours) ->
        SettingsUiState(
            isDarkTheme = dark,
            isNotificationsEnabled = notifs,
            isSoundEnabled = sound,
            isVibrationEnabled = vib,
            userName = name,
            dailyGoalHours = hours
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleDarkTheme(enabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setDarkTheme(enabled)
    }

    fun toggleNotifications(enabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setNotificationsEnabled(enabled)
        if (enabled) {
            AlarmScheduler.scheduleAllDefaultAlarms(context)
        }
    }

    fun toggleSound(enabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setSoundEnabled(enabled)
    }

    fun toggleVibration(enabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setVibrationEnabled(enabled)
    }

    fun updateName(name: String) = viewModelScope.launch {
        userPreferencesRepository.setUserName(name)
    }

    fun updateGoalHours(hours: String) = viewModelScope.launch {
        userPreferencesRepository.setDailyGoalHours(hours)
    }

    fun resyncAlarms() {
        AlarmScheduler.scheduleAllDefaultAlarms(context)
    }
}

class AnalyticsViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userPreferencesRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
