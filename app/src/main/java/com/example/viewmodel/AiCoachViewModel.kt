package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiPlanType
import com.example.data.repository.GeminiCoachRepository
import com.example.data.repository.RebuildRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "COACH"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiCoachUiState(
    val selectedPlanType: AiPlanType = AiPlanType.DAILY_SCHEDULE,
    val currentPlanContent: String = "",
    val isGenerating: Boolean = false,
    val isApplyingPlan: Boolean = false,
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = "COACH",
            text = "Welcome back, Apex Candidate. I am your REBUILD AI Coach. I have direct access to your 148-day countdown, school timings (09:45 AM - 01:00 PM), habit streaks, and 70-chapter syllabus. How can we optimize your performance today?"
        )
    ),
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
        viewModelScope.launch {
            preferencesRepository.geminiApiKey.collectLatest { key ->
                val effective = geminiRepository.getEffectiveApiKey()
                _uiState.update { it.copy(apiKey = key, hasApiKey = effective.isNotBlank()) }
            }
        }

        refreshContextSnapshot()
        generatePlan(AiPlanType.DAILY_SCHEDULE)
    }

    fun selectPlanType(type: AiPlanType) {
        _uiState.update { it.copy(selectedPlanType = type) }
        generatePlan(type)
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

    fun generatePlan(type: AiPlanType = _uiState.value.selectedPlanType, customInstruction: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, actionFeedback = null) }

            val result = geminiRepository.generatePlan(type, customInstruction)
            result.onSuccess { content ->
                _uiState.update { it.copy(currentPlanContent = content, isGenerating = false) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        actionFeedback = "AI Status: ${err.message}",
                        isGenerating = false
                    )
                }
            }
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
                    text = "Offline Mode: Maintain your discipline protocol! Execute your study sessions and stay consistent with the Winter Arc."
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
                        actionFeedback = "⚡ Applied $count AI-optimized study & workout blocks to your today's schedule and alarms!",
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
