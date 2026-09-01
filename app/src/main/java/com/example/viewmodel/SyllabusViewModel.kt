package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SyllabusChapterEntity
import com.example.data.local.entity.SyllabusStatus
import com.example.data.local.entity.SyllabusTopicEntity
import com.example.data.local.entity.SyllabusUnitEntity
import com.example.data.master.MasterSubjectData
import com.example.data.master.MasterSyllabusProvider
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyllabusSubjectSummary(
    val code: String,
    val name: String,
    val totalChapters: Int,
    val completedChapters: Int,
    val masteredChapters: Int,
    val totalTopics: Int,
    val completedTopics: Int,
    val percentage: Int
)

data class SyllabusUiState(
    val selectedSubjectCode: String = "PHYSICS",
    val searchQuery: String = "",
    val units: List<SyllabusUnitEntity> = emptyList(),
    val chapters: List<SyllabusChapterEntity> = emptyList(),
    val topics: List<SyllabusTopicEntity> = emptyList(),
    val subjectSummaries: List<SyllabusSubjectSummary> = emptyList(),
    val overallCompletionPercentage: Int = 0,
    val totalChaptersCount: Int = 0,
    val masteredChaptersCount: Int = 0,
    val expandedUnitId: Long? = null,
    val expandedChapterId: Long? = null,
    val isLoading: Boolean = false
)

class SyllabusViewModel(
    private val rebuildRepository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rebuildRepository.initializeMasterSyllabusIfEmpty()
            _uiState.update { it.copy(isLoading = false) }

            // Observe all chapters and topics to calculate live metrics
            combine(
                rebuildRepository.getAllSyllabusChapters(),
                rebuildRepository.getAllSyllabusTopics()
            ) { allChapters, allTopics ->
                val subjects = listOf("PHYSICS", "CHEMISTRY", "BIOLOGY", "HINDI", "ENGLISH")
                val summaries = subjects.map { code ->
                    val subChapters = allChapters.filter { it.subjectCode == code }
                    val subTopics = allTopics.filter { it.subjectCode == code }
                    val totalCh = subChapters.size
                    val compCh = subChapters.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.MASTERED }
                    val mastCh = subChapters.count { it.status == SyllabusStatus.MASTERED }
                    val totalTop = subTopics.size
                    val compTop = subTopics.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.REVISED_ONCE || it.status == SyllabusStatus.REVISED_TWICE || it.status == SyllabusStatus.MASTERED }
                    val pct = if (totalTop > 0) (compTop * 100) / totalTop else 0

                    val subName = when (code) {
                        "PHYSICS" -> "Physics"
                        "CHEMISTRY" -> "Chemistry"
                        "BIOLOGY" -> "Biology"
                        "HINDI" -> "Hindi Core"
                        "ENGLISH" -> "English Core"
                        else -> code
                    }

                    SyllabusSubjectSummary(
                        code = code,
                        name = subName,
                        totalChapters = totalCh,
                        completedChapters = compCh,
                        masteredChapters = mastCh,
                        totalTopics = totalTop,
                        completedTopics = compTop,
                        percentage = pct
                    )
                }

                val totalAllTopics = allTopics.size
                val compAllTopics = allTopics.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.REVISED_ONCE || it.status == SyllabusStatus.REVISED_TWICE || it.status == SyllabusStatus.MASTERED }
                val overallPct = if (totalAllTopics > 0) (compAllTopics * 100) / totalAllTopics else 0
                val totalChCount = allChapters.size
                val masteredChCount = allChapters.count { it.status == SyllabusStatus.MASTERED }

                _uiState.update {
                    it.copy(
                        chapters = allChapters,
                        topics = allTopics,
                        subjectSummaries = summaries,
                        overallCompletionPercentage = overallPct,
                        totalChaptersCount = totalChCount,
                        masteredChaptersCount = masteredChCount
                    )
                }
            }.collect {}
        }

        // Load units for currently selected subject
        loadUnitsForSubject(_uiState.value.selectedSubjectCode)
    }

    fun selectSubject(subjectCode: String) {
        _uiState.update { it.copy(selectedSubjectCode = subjectCode, expandedUnitId = null, expandedChapterId = null) }
        loadUnitsForSubject(subjectCode)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun loadUnitsForSubject(subjectCode: String) {
        viewModelScope.launch {
            rebuildRepository.getSyllabusUnits(subjectCode).collect { units ->
                _uiState.update {
                    it.copy(
                        units = units,
                        expandedUnitId = if (it.expandedUnitId == null && units.isNotEmpty()) units.first().id else it.expandedUnitId
                    )
                }
            }
        }
    }

    fun toggleUnitExpanded(unitId: Long) {
        _uiState.update {
            it.copy(expandedUnitId = if (it.expandedUnitId == unitId) null else unitId)
        }
    }

    fun toggleChapterExpanded(chapterId: Long) {
        _uiState.update {
            it.copy(expandedChapterId = if (it.expandedChapterId == chapterId) null else chapterId)
        }
    }

    fun updateTopicStatus(topicId: Long, status: SyllabusStatus) {
        viewModelScope.launch {
            rebuildRepository.updateTopicStatus(topicId, status)
        }
    }

    fun updateChapterStatus(chapterId: Long, status: SyllabusStatus) {
        viewModelScope.launch {
            rebuildRepository.updateChapterStatus(chapterId, status)
        }
    }

    fun toggleChapterNotes(chapter: SyllabusChapterEntity) {
        viewModelScope.launch {
            val updated = chapter.copy(notesDone = !chapter.notesDone)
            // Save via update
            rebuildRepository.updateChapterStatus(chapter.id, chapter.status)
        }
    }
}

class SyllabusViewModelFactory(
    private val rebuildRepository: RebuildRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyllabusViewModel(rebuildRepository) as T
    }
}
