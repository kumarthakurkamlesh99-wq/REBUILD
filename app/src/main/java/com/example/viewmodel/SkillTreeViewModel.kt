package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SkillNodeEntity
import com.example.data.repository.UniversalGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SkillTreeUiState(
    val selectedCategory: String = "CODING", // "CODING", "FITNESS", "EXAMS"
    val nodes: List<SkillNodeEntity> = emptyList(),
    val totalMastered: Int = 0,
    val totalNodes: Int = 0,
    val totalXpEarned: Int = 0,
    val isLoading: Boolean = false
)

class SkillTreeViewModel(
    private val repository: UniversalGoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillTreeUiState())
    val uiState: StateFlow<SkillTreeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultSkillTrees()
            loadCategoryNodes("CODING")
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadCategoryNodes(category)
    }

    private fun loadCategoryNodes(category: String) {
        viewModelScope.launch {
            repository.getSkillNodesByCategory(category).collect { list ->
                val mastered = list.count { it.isMastered }
                val xp = list.filter { it.isMastered }.sumOf { it.xpReward }
                _uiState.update {
                    it.copy(
                        nodes = list,
                        totalMastered = mastered,
                        totalNodes = list.size,
                        totalXpEarned = xp
                    )
                }
            }
        }
    }

    fun updateNodeMastery(node: SkillNodeEntity, newPercentage: Int) {
        viewModelScope.launch {
            val isNowMastered = newPercentage >= 100
            repository.updateNodeProgress(node.id, isNowMastered, newPercentage.coerceIn(0, 100))
        }
    }
}

class SkillTreeViewModelFactory(
    private val repository: UniversalGoalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SkillTreeViewModel::class.java)) {
            return SkillTreeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
