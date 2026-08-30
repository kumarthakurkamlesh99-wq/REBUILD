package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ExerciseType
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.WorkoutLevel
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FitnessUiState(
    val selectedLevel: WorkoutLevel = WorkoutLevel.INTERMEDIATE,
    val todayWorkouts: List<WorkoutLogEntity> = emptyList(),
    val totalWorkoutsCompleted: Int = 18,
    val totalCaloriesBurnedToday: Int = 380,
    val totalRunningKmToday: Float = 3.2f,
    val totalPushupsToday: Int = 45,
    val totalSquatsToday: Int = 60
)

class FitnessViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val _selectedLevel = MutableStateFlow(WorkoutLevel.INTERMEDIATE)

    val uiState: StateFlow<FitnessUiState> = combine(
        _selectedLevel,
        repository.getTodayWorkouts()
    ) { level, workouts ->
        val cals = workouts.filter { it.isCompleted }.sumOf { it.caloriesBurned }
        val runningKm = workouts.filter { it.isCompleted && it.exerciseType == ExerciseType.RUNNING }.sumOf { it.distanceKm.toDouble() }.toFloat()
        val pushups = workouts.filter { it.isCompleted && it.exerciseType == ExerciseType.PUSHUPS }.sumOf { it.sets * it.reps }
        val squats = workouts.filter { it.isCompleted && it.exerciseType == ExerciseType.SQUATS }.sumOf { it.sets * it.reps }

        FitnessUiState(
            selectedLevel = level,
            todayWorkouts = workouts,
            totalWorkoutsCompleted = workouts.count { it.isCompleted },
            totalCaloriesBurnedToday = cals,
            totalRunningKmToday = runningKm,
            totalPushupsToday = pushups,
            totalSquatsToday = squats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FitnessUiState()
    )

    fun selectLevel(level: WorkoutLevel) {
        _selectedLevel.value = level
        viewModelScope.launch {
            repository.generateDailyWorkoutPlan(level)
        }
    }

    fun toggleWorkout(workout: WorkoutLogEntity) = viewModelScope.launch {
        repository.toggleWorkoutCompleted(workout)
    }

    fun addCustomWorkout(
        name: String,
        type: ExerciseType,
        sets: Int,
        reps: Int,
        durationMins: Int,
        distanceKm: Float
    ) = viewModelScope.launch {
        val workout = WorkoutLogEntity(
            date = repository.getTodayDateString(),
            exerciseName = name,
            exerciseType = type,
            level = _selectedLevel.value,
            sets = sets,
            reps = reps,
            durationMinutes = durationMins,
            distanceKm = distanceKm,
            caloriesBurned = (durationMins * 8)
        )
        repository.addWorkout(workout)
    }
}

data class HabitsUiState(
    val habits: List<HabitEntity> = emptyList(),
    val todayLogs: List<HabitLogEntity> = emptyList(),
    val completedCount: Int = 6,
    val totalCount: Int = 8,
    val overallDisciplineScore: Int = 82
)

class HabitsViewModel(private val repository: RebuildRepository) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.getAllHabits(),
        repository.getTodayHabitLogs(),
        repository.getTodayDiscipline()
    ) { habits, logs, discipline ->
        val logMap = logs.associateBy { it.habitId }
        val completedCount = habits.count { logMap[it.id]?.isCompleted == true }

        HabitsUiState(
            habits = habits,
            todayLogs = logs,
            completedCount = completedCount,
            totalCount = habits.size,
            overallDisciplineScore = discipline?.totalScore ?: 82
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    fun toggleHabit(habit: HabitEntity) = viewModelScope.launch {
        repository.toggleHabit(habit)
    }

    fun addHabit(name: String, isNegative: Boolean, weight: Int, unit: String) = viewModelScope.launch {
        val habit = HabitEntity(
            name = name,
            isNegativeHabit = isNegative,
            weight = weight,
            targetUnit = unit,
            iconName = if (isNegative) "shield" else "check_circle",
            isDefault = false,
            colorHex = if (isNegative) "#FF5722" else "#38E1FF"
        )
        repository.addCustomHabit(habit)
    }
}

class FitnessViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HabitsViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
