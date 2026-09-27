package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.SyllabusStatus
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val userProfile: UserProfileEntity? = null,
    val winterArcState: WinterArcStateEntity = WinterArcStateEntity(),
    val boardExamConfig: BoardExamConfigEntity = BoardExamConfigEntity(),
    val disciplineScore: DailyDisciplineEntity = DailyDisciplineEntity(date = "", totalScore = 0),
    val schoolStatus: SchoolStatusEntity = SchoolStatusEntity(date = ""),
    val todayTasks: List<DailyPlanTaskEntity> = emptyList(),
    val daysUntilExam: Long = 0,
    val todayStudyMinutes: Int = 0,
    val completedTasksCount: Int = 0,
    val totalTasksCount: Int = 0,
    val progressPercentage: Int = 0,
    val realStreak: Int = 0,
    val winterArcDaysRemaining: Int = 0,
    val subjectSummaries: List<SyllabusSubjectSummary> = emptyList(),
    val overallSyllabusPercentage: Int = 0,
    val totalSyllabusChapters: Int = 0,
    val completedSyllabusChapters: Int = 0
)

class HomeViewModel(private val repository: RebuildRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.syncWinterArcCalculations()
            repository.initializeMasterSyllabusIfEmpty()
        }
    }

    private val baseProfileFlow = combine(
        repository.getUserProfile(),
        repository.getWinterArcState(),
        repository.getBoardExamConfig()
    ) { profile, arc, exam ->
        Triple(profile, arc ?: WinterArcStateEntity(), exam ?: BoardExamConfigEntity())
    }

    private val syllabusFlow = combine(
        repository.getAllSyllabusChapters(),
        repository.getAllSyllabusTopics()
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
        val totalCh = allChapters.size
        val compCh = allChapters.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.MASTERED }

        SyllabusBundle(summaries, overallPct, totalCh, compCh)
    }

    private data class DailyStatusBundle(
        val discipline: DailyDisciplineEntity?,
        val school: SchoolStatusEntity?,
        val tasks: List<DailyPlanTaskEntity>,
        val studyMins: Int
    )

    private data class SyllabusBundle(
        val summaries: List<SyllabusSubjectSummary>,
        val overallPct: Int,
        val totalCh: Int,
        val compCh: Int
    )

    private val statusFlow = combine(
        repository.getTodayDiscipline(),
        repository.getTodaySchoolStatus(),
        repository.getTodayTasks(),
        repository.getTodayStudyMinutes()
    ) { discipline, school, tasks, studyMins ->
        DailyStatusBundle(discipline, school, tasks, studyMins)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseProfileFlow,
        statusFlow,
        syllabusFlow
    ) { (profile, safeWinterArc, safeExamConfig), statusBundle, syllabusBundle ->
        val safeDiscipline = statusBundle.discipline ?: DailyDisciplineEntity(
            date = repository.getTodayDateString(),
            totalScore = 0
        )
        val safeSchool = statusBundle.school ?: SchoolStatusEntity(date = repository.getTodayDateString())
        val daysLeft = repository.calculateDaysUntilBoardExam(safeExamConfig.examDate)
        val completedCount = statusBundle.tasks.count { it.isCompleted }
        val totalCount = statusBundle.tasks.size
        val progress = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

        val dayNum = repository.calculateWinterArcDayNumber(safeWinterArc.startDate, safeWinterArc.targetDays)
        val arcDaysLeft = repository.calculateWinterArcDaysRemaining(safeWinterArc.startDate, safeWinterArc.targetDays)
        val realStreak = repository.calculateRealStreak()
        val score = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else safeDiscipline.totalScore

        val dynamicArc = safeWinterArc.copy(
            currentDay = dayNum,
            streak = realStreak,
            transformationScore = score
        )

        HomeUiState(
            userProfile = profile,
            winterArcState = dynamicArc,
            boardExamConfig = safeExamConfig,
            disciplineScore = safeDiscipline.copy(totalScore = score),
            schoolStatus = safeSchool,
            todayTasks = statusBundle.tasks,
            daysUntilExam = daysLeft,
            todayStudyMinutes = statusBundle.studyMins,
            completedTasksCount = completedCount,
            totalTasksCount = totalCount,
            progressPercentage = progress,
            realStreak = realStreak,
            winterArcDaysRemaining = arcDaysLeft,
            subjectSummaries = syllabusBundle.summaries,
            overallSyllabusPercentage = syllabusBundle.overallPct,
            totalSyllabusChapters = syllabusBundle.totalCh,
            completedSyllabusChapters = syllabusBundle.compCh
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = Long.MAX_VALUE),
        initialValue = HomeUiState()
    )

    fun onDispatchSchool() {
        viewModelScope.launch {
            repository.dispatchSchool()
        }
    }

    fun onArrivedSchool() {
        viewModelScope.launch {
            repository.arrivedSchool()
        }
    }

    fun onDispatchHome() {
        viewModelScope.launch {
            repository.dispatchHome()
        }
    }

    fun onArrivedHome() {
        viewModelScope.launch {
            repository.arrivedHome()
        }
    }

    fun toggleTask(task: DailyPlanTaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun generateTodayPlan() {
        viewModelScope.launch {
            repository.generateSmartDailyPlan(repository.getTodayDateString())
        }
    }
}

class HomeViewModelFactory(private val repository: RebuildRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
