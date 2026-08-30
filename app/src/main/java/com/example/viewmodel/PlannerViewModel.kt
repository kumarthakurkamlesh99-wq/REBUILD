package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.TaskType
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SchoolAnalyticsState(
    val totalSchoolDays: Int = 24,
    val presentDays: Int = 22,
    val absentDays: Int = 2,
    val avgArrivalTime: String = "07:42 AM",
    val avgReturnTime: String = "02:15 PM",
    val avgTravelTimeMinutes: Int = 28
)

data class PlannerUiState(
    val schoolStatus: SchoolStatusEntity = SchoolStatusEntity(date = ""),
    val todayTasks: List<DailyPlanTaskEntity> = emptyList(),
    val allLogs: List<SchoolStatusEntity> = emptyList(),
    val analytics: SchoolAnalyticsState = SchoolAnalyticsState(),
    val dailyDeepWorkGoalHours: Float = 6.0f,
    val currentDeepWorkHours: Float = 3.5f
)

class PlannerViewModel(private val repository: RebuildRepository) : ViewModel() {

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.getTodaySchoolStatus(),
        repository.getTodayTasks(),
        repository.getAllSchoolLogs(),
        repository.getTodayStudyMinutes()
    ) { school, tasks, logs, studyMins ->
        val safeSchool = school ?: SchoolStatusEntity(date = repository.getTodayDateString())
        val presentCount = logs.count { it.isPresent }
        val absentCount = logs.count { !it.isPresent && !it.isHoliday }
        val totalDays = logs.size.coerceAtLeast(1)

        val analytics = SchoolAnalyticsState(
            totalSchoolDays = totalDays,
            presentDays = presentCount.coerceAtLeast(1),
            absentDays = absentCount,
            avgArrivalTime = "07:45 AM",
            avgReturnTime = "02:20 PM",
            avgTravelTimeMinutes = 26
        )

        PlannerUiState(
            schoolStatus = safeSchool,
            todayTasks = tasks,
            allLogs = logs,
            analytics = analytics,
            dailyDeepWorkGoalHours = 6.0f,
            currentDeepWorkHours = (studyMins.toFloat() / 60.0f)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
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
        details: String
    ) = viewModelScope.launch {
        val task = DailyPlanTaskEntity(
            date = repository.getTodayDateString(),
            subject = subject,
            title = title,
            type = type,
            details = details,
            targetMinutes = targetMins,
            xpReward = targetMins
        )
        repository.addTask(task)
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
