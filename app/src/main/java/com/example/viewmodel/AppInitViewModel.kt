package com.example.viewmodel

import android.util.Log
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

    companion object {
        private const val TAG = "Splash"
    }

    // Source of truth: initialize immediately from persistent storage
    private val _startupState = MutableStateFlow<AppStartupState>(
        if (userPreferencesRepository.isOnboardingCompletedSync()) {
            AppStartupState.Ready
        } else {
            AppStartupState.NeedsOnboarding
        }
    )
    val startupState: StateFlow<AppStartupState> = _startupState.asStateFlow()

    init {
        checkStartupState()
    }

    fun checkStartupState() {
        viewModelScope.launch {
            Log.d(TAG, "Splash → Checking onboarding state")
            try {
                // Use single persistent onboarding flag as the source of truth.
                // No hardcoded onboarding bypasses.
                val isCompleted = userPreferencesRepository.isOnboardingCompleted.first()
                Log.d(TAG, "Onboarding Completed = $isCompleted")

                val target = if (isCompleted) "Dashboard" else "Onboarding"
                Log.d(TAG, "Navigation Target = $target")

                _startupState.value = if (isCompleted) {
                    AppStartupState.Ready
                } else {
                    AppStartupState.NeedsOnboarding
                }
            } catch (e: Exception) {
                // Synchronous fallback to SharedPreferences persistent flag
                val isCompleted = userPreferencesRepository.isOnboardingCompletedSync()
                Log.d(TAG, "Onboarding Completed = $isCompleted")

                val target = if (isCompleted) "Dashboard" else "Onboarding"
                Log.d(TAG, "Navigation Target = $target")

                _startupState.value = if (isCompleted) {
                    AppStartupState.Ready
                } else {
                    AppStartupState.NeedsOnboarding
                }
            }
        }
    }

    fun onOnboardingFinished() {
        Log.d(TAG, "Onboarding Completed = true")
        Log.d(TAG, "Navigation Target = Dashboard")
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
            _startupState.value = AppStartupState.Ready
        }
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
