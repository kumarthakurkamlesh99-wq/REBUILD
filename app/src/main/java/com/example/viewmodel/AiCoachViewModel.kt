package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AiPlanCacheEntity
import com.example.data.repository.AiPlanType
import com.example.data.repository.GeminiCoachRepository
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "COACH"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiCoachUiState(
    val selectedPlanType: AiPlanType = AiPlanType.DAILY_SCHEDULE,
    val currentPlanContent: String = "",
    val promptInput: String = "",
    val isEditMode: Boolean = false,
    val editedPlanText: String = "",
    val savedPlans: List<AiPlanCacheEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val isApplyingPlan: Boolean = false,
    val isFromCache: Boolean = false,
    val isLocalGenerated: Boolean = false,
    val lastCachedDate: String = "",
    val lastCachedTimestamp: Long = 0L,
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val chatInput: String = "",
    val contextSnapshot: String = "",
    val actionFeedback: String? = null
)

class AiCoachViewModel(
    private val geminiRepository: GeminiCoachRepository,
    private val rebuildRepository: RebuildRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCoachUiState())
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    init {
        // Collect API key
        viewModelScope.launch {
            preferencesRepository.geminiApiKey.collectLatest { key ->
                val effective = geminiRepository.getEffectiveApiKey()
                _uiState.update { it.copy(apiKey = key, hasApiKey = effective.isNotBlank()) }
            }
        }

        // Collect all saved plans from Room DB
        viewModelScope.launch {
            geminiRepository.getAllCachedPlans().collectLatest { plans ->
                _uiState.update { it.copy(savedPlans = plans) }
            }
        }

        // Initialize Chat with dynamic telemetry greeting
        viewModelScope.launch {
            val exam = rebuildRepository.getBoardExamConfigDirect()
            val daysLeft = if (exam != null) rebuildRepository.calculateDaysUntilBoardExam(exam.examDate) else 0L
            val profile = rebuildRepository.getUserProfileDirect()
            val schoolHours = if (profile != null && profile.hasSchool) "${profile.schoolStartTime} - ${profile.schoolEndTime}" else "Full Day Self Study"
            val chapsCount = rebuildRepository.getTotalChaptersCount().firstOrNull() ?: 70

            val welcome = "Welcome back, ${profile?.name ?: "Candidate"}. I am your REBUILD AI Coach. Live telemetry active: $daysLeft days to Board Exam, school schedule ($schoolHours), and $chapsCount syllabus chapters. How can we optimize your performance today?"
            _uiState.update {
                it.copy(
                    chatMessages = listOf(ChatMessage(sender = "COACH", text = welcome))
                )
            }
        }

        // Ensure non-blank screen: load cached plan, or generate offline study plan immediately
        viewModelScope.launch {
            val cached = geminiRepository.getCachedPlanDirect(AiPlanType.DAILY_SCHEDULE.key)
            if (cached != null && cached.content.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        currentPlanContent = cached.content,
                        isFromCache = true,
                        lastCachedDate = cached.generatedDate,
                        lastCachedTimestamp = cached.timestamp
                    )
                }
            } else {
                // Generate immediate local offline study plan so UI is never blank
                generateLocalPlan()
            }
        }
    }

    fun setPromptInput(text: String) {
        _uiState.update { it.copy(promptInput = text) }
    }

    fun selectPlanType(type: AiPlanType) {
        _uiState.update { it.copy(selectedPlanType = type) }
        viewModelScope.launch {
            val cached = geminiRepository.getCachedPlan(type.key).firstOrNull()
            if (cached != null && cached.content.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        currentPlanContent = cached.content,
                        isFromCache = true,
                        isLocalGenerated = false,
                        lastCachedDate = cached.generatedDate,
                        lastCachedTimestamp = cached.timestamp
                    )
                }
            } else {
                generateLocalPlan(type)
            }
        }
    }

    fun generateLocalPlan(type: AiPlanType = _uiState.value.selectedPlanType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, actionFeedback = null) }
            val prompt = _uiState.value.promptInput.trim()
            val content = geminiRepository.generateLocalOfflinePlan(type, prompt)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            _uiState.update {
                it.copy(
                    currentPlanContent = content,
                    isGenerating = false,
                    isFromCache = false,
                    isLocalGenerated = true,
                    lastCachedDate = today,
                    lastCachedTimestamp = System.currentTimeMillis(),
                    actionFeedback = "Offline Study Planner generated based on your syllabus and schedule."
                )
            }
        }
    }

    fun generatePlan(
        type: AiPlanType = _uiState.value.selectedPlanType,
        customInstruction: String = _uiState.value.promptInput,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, actionFeedback = null) }

            val effectiveKey = geminiRepository.getEffectiveApiKey()
            if (effectiveKey.isBlank()) {
                // If AI unavailable, show useful offline study planner
                val content = geminiRepository.generateLocalOfflinePlan(type, customInstruction)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _uiState.update {
                    it.copy(
                        currentPlanContent = content,
                        isGenerating = false,
                        isFromCache = false,
                        isLocalGenerated = true,
                        lastCachedDate = today,
                        lastCachedTimestamp = System.currentTimeMillis(),
                        actionFeedback = "AI key not configured: Generated high-yield offline study planner."
                    )
                }
                return@launch
            }

            val result = geminiRepository.generatePlan(type, customInstruction, forceRefresh = forceRefresh)
            result.onSuccess { content ->
                val cached = geminiRepository.getCachedPlanDirect(type.key)
                _uiState.update {
                    it.copy(
                        currentPlanContent = content,
                        isGenerating = false,
                        isFromCache = !forceRefresh,
                        isLocalGenerated = false,
                        lastCachedDate = cached?.generatedDate ?: "",
                        lastCachedTimestamp = cached?.timestamp ?: System.currentTimeMillis(),
                        actionFeedback = "AI Plan generated successfully!"
                    )
                }
            }.onFailure { err ->
                // Fallback to offline planner so screen is NEVER blank
                val fallback = geminiRepository.generateLocalOfflinePlan(type, customInstruction)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _uiState.update {
                    it.copy(
                        currentPlanContent = fallback,
                        isGenerating = false,
                        isFromCache = false,
                        isLocalGenerated = true,
                        lastCachedDate = today,
                        lastCachedTimestamp = System.currentTimeMillis(),
                        actionFeedback = "AI unavailable (${err.message}). Showing offline study planner."
                    )
                }
            }
        }
    }

    // Editable Plan Handlers
    fun startEditingPlan() {
        _uiState.update {
            it.copy(
                isEditMode = true,
                editedPlanText = it.currentPlanContent
            )
        }
    }

    fun updateEditedPlanText(newText: String) {
        _uiState.update { it.copy(editedPlanText = newText) }
    }

    fun cancelEditingPlan() {
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun saveEditedPlan() {
        val updatedText = _uiState.value.editedPlanText
        val type = _uiState.value.selectedPlanType
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val entity = AiPlanCacheEntity(
                planType = type.key,
                title = type.title,
                content = updatedText,
                generatedDate = today,
                timestamp = System.currentTimeMillis()
            )
            geminiRepository.saveCustomPlan(entity)
            _uiState.update {
                it.copy(
                    currentPlanContent = updatedText,
                    isEditMode = false,
                    lastCachedDate = today,
                    actionFeedback = "Plan edits saved to local database!"
                )
            }
        }
    }

    fun savePlanWithCustomTitle(customTitle: String) {
        val type = _uiState.value.selectedPlanType
        val title = if (customTitle.isNotBlank()) customTitle.trim() else type.title
        val key = "${type.key}_${System.currentTimeMillis()}"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val entity = AiPlanCacheEntity(
                planType = key,
                title = title,
                content = _uiState.value.currentPlanContent,
                generatedDate = today,
                timestamp = System.currentTimeMillis()
            )
            geminiRepository.saveCustomPlan(entity)
            _uiState.update { it.copy(actionFeedback = "Saved '$title' to your plans library.") }
        }
    }

    fun loadSavedPlan(plan: AiPlanCacheEntity) {
        _uiState.update {
            it.copy(
                currentPlanContent = plan.content,
                lastCachedDate = plan.generatedDate,
                lastCachedTimestamp = plan.timestamp,
                isFromCache = true,
                isEditMode = false,
                actionFeedback = "Loaded '${plan.title}'"
            )
        }
    }

    fun deleteSavedPlan(plan: AiPlanCacheEntity) {
        viewModelScope.launch {
            geminiRepository.deletePlan(plan.planType)
            _uiState.update { it.copy(actionFeedback = "Deleted plan '${plan.title}'.") }
        }
    }

    fun setChatInput(text: String) {
        _uiState.update { it.copy(chatInput = text) }
    }

    fun refreshContextSnapshot() {
        viewModelScope.launch {
            val snapshot = geminiRepository.buildAppContextSnapshot()
            _uiState.update { it.copy(contextSnapshot = snapshot) }
        }
    }

    fun sendChatMessage() {
        val text = _uiState.value.chatInput.trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(sender = "USER", text = text)
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                chatInput = "",
                isGenerating = true
            )
        }

        viewModelScope.launch {
            val result = geminiRepository.askCoach(text)
            result.onSuccess { reply ->
                val coachMsg = ChatMessage(sender = "COACH", text = reply)
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + coachMsg,
                        isGenerating = false
                    )
                }
            }.onFailure { _ ->
                val coachMsg = ChatMessage(
                    sender = "COACH",
                    text = "Offline Mode: Maintain your discipline protocol! Execute your study sessions and stay consistent with your Bihar Board PCM syllabus and Winter Arc."
                )
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + coachMsg,
                        isGenerating = false
                    )
                }
            }
        }
    }

    fun applyPlanToLocalSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingPlan = true) }
            try {
                val count = geminiRepository.applyAiPlanToLocalSchedule(_uiState.value.currentPlanContent)
                _uiState.update {
                    it.copy(
                        actionFeedback = "⚡ Applied $count study & workout blocks to your today's schedule and alarms!",
                        isApplyingPlan = false
                    )
                }
                rebuildRepository.addXp(50)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionFeedback = "Error applying plan: ${e.message}",
                        isApplyingPlan = false
                    )
                }
            }
        }
    }

    fun updateApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiApiKey(key.trim())
            _uiState.update { it.copy(actionFeedback = "Gemini API Key updated successfully.") }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(actionFeedback = null) }
    }
}

class AiCoachViewModelFactory(
    private val geminiRepository: GeminiCoachRepository,
    private val rebuildRepository: RebuildRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiCoachViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiCoachViewModel(geminiRepository, rebuildRepository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
