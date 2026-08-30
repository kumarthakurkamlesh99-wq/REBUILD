package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val studySessions: List<StudySessionEntity> = emptyList(),
    val disciplineTrend: List<DailyDisciplineEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val workouts: List<WorkoutLogEntity> = emptyList(),
    val schoolLogs: List<SchoolStatusEntity> = emptyList(),
    val weeklyStudyHours: Float = 34.5f,
    val monthlyStudyHours: Float = 142.0f,
    val averageDisciplineScore: Int = 84,
    val habitCompletionRate: Int = 88,
    val attendanceRate: Int = 92
)

class AnalyticsViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val studyAndDisciplineFlow = combine(
        repository.getAllStudySessions(),
        repository.getDisciplineTrends()
    ) { sessions, discipline -> Pair(sessions, discipline) }

    private val subjectsAndHabitsFlow = combine(
        repository.getAllSubjects(),
        repository.getAllHabits()
    ) { subjects, habits -> Pair(subjects, habits) }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        studyAndDisciplineFlow,
        subjectsAndHabitsFlow,
        repository.getAllWorkouts(),
        repository.getAllSchoolLogs()
    ) { (sessions, discipline), (subjects, habits), workouts, schoolLogs ->
        val totalMins = sessions.sumOf { it.durationMinutes }
        val avgDiscipline = if (discipline.isNotEmpty()) discipline.map { it.totalScore }.average().toInt() else 82
        val presentDays = schoolLogs.count { it.isPresent }
        val totalSchoolDays = schoolLogs.size.coerceAtLeast(1)
        val attRate = ((presentDays.toFloat() / totalSchoolDays) * 100).toInt()

        AnalyticsUiState(
            studySessions = sessions,
            disciplineTrend = discipline,
            subjects = subjects,
            habits = habits,
            workouts = workouts,
            schoolLogs = schoolLogs,
            weeklyStudyHours = 34.5f,
            monthlyStudyHours = (totalMins.toFloat() / 60.0f).coerceAtLeast(42f),
            averageDisciplineScore = avgDiscipline,
            habitCompletionRate = 88,
            attendanceRate = attRate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
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
