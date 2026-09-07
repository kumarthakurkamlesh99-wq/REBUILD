package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.MistakeSeverity
import com.example.data.repository.VisionTwoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MistakeNotebookUiState(
    val mistakes: List<MistakeEntity> = emptyList(),
    val filterSubject: String? = null,
    val showOnlyUnresolved: Boolean = false,
    val selectedMistake: MistakeEntity? = null,
    val isAddingMistake: Boolean = false,
    val statusMessage: String? = null
)

class MistakeNotebookViewModel(
    private val visionTwoRepository: VisionTwoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MistakeNotebookUiState())
    val uiState: StateFlow<MistakeNotebookUiState> = _uiState.asStateFlow()

    init {
        loadMistakes()
    }

    private fun loadMistakes() {
        viewModelScope.launch {
            visionTwoRepository.getAllMistakes().collectLatest { all ->
                _uiState.update { it.copy(mistakes = all) }
            }
        }
    }

    fun setFilterSubject(subject: String?) {
        _uiState.update { it.copy(filterSubject = subject) }
    }

    fun toggleShowOnlyUnresolved() {
        _uiState.update { it.copy(showOnlyUnresolved = !it.showOnlyUnresolved) }
    }

    fun selectMistake(mistake: MistakeEntity?) {
        _uiState.update { it.copy(selectedMistake = mistake) }
    }

    fun toggleResolve(mistake: MistakeEntity) {
        viewModelScope.launch {
            visionTwoRepository.toggleMistakeResolved(mistake)
        }
    }

    fun deleteMistake(id: Long) {
        viewModelScope.launch {
            visionTwoRepository.deleteMistake(id)
            if (_uiState.value.selectedMistake?.id == id) {
                _uiState.update { it.copy(selectedMistake = null) }
            }
        }
    }

    fun addMistake(
        subjectCode: String,
        chapterTitle: String,
        topicTitle: String = "",
        questionOrContext: String,
        studentMistake: String,
        correctSolution: String,
        coreConcept: String,
        severity: MistakeSeverity = MistakeSeverity.MODERATE,
        whyMade: String = ""
    ) {
        viewModelScope.launch {
            visionTwoRepository.addMistake(
                subjectCode = subjectCode.uppercase(),
                chapterTitle = chapterTitle,
                topicTitle = topicTitle,
                questionOrContext = questionOrContext,
                studentMistake = studentMistake,
                correctSolution = correctSolution,
                coreConcept = coreConcept,
                severity = severity,
                whyMade = whyMade
            )
            _uiState.update { it.copy(isAddingMistake = false, statusMessage = "Mistake analyzed & cataloged.") }
        }
    }

    fun setAddingMistake(isAdding: Boolean) {
        _uiState.update { it.copy(isAddingMistake = isAdding) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

class MistakeNotebookViewModelFactory(
    private val visionTwoRepository: VisionTwoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MistakeNotebookViewModel(visionTwoRepository) as T
    }
}
