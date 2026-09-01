package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AiCoachPersona
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.repository.GeminiCoachRepository
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val currentInput: String = "",
    val selectedPersona: AiCoachPersona = AiCoachPersona.BOARD_EXAM_COACH,
    val isSending: Boolean = false,
    val contextSnapshot: String = "",
    val showContextDialog: Boolean = false,
    val quickPrompts: List<String> = listOf(
        "What should I study today?",
        "Create a plan for tomorrow.",
        "Am I behind schedule?",
        "Which chapter should I finish next?",
        "Analyze my Physics progress.",
        "Create a workout based on today's schedule."
    )
)

class AiChatViewModel(
    private val geminiRepository: GeminiCoachRepository,
    private val rebuildRepository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            rebuildRepository.getChatMessages().collect { dbMessages ->
                if (dbMessages.isEmpty()) {
                    // Seed initial welcome message
                    val initialMessage = ChatMessageEntity(
                        role = "model",
                        content = "Welcome back. I am your REBUILD AI Coach with direct access to your enrolled Class 12 syllabus, countdown, study sessions, school schedule, and habit streaks. How can we optimize your performance today?",
                        persona = _uiState.value.selectedPersona
                    )
                    rebuildRepository.saveChatMessage(initialMessage)
                } else {
                    _uiState.update { it.copy(messages = dbMessages) }
                }
            }
        }
        refreshContextSnapshot()
    }

    fun setInput(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun selectPersona(persona: AiCoachPersona) {
        _uiState.update { it.copy(selectedPersona = persona) }
    }

    fun toggleContextDialog(show: Boolean) {
        if (show) refreshContextSnapshot()
        _uiState.update { it.copy(showContextDialog = show) }
    }

    fun refreshContextSnapshot() {
        viewModelScope.launch {
            val snapshot = geminiRepository.buildAppContextSnapshot()
            _uiState.update { it.copy(contextSnapshot = snapshot) }
        }
    }

    fun sendMessage(customPrompt: String? = null) {
        val messageText = (customPrompt ?: _uiState.value.currentInput).trim()
        if (messageText.isBlank() || _uiState.value.isSending) return

        val currentPersona = _uiState.value.selectedPersona
        val userEntity = ChatMessageEntity(
            role = "user",
            content = messageText,
            persona = currentPersona
        )

        _uiState.update { it.copy(currentInput = "", isSending = true) }

        viewModelScope.launch {
            rebuildRepository.saveChatMessage(userEntity)
            val currentHistory = _uiState.value.messages

            val result = geminiRepository.sendChatMessage(
                userMessage = messageText,
                persona = currentPersona,
                history = currentHistory
            )

            val replyText = result.getOrElse {
                "Coach: Let's execute your daily goals with full intensity. Complete your study blocks and maintain your streak."
            }

            val coachEntity = ChatMessageEntity(
                role = "model",
                content = replyText,
                persona = currentPersona
            )
            rebuildRepository.saveChatMessage(coachEntity)
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            rebuildRepository.clearChatHistory()
        }
    }
}

class AiChatViewModelFactory(
    private val geminiRepository: GeminiCoachRepository,
    private val rebuildRepository: RebuildRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiChatViewModel(geminiRepository, rebuildRepository) as T
    }
}
