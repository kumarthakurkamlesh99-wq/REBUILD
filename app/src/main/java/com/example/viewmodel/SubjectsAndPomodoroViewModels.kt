package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.SessionType
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubjectsUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubject: SubjectEntity? = null,
    val chapters: List<ChapterEntity> = emptyList(),
    val totalChaptersCount: Int = 0,
    val completedChaptersCount: Int = 0,
    val overallProgress: Int = 0
)

class SubjectsViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId = _selectedSubjectId.asStateFlow()

    private val _currentChapters = MutableStateFlow<List<ChapterEntity>>(emptyList())

    val uiState: StateFlow<SubjectsUiState> = combine(
        repository.getAllSubjects(),
        _selectedSubjectId,
        _currentChapters
    ) { subjects, selectedId, chapters ->
        val activeSubject = subjects.find { it.id == selectedId } ?: subjects.firstOrNull()
        val totalChaps = subjects.sumOf { it.totalChapters }
        val doneChaps = subjects.sumOf { it.completedChapters }
        val overall = if (totalChaps > 0) ((doneChaps.toFloat() / totalChaps) * 100).toInt() else 0

        SubjectsUiState(
            subjects = subjects,
            selectedSubject = activeSubject,
            chapters = chapters,
            totalChaptersCount = totalChaps,
            completedChaptersCount = doneChaps,
            overallProgress = overall
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubjectsUiState()
    )

    init {
        viewModelScope.launch {
            repository.getAllSubjects().collect { subjects ->
                if (_selectedSubjectId.value == null && subjects.isNotEmpty()) {
                    selectSubject(subjects.first().id)
                }
            }
        }
    }

    fun selectSubject(subjectId: Long) {
        _selectedSubjectId.value = subjectId
        viewModelScope.launch {
            repository.getChaptersForSubject(subjectId).collect {
                _currentChapters.value = it
            }
        }
    }

    fun updateChapterProgress(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(chapter)
        }
    }

    fun incrementRevision(chapterId: Long) {
        viewModelScope.launch {
            repository.incrementChapterRevision(chapterId)
        }
    }
}

data class PomodoroUiState(
    val sessionType: SessionType = SessionType.POMODORO_25_5,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val completedSessionsCount: Int = 4,
    val todayFocusMinutes: Int = 110,
    val weeklyFocusHours: Float = 14.5f,
    val selectedSubject: String = "Physics",
    val selectedChapter: String = "Nuclei",
    val recentSessions: List<StudySessionEntity> = emptyList()
)

class PomodoroViewModel(private val repository: RebuildRepository) : ViewModel() {

    private val _pomodoroState = MutableStateFlow(PomodoroUiState())
    private var timerJob: Job? = null

    val uiState: StateFlow<PomodoroUiState> = combine(
        _pomodoroState,
        repository.getTodayStudySessions(),
        repository.getTodayStudyMinutes()
    ) { pState, sessions, studyMins ->
        pState.copy(
            recentSessions = sessions,
            todayFocusMinutes = studyMins,
            completedSessionsCount = sessions.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PomodoroUiState()
    )

    fun setMode(type: SessionType) {
        timerJob?.cancel()
        val durationMins = when (type) {
            SessionType.POMODORO_25_5 -> 25
            SessionType.POMODORO_50_10 -> 50
            SessionType.CUSTOM_FOCUS -> 45
            SessionType.DEEP_WORK -> 90
            SessionType.REVISION -> 30
            SessionType.PYQ_PRACTICE -> 60
        }
        val totalSecs = durationMins * 60
        _pomodoroState.value = _pomodoroState.value.copy(
            sessionType = type,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            isRunning = false,
            isBreak = false
        )
    }

    fun startTimer() {
        if (_pomodoroState.value.isRunning) return
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = true)

        timerJob = viewModelScope.launch {
            while (_pomodoroState.value.remainingSeconds > 0 && _pomodoroState.value.isRunning) {
                delay(1000)
                _pomodoroState.value = _pomodoroState.value.copy(
                    remainingSeconds = _pomodoroState.value.remainingSeconds - 1
                )
            }

            if (_pomodoroState.value.remainingSeconds <= 0) {
                onSessionCompleted()
            }
        }
    }

    fun pauseTimer() {
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = false)
        timerJob?.cancel()
    }

    fun resetTimer() {
        timerJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(
            remainingSeconds = _pomodoroState.value.totalSeconds,
            isRunning = false,
            isBreak = false
        )
    }

    private fun onSessionCompleted() {
        val currentState = _pomodoroState.value
        val completedDurationMins = currentState.totalSeconds / 60
        val xp = if (currentState.isBreak) 0 else completedDurationMins * 2

        viewModelScope.launch {
            if (!currentState.isBreak) {
                repository.recordStudySession(
                    subjectName = currentState.selectedSubject,
                    chapterName = currentState.selectedChapter,
                    durationMinutes = completedDurationMins,
                    sessionType = currentState.sessionType,
                    xpReward = xp
                )
            }
            // Switch to break
            val breakMins = if (currentState.sessionType == SessionType.POMODORO_50_10) 10 else 5
            val breakSecs = breakMins * 60
            _pomodoroState.value = currentState.copy(
                isBreak = !currentState.isBreak,
                isRunning = false,
                totalSeconds = breakSecs,
                remainingSeconds = breakSecs
            )
        }
    }

    fun setSelectedSubjectAndChapter(subject: String, chapter: String) {
        _pomodoroState.value = _pomodoroState.value.copy(
            selectedSubject = subject,
            selectedChapter = chapter
        )
    }
}

class SubjectsViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PomodoroViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PomodoroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
