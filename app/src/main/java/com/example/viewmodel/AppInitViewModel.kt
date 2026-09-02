package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AppStartupState {
    object Loading : AppStartupState
    object NeedsOnboarding : AppStartupState
    object Ready : AppStartupState
}

class AppInitViewModel(
    private val repository: RebuildRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _startupState = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    val startupState: StateFlow<AppStartupState> = _startupState.asStateFlow()

    init {
        checkStartupState()
    }

    fun checkStartupState() {
        viewModelScope.launch {
            try {
                // Check both DataStore and Room DB for rock-solid consistency
                val dataStoreCompleted = userPreferencesRepository.isOnboardingCompleted.first()
                val profileDirect = repository.getUserProfileDirect()
                val dbCompleted = profileDirect != null && profileDirect.isCompleted

                val isOnboarded = dataStoreCompleted || dbCompleted

                // If DB is completed but DataStore was out-of-sync, sync it immediately
                if (dbCompleted && !dataStoreCompleted) {
                    userPreferencesRepository.setOnboardingCompleted(true)
                }

                _startupState.value = if (isOnboarded) {
                    AppStartupState.Ready
                } else {
                    AppStartupState.NeedsOnboarding
                }
            } catch (e: Exception) {
                // Fallback direct check
                val profileDirect = repository.getUserProfileDirect()
                if (profileDirect != null && profileDirect.isCompleted) {
                    _startupState.value = AppStartupState.Ready
                } else {
                    _startupState.value = AppStartupState.NeedsOnboarding
                }
            }
        }
    }

    fun onOnboardingFinished() {
        _startupState.value = AppStartupState.Ready
    }
}

class AppInitViewModelFactory(
    private val repository: RebuildRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppInitViewModel::class.java)) {
            return AppInitViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
