package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.GoalEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalsUiState(
    val activeGoals: List<GoalEntity> = emptyList(),
    val completedGoals: List<GoalEntity> = emptyList(),
    val allGoals: List<GoalEntity> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val overallProgress: Int = 0,
    val selectedCategory: GoalCategory? = null
)

class GoalsViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<GoalCategory?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        repository.getAllGoals(),
        _selectedCategory
    ) { allGoals, categoryFilter ->
        val filtered = if (categoryFilter != null) {
            allGoals.filter { it.category == categoryFilter }
        } else {
            allGoals
        }

        val active = filtered.filter { !it.isCompleted }
        val completed = filtered.filter { it.isCompleted }
        val total = allGoals.size
        val done = allGoals.count { it.isCompleted }
        val overall = if (total > 0) {
            (allGoals.sumOf { it.progressPercentage } / total)
        } else 0

        GoalsUiState(
            activeGoals = active,
            completedGoals = completed,
            allGoals = allGoals,
            totalCount = total,
            completedCount = done,
            activeCount = total - done,
            overallProgress = overall,
            selectedCategory = categoryFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = Long.MAX_VALUE),
        initialValue = GoalsUiState()
    )

    fun setCategoryFilter(category: GoalCategory?) {
        _selectedCategory.value = category
    }

    fun addGoal(
        title: String,
        description: String,
        category: GoalCategory,
        targetDate: String?,
        reminderEnabled: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?
    ) = viewModelScope.launch {
        val goal = GoalEntity(
            title = title,
            description = description,
            category = category,
            targetDate = targetDate,
            reminderEnabled = reminderEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
        repository.createGoal(goal)
    }

    fun updateGoal(goal: GoalEntity) = viewModelScope.launch {
        repository.updateGoal(goal)
    }

    fun toggleGoal(goal: GoalEntity) = viewModelScope.launch {
        repository.toggleGoalCompleted(goal)
    }

    fun updateProgress(goal: GoalEntity, progress: Int) = viewModelScope.launch {
        repository.updateGoalProgress(goal, progress)
    }

    fun deleteGoal(goal: GoalEntity) = viewModelScope.launch {
        repository.deleteGoal(goal)
    }
}

class GoalsViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
