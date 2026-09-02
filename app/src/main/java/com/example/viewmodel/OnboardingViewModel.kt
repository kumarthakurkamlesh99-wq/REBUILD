package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 5,
    val name: String = "",
    val studentClass: String = "Class 12",
    val board: String = "CBSE",
    val stream: String = "Science (PCM)",
    val targetPercentage: Int = 95,
    val targetExamName: String = "CBSE Class 12 Board Exam 2027",
    val targetExamDate: String = "2027-02-15",
    val hasSchool: Boolean = true,
    val schoolStartTime: String = "09:45",
    val schoolEndTime: String = "13:00",
    val travelTimeMinutes: Int = 25,
    val weeklyOffDays: String = "Sunday",
    val wakeUpTime: String = "06:00",
    val sleepTime: String = "22:30",
    val dailyStudyGoalHours: Float = 6.0f,
    val preferredSessionDurationMinutes: Int = 50,
    val workoutType: String = "Calisthenics",
    val workoutTime: String = "17:00",
    val workoutDurationMinutes: Int = 30,
    val coachingStyle: String = "Monk Mode (Strict Discipline)",
    val geminiApiKey: String = "",
    // Notifications toggles
    val notifyWakeUp: Boolean = true,
    val notifySchoolDeparture: Boolean = true,
    val notifySchoolArrival: Boolean = true,
    val notifyReturnHome: Boolean = true,
    val notifyStudySessions: Boolean = true,
    val notifyWorkout: Boolean = true,
    val notifyRevision: Boolean = true,
    val notifyReflection: Boolean = true,
    val notifySleep: Boolean = true,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val repository: RebuildRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = repository.getUserProfileDirect()
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        name = existing.name,
                        studentClass = existing.studentClass,
                        board = existing.board,
                        stream = existing.stream,
                        targetPercentage = existing.targetPercentage,
                        targetExamName = existing.targetExamName,
                        targetExamDate = existing.targetExamDate,
                        hasSchool = existing.hasSchool,
                        schoolStartTime = existing.schoolStartTime,
                        schoolEndTime = existing.schoolEndTime,
                        travelTimeMinutes = existing.travelTimeMinutes,
                        weeklyOffDays = if (existing.weeklyOffDaysJson.contains("Sunday")) "Sunday" else "None",
                        wakeUpTime = existing.wakeUpTime,
                        sleepTime = existing.sleepTime,
                        dailyStudyGoalHours = existing.dailyStudyGoalHours,
                        preferredSessionDurationMinutes = existing.preferredSessionDurationMinutes,
                        workoutType = existing.workoutType,
                        workoutTime = existing.workoutTime,
                        workoutDurationMinutes = existing.workoutDurationMinutes,
                        coachingStyle = existing.coachingStyle,
                        geminiApiKey = existing.geminiApiKey,
                        notifyWakeUp = existing.notifyWakeUp,
                        notifySchoolDeparture = existing.notifySchoolDeparture,
                        notifySchoolArrival = existing.notifySchoolArrival,
                        notifyReturnHome = existing.notifyReturnHome,
                        notifyStudySessions = existing.notifyStudySessions,
                        notifyWorkout = existing.notifyWorkout,
                        notifyRevision = existing.notifyRevision,
                        notifyReflection = existing.notifyReflection,
                        notifySleep = existing.notifySleep,
                        isCompleted = existing.isCompleted
                    )
                }
            }
        }
    }

    fun nextStep() {
        if (_uiState.value.currentStep < _uiState.value.totalSteps) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun setStep(step: Int) {
        if (step in 1.._uiState.value.totalSteps) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateStudentClass(cls: String) = _uiState.update { it.copy(studentClass = cls) }
    fun updateBoard(board: String) = _uiState.update { it.copy(board = board) }
    fun updateStream(stream: String) = _uiState.update { it.copy(stream = stream) }
    fun updateTargetPercentage(perc: Int) = _uiState.update { it.copy(targetPercentage = perc) }
    fun updateTargetExamName(name: String) = _uiState.update { it.copy(targetExamName = name) }
    fun updateTargetExamDate(date: String) = _uiState.update { it.copy(targetExamDate = date) }

    fun updateHasSchool(has: Boolean) = _uiState.update { it.copy(hasSchool = has) }
    fun updateSchoolStartTime(time: String) = _uiState.update { it.copy(schoolStartTime = time) }
    fun updateSchoolEndTime(time: String) = _uiState.update { it.copy(schoolEndTime = time) }
    fun updateTravelTime(mins: Int) = _uiState.update { it.copy(travelTimeMinutes = mins) }
    fun updateWeeklyOffDays(days: String) = _uiState.update { it.copy(weeklyOffDays = days) }

    fun updateWakeUpTime(time: String) = _uiState.update { it.copy(wakeUpTime = time) }
    fun updateSleepTime(time: String) = _uiState.update { it.copy(sleepTime = time) }
    fun updateDailyStudyGoalHours(hours: Float) = _uiState.update { it.copy(dailyStudyGoalHours = hours) }
    fun updateSessionDuration(mins: Int) = _uiState.update { it.copy(preferredSessionDurationMinutes = mins) }

    fun updateWorkoutType(type: String) = _uiState.update { it.copy(workoutType = type) }
    fun updateWorkoutTime(time: String) = _uiState.update { it.copy(workoutTime = time) }
    fun updateWorkoutDuration(mins: Int) = _uiState.update { it.copy(workoutDurationMinutes = mins) }
    fun updateCoachingStyle(style: String) = _uiState.update { it.copy(coachingStyle = style) }
    fun updateGeminiApiKey(key: String) = _uiState.update { it.copy(geminiApiKey = key) }

    fun toggleNotifyWakeUp() = _uiState.update { it.copy(notifyWakeUp = !it.notifyWakeUp) }
    fun toggleNotifySchoolDeparture() = _uiState.update { it.copy(notifySchoolDeparture = !it.notifySchoolDeparture) }
    fun toggleNotifySchoolArrival() = _uiState.update { it.copy(notifySchoolArrival = !it.notifySchoolArrival) }
    fun toggleNotifyReturnHome() = _uiState.update { it.copy(notifyReturnHome = !it.notifyReturnHome) }
    fun toggleNotifyStudySessions() = _uiState.update { it.copy(notifyStudySessions = !it.notifyStudySessions) }
    fun toggleNotifyWorkout() = _uiState.update { it.copy(notifyWorkout = !it.notifyWorkout) }
    fun toggleNotifyRevision() = _uiState.update { it.copy(notifyRevision = !it.notifyRevision) }
    fun toggleNotifyReflection() = _uiState.update { it.copy(notifyReflection = !it.notifyReflection) }
    fun toggleNotifySleep() = _uiState.update { it.copy(notifySleep = !it.notifySleep) }

    fun completeOnboarding(onSuccess: () -> Unit) {
        val s = _uiState.value
        val entity = UserProfileEntity(
            name = if (s.name.isBlank()) "Student" else s.name.trim(),
            studentClass = s.studentClass,
            board = s.board,
            stream = s.stream,
            targetPercentage = s.targetPercentage,
            targetExamName = s.targetExamName,
            targetExamDate = s.targetExamDate,
            hasSchool = s.hasSchool,
            schoolStartTime = s.schoolStartTime,
            schoolEndTime = s.schoolEndTime,
            travelTimeMinutes = s.travelTimeMinutes,
            weeklyOffDaysJson = "[\"${s.weeklyOffDays}\"]",
            wakeUpTime = s.wakeUpTime,
            sleepTime = s.sleepTime,
            dailyStudyGoalHours = s.dailyStudyGoalHours,
            preferredSessionDurationMinutes = s.preferredSessionDurationMinutes,
            workoutType = s.workoutType,
            workoutTime = s.workoutTime,
            workoutDurationMinutes = s.workoutDurationMinutes,
            coachingStyle = s.coachingStyle,
            geminiApiKey = s.geminiApiKey.trim(),
            notifyWakeUp = s.notifyWakeUp,
            notifySchoolDeparture = s.notifySchoolDeparture,
            notifySchoolArrival = s.notifySchoolArrival,
            notifyReturnHome = s.notifyReturnHome,
            notifyStudySessions = s.notifyStudySessions,
            notifyWorkout = s.notifyWorkout,
            notifyRevision = s.notifyRevision,
            notifyReflection = s.notifyReflection,
            notifySleep = s.notifySleep,
            isCompleted = true,
            createdAtTimestamp = System.currentTimeMillis(),
            updatedAtTimestamp = System.currentTimeMillis()
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.initializeUserSystem(entity)
            userPreferencesRepository.setOnboardingCompleted(true)
            userPreferencesRepository.setUserName(entity.name)
            userPreferencesRepository.setDailyGoalHours(entity.dailyStudyGoalHours.toInt().toString())
            if (entity.geminiApiKey.isNotBlank()) {
                userPreferencesRepository.setGeminiApiKey(entity.geminiApiKey)
            }
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
            onSuccess()
        }
    }
}

class OnboardingViewModelFactory(
    private val repository: RebuildRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
