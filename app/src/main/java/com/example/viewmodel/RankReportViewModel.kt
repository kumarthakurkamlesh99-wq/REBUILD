package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.LevelPurchaseEntity
import com.example.data.model.RankLevel
import com.example.data.model.RankLevelSystem
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class RankReportUiState(
    val currentLevel: Int = 1,
    val maxUnlockedLevel: Int = 1,
    val unlockedLevels: Set<Int> = setOf(1),
    val levelPurchases: Map<Int, LevelPurchaseEntity> = emptyMap(),
    val currentXp: Int = 0,
    val currentRank: RankLevel = RankLevelSystem.RANKS[0],
    val xpRequiredForNext: Int = 250,
    val progress: Float = 0f,
    val daysUntilExam: Long = 163,
    val dynamicAnalysis: String = "",
    val allRanks: List<RankLevel> = RankLevelSystem.RANKS,
    val selectedLockedRankForPurchase: RankLevel? = null,
    val isPurchasing: Boolean = false,
    val lastUnlockedPurchase: Pair<LevelPurchaseEntity, RankLevel>? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

class RankReportViewModel(
    private val repository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankReportUiState())
    val uiState: StateFlow<RankReportUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        viewModelScope.launch {
            repository.seedInitialXpTransactionsIfEmpty()
            repository.getMaxPurchasedLevel()
        }

        viewModelScope.launch {
            combine(
                repository.getWinterArcState(),
                repository.getUserProfile(),
                repository.getAllLevelPurchases()
            ) { arcState, profile, purchases ->
                val xp = repository.getCurrentXpBalance()
                val purchasesMap = purchases.associateBy { it.level }
                val unlockedLevelsSet = if (purchases.isEmpty()) setOf(1) else (purchases.map { it.level }.toSet() + 1)
                val maxUnlocked = unlockedLevelsSet.maxOrNull() ?: 1

                val activeRank = RankLevelSystem.getRankForLevel(maxUnlocked)
                val progress = RankLevelSystem.calculateProgress(xp, activeRank)
                val xpForNext = RankLevelSystem.getXpRequiredForNextLevel(xp, activeRank)

                val examDateStr = profile?.targetExamDate ?: "2027-02-15"
                val daysUntilExam = calculateDaysRemaining(examDateStr)
                val analysis = RankLevelSystem.generateDynamicAnalysis(activeRank, daysUntilExam)

                _uiState.update { current ->
                    current.copy(
                        currentLevel = maxUnlocked,
                        maxUnlockedLevel = maxUnlocked,
                        unlockedLevels = unlockedLevelsSet,
                        levelPurchases = purchasesMap,
                        currentXp = xp,
                        currentRank = activeRank,
                        xpRequiredForNext = xpForNext,
                        progress = progress,
                        daysUntilExam = daysUntilExam,
                        dynamicAnalysis = analysis,
                        allRanks = RankLevelSystem.RANKS,
                        isLoading = false
                    )
                }
            }.collectLatest { }
        }
    }

    fun openPurchaseModal(rank: RankLevel) {
        _uiState.update { it.copy(selectedLockedRankForPurchase = rank, errorMessage = null) }
    }

    fun dismissPurchaseModal() {
        _uiState.update { it.copy(selectedLockedRankForPurchase = null, isPurchasing = false) }
    }

    fun dismissCelebration() {
        _uiState.update { it.copy(lastUnlockedPurchase = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun confirmLevelPurchase(rank: RankLevel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }
            val result = repository.purchaseLevel(rank.level)
            result.onSuccess { purchase ->
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        selectedLockedRankForPurchase = null,
                        lastUnlockedPurchase = Pair(purchase, rank)
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        errorMessage = err.message ?: "Failed to unlock level"
                    )
                }
            }
        }
    }

    private fun calculateDaysRemaining(targetDateStr: String): Long {
        return try {
            val target = dateFormat.parse(targetDateStr) ?: return 163L
            val now = Date()
            val diffMs = target.time - now.time
            max(0L, TimeUnit.MILLISECONDS.toDays(diffMs))
        } catch (e: Exception) {
            163L
        }
    }
}

class RankReportViewModelFactory(
    private val repository: RebuildRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankReportViewModel::class.java)) {
            return RankReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
