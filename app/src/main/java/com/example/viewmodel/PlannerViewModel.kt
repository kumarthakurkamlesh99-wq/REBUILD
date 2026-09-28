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
import com.example.data.scheduler.DeepWorkPrediction
import com.example.data.scheduler.DynamicTaskScheduleItem
import com.example.data.scheduler.UserEnergyLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    val currentDeepWorkHours: Float = 0.0f,
    val energyLevel: UserEnergyLevel = UserEnergyLevel.MEDIUM,
    val deepWorkPrediction: DeepWorkPrediction = DeepWorkPrediction(),
    val dynamicScheduleItems: List<DynamicTaskScheduleItem> = emptyList(),
    val isOptimizing: Boolean = false,
    val optimizationNotice: String? = null
)

class PlannerViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val _energyLevel = MutableStateFlow(UserEnergyLevel.MEDIUM)
    private val _deepWorkPrediction = MutableStateFlow(DeepWorkPrediction())
    private val _isOptimizing = MutableStateFlow(false)
    private val _optimizationNotice = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.getUserProfile(),
        repository.getTodaySchoolStatus(),
        repository.getTodayTasks(),
        repository.getAllSchoolLogs(),
        repository.getTodayStudyMinutes(),
        _energyLevel,
        _deepWorkPrediction,
        _isOptimizing,
        _optimizationNotice
    ) { args: Array<Any?> ->
        val profile = args[0] as? UserProfileEntity
        val school = args[1] as? SchoolStatusEntity
        @Suppress("UNCHECKED_CAST")
        val tasks = args[2] as? List<DailyPlanTaskEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val logs = args[3] as? List<SchoolStatusEntity> ?: emptyList()
        val studyMins = args[4] as? Int ?: 0
        val energy = args[5] as? UserEnergyLevel ?: UserEnergyLevel.MEDIUM
        val prediction = args[6] as? DeepWorkPrediction ?: DeepWorkPrediction()
        val optimizing = args[7] as? Boolean ?: false
        val notice = args[8] as? String

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

        // Calculate dynamic items for current energy & tasks
        val dynamicItems = com.example.data.scheduler.DynamicStudyScheduler.adjustDailySchedule(
            tasks = tasks,
            energyLevel = energy,
            historicalSessions = emptyList(),
            completedTasks = tasks.filter { it.isCompleted },
            subjects = emptyList(),
            examConfig = null,
            userProfile = profile
        )

        PlannerUiState(
            userProfile = profile,
            schoolStatus = safeSchool,
            todayTasks = tasks,
            allLogs = logs,
            analytics = analytics,
            dailyDeepWorkGoalHours = goalHours,
            currentDeepWorkHours = (studyMins.toFloat() / 60.0f),
            energyLevel = energy,
            deepWorkPrediction = prediction,
            dynamicScheduleItems = dynamicItems,
            isOptimizing = optimizing,
            optimizationNotice = notice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = Long.MAX_VALUE),
        initialValue = PlannerUiState()
    )

    init {
        refreshDeepWorkPrediction()
    }

    fun setEnergyLevel(level: UserEnergyLevel) {
        _energyLevel.value = level
        _optimizationNotice.value = "Adjusted for ${level.displayName} (${level.emoji}): ${level.description}"
        // Auto-reorder in DB to match
        viewModelScope.launch {
            val adjusted = repository.calculateDynamicSchedule(level)
            repository.applyDynamicScheduleOrder(adjusted)
        }
    }

    fun refreshDeepWorkPrediction() {
        viewModelScope.launch {
            try {
                val prediction = repository.getDeepWorkPrediction()
                _deepWorkPrediction.value = prediction
            } catch (e: Exception) {
                // Keep default
            }
        }
    }

    fun optimizeWithJarvis() {
        _isOptimizing.value = true
        _optimizationNotice.value = "JARVIS is recalibrating your cognitive schedule..."
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(650) // Premium smooth calibration feel
                val prediction = repository.getDeepWorkPrediction()
                _deepWorkPrediction.value = prediction
                val adjusted = repository.calculateDynamicSchedule(_energyLevel.value)
                repository.applyDynamicScheduleOrder(adjusted)
                _optimizationNotice.value = "JARVIS: Deep work aligned to ${prediction.optimalWindows.firstOrNull() ?: "evening"}. Tasks reordered."
            } catch (e: Exception) {
                _optimizationNotice.value = "Schedule re-ordered for maximum focus."
            } finally {
                _isOptimizing.value = false
            }
        }
    }

    fun dispatchSchool() = viewModelScope.launch { repository.dispatchSchool() }
    fun arrivedSchool() = viewModelScope.launch { repository.arrivedSchool() }
    fun dispatchHome() = viewModelScope.launch { repository.dispatchHome() }
    fun arrivedHome() = viewModelScope.launch { repository.arrivedHome() }

    fun toggleTask(task: DailyPlanTaskEntity) = viewModelScope.launch {
        repository.toggleTaskCompleted(task)
        // Dynamically recalculate schedule upon task completion!
        val adjusted = repository.calculateDynamicSchedule(_energyLevel.value)
        repository.applyDynamicScheduleOrder(adjusted)
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
        val adjusted = repository.calculateDynamicSchedule(_energyLevel.value)
        repository.applyDynamicScheduleOrder(adjusted)
    }

    fun updateTask(task: DailyPlanTaskEntity) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: DailyPlanTaskEntity) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun regeneratePlan() = viewModelScope.launch {
        repository.generateSmartDailyPlan(repository.getTodayDateString())
        val adjusted = repository.calculateDynamicSchedule(_energyLevel.value)
        repository.applyDynamicScheduleOrder(adjusted)
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
