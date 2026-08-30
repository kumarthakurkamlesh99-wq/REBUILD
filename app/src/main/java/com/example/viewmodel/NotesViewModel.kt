package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyReflectionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val selectedSubjectTag: String = "All",
    val todayReflection: DailyReflectionEntity? = null,
    val recentReflections: List<DailyReflectionEntity> = emptyList(),
    val searchQuery: String = "",
    val activeTab: Int = 0 // 0: Reflection Journal, 1: Study Notes & Formulas
)

class NotesViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val _selectedTag = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")
    private val _activeTab = MutableStateFlow(0)

    private val _filterParams = combine(_selectedTag, _searchQuery, _activeTab) { tag, query, tab ->
        Triple(tag, query, tab)
    }

    val uiState: StateFlow<NotesUiState> = combine(
        repository.getAllNotes(),
        _filterParams,
        repository.getTodayReflection(),
        repository.getRecentReflections()
    ) { notes, filter, reflection, recentList ->
        val (tag, query, tab) = filter

        val filtered = notes.filter { note ->
            val matchesTag = if (tag == "All") true else note.subjectTag.equals(tag, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                note.title.contains(query, ignoreCase = true) || note.content.contains(query, ignoreCase = true)
            }
            matchesTag && matchesQuery
        }

        val safeReflection = reflection ?: DailyReflectionEntity(
            date = repository.getTodayDateString(),
            dailyScore = 8,
            whatWentWell = "",
            whatHeldMeBack = "",
            gratitude = "",
            tomorrowGoal = "",
            mood = "Focused"
        )

        NotesUiState(
            notes = filtered,
            selectedSubjectTag = tag,
            todayReflection = safeReflection,
            recentReflections = recentList,
            searchQuery = query,
            activeTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun setSelectedTag(tag: String) {
        _selectedTag.value = tag
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun saveNote(
        title: String,
        content: String,
        subjectTag: String = "General",
        isPinned: Boolean = false,
        colorHex: String = "#7C8CFF"
    ) = viewModelScope.launch {
        val note = NoteEntity(
            title = title,
            content = content,
            subjectTag = subjectTag,
            date = repository.getTodayDateString(),
            isPinned = isPinned,
            colorHex = colorHex
        )
        repository.saveNote(note)
        repository.addXp(15)
    }

    fun addNote(
        title: String,
        content: String,
        subjectTag: String = "General",
        isPinned: Boolean = false,
        colorHex: String = "#7C8CFF"
    ) = saveNote(title, content, subjectTag, isPinned, colorHex)

    fun updateNote(note: NoteEntity) = viewModelScope.launch {
        repository.saveNote(note)
    }

    fun deleteNote(note: NoteEntity) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun saveDailyReflection(
        score: Int,
        whatWentWell: String,
        whatHeldMeBack: String,
        gratitude: String,
        tomorrowGoal: String,
        mood: String
    ) = viewModelScope.launch {
        val reflection = DailyReflectionEntity(
            date = repository.getTodayDateString(),
            dailyScore = score,
            whatWentWell = whatWentWell,
            whatHeldMeBack = whatHeldMeBack,
            gratitude = gratitude,
            tomorrowGoal = tomorrowGoal,
            mood = mood
        )
        repository.saveReflection(reflection)
        repository.addXp(30)
    }
}

class NotesViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
