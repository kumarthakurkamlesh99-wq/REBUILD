package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SchoolAnalyticsState(
    val totalSchoolDays: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val avgArrivalTime: String = "09:45 AM",
    val avgReturnTime: String = "01:00 PM",
    val avgTravelTimeMinutes: Int = 25
)

data class PlannerUiState(
    val userProfile: UserProfileEntity? = null,
    val schoolStatus: SchoolStatusEntity = SchoolStatusEntity(date = ""),
    val todayTasks: List<DailyPlanTaskEntity> = emptyList(),
    val allLogs: List<SchoolStatusEntity> = emptyList(),
    val analytics: SchoolAnalyticsState = SchoolAnalyticsState(),
    val dailyDeepWorkGoalHours: Float = 6.0f,
    val currentDeepWorkHours: Float = 0.0f
)

class PlannerViewModel(private val repository: RebuildRepository) : ViewModel() {

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.getUserProfile(),
        repository.getTodaySchoolStatus(),
        repository.getTodayTasks(),
        repository.getAllSchoolLogs(),
        repository.getTodayStudyMinutes()
    ) { profile, school, tasks, logs, studyMins ->
        val safeSchool = school ?: SchoolStatusEntity(date = repository.getTodayDateString())
        val presentCount = logs.count { it.isPresent }
        val absentCount = logs.count { !it.isPresent && !it.isHoliday }
        val totalDays = logs.size

        val goalHours = profile?.dailyStudyGoalHours ?: 6.0f
        val schoolArrival = profile?.schoolStartTime ?: "09:45"
        val schoolReturn = profile?.schoolEndTime ?: "13:00"
        val travelMins = profile?.travelTimeMinutes ?: 25

        val analytics = SchoolAnalyticsState(
            totalSchoolDays = totalDays,
            presentDays = presentCount,
            absentDays = absentCount,
            avgArrivalTime = "$schoolArrival AM",
            avgReturnTime = "$schoolReturn PM",
            avgTravelTimeMinutes = travelMins
        )

        PlannerUiState(
            userProfile = profile,
            schoolStatus = safeSchool,
            todayTasks = tasks,
            allLogs = logs,
            analytics = analytics,
            dailyDeepWorkGoalHours = goalHours,
            currentDeepWorkHours = (studyMins.toFloat() / 60.0f)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = Long.MAX_VALUE),
        initialValue = PlannerUiState()
    )

    fun dispatchSchool() = viewModelScope.launch { repository.dispatchSchool() }
    fun arrivedSchool() = viewModelScope.launch { repository.arrivedSchool() }
    fun dispatchHome() = viewModelScope.launch { repository.dispatchHome() }
    fun arrivedHome() = viewModelScope.launch { repository.arrivedHome() }

    fun toggleTask(task: DailyPlanTaskEntity) = viewModelScope.launch {
        repository.toggleTaskCompleted(task)
    }

    fun addNewTask(
        subject: String,
        title: String,
        type: TaskType,
        targetMins: Int,
        details: String,
        reminderHour: Int? = null,
        reminderMinute: Int? = null
    ) = viewModelScope.launch {
        val task = DailyPlanTaskEntity(
            date = repository.getTodayDateString(),
            subject = subject,
            title = title,
            type = type,
            details = details,
            targetMinutes = targetMins,
            xpReward = targetMins,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
        repository.addTask(task)
    }

    fun updateTask(task: DailyPlanTaskEntity) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: DailyPlanTaskEntity) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun regeneratePlan() = viewModelScope.launch {
        repository.generateSmartDailyPlan(repository.getTodayDateString())
    }
}

class PlannerViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
