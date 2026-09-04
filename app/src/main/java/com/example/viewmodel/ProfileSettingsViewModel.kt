package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileSettingsUiState(
    val name: String = "Kamlesh Kumar Thakur",
    val studentClass: String = "Class 12",
    val board: String = "Bihar Board",
    val stream: String = "Science (PCM)",
    val targetPercentage: Int = 95,
    val wakeUpTime: String = "06:00",
    val sleepTime: String = "22:30",
    val avatarUri: String = "",
    val winterArcStartDate: String = "2026-08-01",
    val targetExamDate: String = "2027-02-15",
    val goal: String = "Crack Bihar Board Class 12 with 95%+ and build elite discipline",
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val originalProfile: UserProfileEntity? = null
)

class ProfileSettingsViewModel(
    private val repository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState: StateFlow<ProfileSettingsUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val existing = repository.getUserProfileDirect() ?: UserProfileEntity()
            _uiState.update {
                it.copy(
                    name = existing.name,
                    studentClass = existing.studentClass,
                    board = existing.board,
                    stream = existing.stream,
                    targetPercentage = existing.targetPercentage,
                    wakeUpTime = existing.wakeUpTime,
                    sleepTime = existing.sleepTime,
                    avatarUri = existing.avatarUri,
                    winterArcStartDate = existing.winterArcStartDate,
                    targetExamDate = existing.targetExamDate,
                    goal = existing.goal,
                    originalProfile = existing,
                    isLoading = false
                )
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateStudentClass(c: String) = _uiState.update { it.copy(studentClass = c) }
    fun updateBoard(b: String) = _uiState.update { it.copy(board = b) }
    fun updateStream(s: String) = _uiState.update { it.copy(stream = s) }
    fun updateTargetPercentage(p: Int) = _uiState.update { it.copy(targetPercentage = p.coerceIn(50, 100)) }
    fun updateWakeUpTime(t: String) = _uiState.update { it.copy(wakeUpTime = t) }
    fun updateSleepTime(t: String) = _uiState.update { it.copy(sleepTime = t) }
    fun updateAvatarUri(uri: String) = _uiState.update { it.copy(avatarUri = uri) }
    fun updateWinterArcStartDate(d: String) = _uiState.update { it.copy(winterArcStartDate = d) }
    fun updateTargetExamDate(d: String) = _uiState.update { it.copy(targetExamDate = d) }
    fun updateGoal(g: String) = _uiState.update { it.copy(goal = g) }

    fun saveProfile() {
        val s = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val base = s.originalProfile ?: UserProfileEntity()
            val updated = base.copy(
                name = if (s.name.isBlank()) "Kamlesh Kumar Thakur" else s.name.trim(),
                studentClass = s.studentClass,
                board = s.board,
                stream = s.stream,
                targetPercentage = s.targetPercentage,
                targetExamName = "${s.board} ${s.studentClass} Exam",
                targetExamDate = s.targetExamDate,
                wakeUpTime = s.wakeUpTime,
                sleepTime = s.sleepTime,
                avatarUri = s.avatarUri,
                winterArcStartDate = s.winterArcStartDate,
                goal = s.goal.trim(),
                updatedAtTimestamp = System.currentTimeMillis()
            )

            repository.saveUserProfile(updated)
            repository.syncWinterArcCalculations()
            _uiState.update { it.copy(isSaving = false, originalProfile = updated) }
            _saveSuccessEvent.emit(Unit)
        }
    }
}

class ProfileSettingsViewModelFactory(
    private val repository: RebuildRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileSettingsViewModel::class.java)) {
            return ProfileSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
