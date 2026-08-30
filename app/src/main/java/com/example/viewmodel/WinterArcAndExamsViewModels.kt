package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.HolidayEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

data class WinterArcUiState(
    val state: WinterArcStateEntity = WinterArcStateEntity(),
    val daysRemaining: Int = 63,
    val progressPercentage: Int = 30,
    val rankTitle: String = "Frost Vanguard",
    val recentScores: List<DailyDisciplineEntity> = emptyList()
)

class WinterArcViewModel(private val repository: RebuildRepository) : ViewModel() {

    val uiState: StateFlow<WinterArcUiState> = combine(
        repository.getWinterArcState(),
        repository.getDisciplineTrends()
    ) { arc, trends ->
        val safeArc = arc ?: WinterArcStateEntity()
        val remaining = max(0, safeArc.targetDays - safeArc.currentDay)
        val perc = ((safeArc.currentDay.toFloat() / safeArc.targetDays) * 100).toInt()

        val rank = when {
            safeArc.level >= 25 -> "Apex Ascendant"
            safeArc.level >= 18 -> "Glacier Sovereign"
            safeArc.level >= 12 -> "Frost Vanguard"
            safeArc.level >= 6 -> "Iron Disciple"
            else -> "Novice Arc"
        }

        WinterArcUiState(
            state = safeArc,
            daysRemaining = remaining,
            progressPercentage = perc,
            rankTitle = rank,
            recentScores = trends
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WinterArcUiState()
    )

    fun updateDayAndStreak(day: Int, streak: Int) = viewModelScope.launch {
        repository.updateWinterArcDay(day, streak)
    }
}

data class BoardExamUiState(
    val config: BoardExamConfigEntity = BoardExamConfigEntity(),
    val remainingDays: Long = 148,
    val remainingChapters: Int = 32,
    val completionPercentage: Int = 54,
    val requiredDailyChaptersRate: Float = 0.22f,
    val isAheadOfSchedule: Boolean = true,
    val holidays: List<HolidayEntity> = emptyList()
)

class BoardExamViewModel(private val repository: RebuildRepository) : ViewModel() {

    val uiState: StateFlow<BoardExamUiState> = combine(
        repository.getBoardExamConfig(),
        repository.getAllHolidays()
    ) { config, holidays ->
        val safeConfig = config ?: BoardExamConfigEntity()
        val daysLeft = repository.calculateDaysUntilBoardExam(safeConfig.examDate)
        val remainingChaps = max(0, safeConfig.totalSyllabusChapters - safeConfig.completedChapters)
        val perc = if (safeConfig.totalSyllabusChapters > 0) {
            ((safeConfig.completedChapters.toFloat() / safeConfig.totalSyllabusChapters) * 100).toInt()
        } else 0

        val requiredDailyRate = if (daysLeft > 0) (remainingChaps.toFloat() / daysLeft) else 1.0f
        // If current completion is higher than expected time elapsed ratio, Ahead Of Schedule
        val isAhead = perc >= 45

        BoardExamUiState(
            config = safeConfig,
            remainingDays = daysLeft,
            remainingChapters = remainingChaps,
            completionPercentage = perc,
            requiredDailyChaptersRate = requiredDailyRate,
            isAheadOfSchedule = isAhead,
            holidays = holidays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BoardExamUiState()
    )

    fun updateExamDate(dateStr: String, totalChapters: Int, completedChapters: Int) = viewModelScope.launch {
        val current = uiState.value.config
        val updated = current.copy(
            examDate = dateStr,
            totalSyllabusChapters = totalChapters,
            completedChapters = completedChapters
        )
        repository.updateBoardExamConfig(updated)
    }

    fun addCustomHoliday(name: String, dateStr: String, reductionPercent: Int) = viewModelScope.launch {
        val holiday = HolidayEntity(
            name = name,
            date = dateStr,
            isIndianFestival = false,
            workloadReductionPercent = reductionPercent
        )
        repository.addHoliday(holiday)
    }
}

class WinterArcViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WinterArcViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WinterArcViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class BoardExamViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardExamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoardExamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
