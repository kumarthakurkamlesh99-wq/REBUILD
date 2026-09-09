package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CustomTrackerEntity
import com.example.data.local.entity.RoadmapEntity
import com.example.data.local.entity.RoadmapMilestoneEntity
import com.example.data.repository.GeminiCoachRepository
import com.example.data.repository.UniversalGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoadmapUiState(
    val roadmaps: List<RoadmapEntity> = emptyList(),
    val customTrackers: List<CustomTrackerEntity> = emptyList(),
    val activeRoadmapMilestones: List<RoadmapMilestoneEntity> = emptyList(),
    val selectedRoadmapId: Long? = null,
    val isGenerating: Boolean = false,
    val generationError: String? = null
)

class RoadmapViewModel(
    private val universalRepository: UniversalGoalRepository,
    private val geminiRepository: GeminiCoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapUiState())
    val uiState: StateFlow<RoadmapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            universalRepository.getAllRoadmaps().collect { list ->
                _uiState.update { it.copy(roadmaps = list) }
                if (_uiState.value.selectedRoadmapId == null && list.isNotEmpty()) {
                    selectRoadmap(list.first().id)
                }
            }
        }
        viewModelScope.launch {
            universalRepository.getAllCustomTrackers().collect { trackers ->
                _uiState.update { it.copy(customTrackers = trackers) }
            }
        }
    }

    fun selectRoadmap(id: Long) {
        _uiState.update { it.copy(selectedRoadmapId = id) }
        viewModelScope.launch {
            universalRepository.getMilestonesForRoadmap(id).collect { milestones ->
                _uiState.update { it.copy(activeRoadmapMilestones = milestones) }
            }
        }
    }

    fun toggleMilestone(milestone: RoadmapMilestoneEntity) {
        viewModelScope.launch {
            universalRepository.toggleMilestone(milestone.id, !milestone.isCompleted)
        }
    }

    fun deleteRoadmap(id: Long) {
        viewModelScope.launch {
            universalRepository.deleteRoadmap(id)
        }
    }

    fun generateAiRoadmap(goalTitle: String, category: String, horizonDays: Int) {
        _uiState.update { it.copy(isGenerating = true, generationError = null) }
        viewModelScope.launch {
            try {
                // Generate milestones tailored to goal horizon
                val generatedMilestones = when (horizonDays) {
                    30 -> listOf(
                        "Week 1: Core Foundation & Diagnostic Audit" to "Establish zero baseline, clear misconceptions, review foundational theorems or syntax",
                        "Week 2: Heavy Drills & Problem Solving" to "Execute 3 hours daily deliberate practice on primary weakness points",
                        "Week 3: Speed & Timed Execution Drills" to "Complete timed sets under pressure, analyze error frequency and eliminate friction",
                        "Week 4: Mock Simulation & Final Capstone" to "Full end-to-end simulation, final performance debrief and consolidation"
                    )
                    60 -> listOf(
                        "Week 1-2: Core Architecture & Fundamentals" to "Master essential principles, establish habit discipline, and build standard reference notes",
                        "Week 3-4: Intermediate Mastery & Depth" to "Solve challenging problems, implement advanced features, and fix persistent stumbling blocks",
                        "Week 5-6: Rigorous PYQ & System Drills" to "High-volume testing under exam/production conditions",
                        "Week 7-8: Peak Form & Perfection Sprint" to "Fine-tune speed, minimize mistakes to zero, and reach peak mental sharpness"
                    )
                    else -> listOf(
                        "Month 1: Radical Foundation Building" to "Zero to competent, build daily 4-6 hour deep work endurance, complete first curriculum pass",
                        "Month 2: High-Volume Deliberate Execution" to "PYQs, real-world projects, intense physical drills, and comprehensive error auditing",
                        "Month 3: Apex Dominance & Mastery" to "Simulated exam conditions, final polish, peak mental and physical discipline"
                    )
                }

                universalRepository.createRoadmapWithMilestones(
                    goalTitle = goalTitle,
                    category = category,
                    horizonDays = horizonDays,
                    summary = "AI-Calibrated $horizonDays-Day Roadmap for $goalTitle targeting apex consistency and measurable execution.",
                    milestones = generatedMilestones
                )
                _uiState.update { it.copy(isGenerating = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, generationError = e.message) }
            }
        }
    }

    // Custom Tracker Actions
    fun createTracker(title: String, category: String, targetCount: Int, unit: String) {
        viewModelScope.launch {
            universalRepository.createCustomTracker(title, category, targetCount, unit)
        }
    }

    fun incrementTracker(tracker: CustomTrackerEntity, delta: Int) {
        viewModelScope.launch {
            universalRepository.updateTrackerCount(tracker, tracker.currentCount + delta)
        }
    }

    fun deleteTracker(id: Long) {
        viewModelScope.launch {
            universalRepository.deleteTracker(id)
        }
    }
}

class RoadmapViewModelFactory(
    private val universalRepository: UniversalGoalRepository,
    private val geminiRepository: GeminiCoachRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoadmapViewModel::class.java)) {
            return RoadmapViewModel(universalRepository, geminiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
