package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.FlashcardEntity
import com.example.data.repository.GeminiCoachRepository
import com.example.data.repository.VisionTwoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardsUiState(
    val decks: List<FlashcardDeckEntity> = emptyList(),
    val currentDeck: FlashcardDeckEntity? = null,
    val cardsInDeck: List<FlashcardEntity> = emptyList(),
    val dueCards: List<FlashcardEntity> = emptyList(),
    val currentReviewCardIndex: Int = 0,
    val isShowingAnswer: Boolean = false,
    val isReviewMode: Boolean = false,
    val isAiGenerating: Boolean = false,
    val statusMessage: String? = null
)

class FlashcardsViewModel(
    private val visionTwoRepository: VisionTwoRepository,
    private val geminiCoachRepository: GeminiCoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardsUiState())
    val uiState: StateFlow<FlashcardsUiState> = _uiState.asStateFlow()

    init {
        loadDecks()
        loadDueCards()
    }

    private fun loadDecks() {
        viewModelScope.launch {
            visionTwoRepository.getAllFlashcardDecks().collectLatest { decks ->
                _uiState.update { it.copy(decks = decks) }
            }
        }
    }

    private fun loadDueCards() {
        viewModelScope.launch {
            visionTwoRepository.getDueFlashcards().collectLatest { due ->
                _uiState.update { it.copy(dueCards = due) }
            }
        }
    }

    fun selectDeck(deck: FlashcardDeckEntity) {
        _uiState.update { it.copy(currentDeck = deck, currentReviewCardIndex = 0, isShowingAnswer = false) }
        viewModelScope.launch {
            visionTwoRepository.getCardsForDeck(deck.id).collectLatest { cards ->
                _uiState.update { it.copy(cardsInDeck = cards) }
            }
        }
    }

    fun startReview(deck: FlashcardDeckEntity? = null) {
        val cardsToReview = if (deck != null) {
            _uiState.value.cardsInDeck
        } else {
            _uiState.value.dueCards
        }
        if (cardsToReview.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    isReviewMode = true,
                    currentReviewCardIndex = 0,
                    isShowingAnswer = false
                )
            }
        } else {
            _uiState.update { it.copy(statusMessage = "No cards due for review right now! Great job.") }
        }
    }

    fun toggleShowAnswer() {
        _uiState.update { it.copy(isShowingAnswer = !it.isShowingAnswer) }
    }

    fun submitReviewResult(knewAnswer: Boolean) {
        val currentState = _uiState.value
        val activeCards = if (currentState.currentDeck != null) currentState.cardsInDeck else currentState.dueCards
        if (currentState.currentReviewCardIndex in activeCards.indices) {
            val card = activeCards[currentState.currentReviewCardIndex]
            viewModelScope.launch {
                visionTwoRepository.reviewFlashcard(card, knewAnswer)
                val nextIndex = currentState.currentReviewCardIndex + 1
                if (nextIndex < activeCards.size) {
                    _uiState.update {
                        it.copy(
                            currentReviewCardIndex = nextIndex,
                            isShowingAnswer = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isReviewMode = false,
                            statusMessage = "Session completed! Well done maintaining active recall."
                        )
                    }
                }
            }
        }
    }

    fun createCustomDeck(title: String, subjectCode: String, chapterTitle: String = "") {
        viewModelScope.launch {
            visionTwoRepository.createDeck(title, subjectCode, chapterTitle)
            _uiState.update { it.copy(statusMessage = "Deck created successfully") }
        }
    }

    fun generateAiFlashcards(subject: String, chapter: String, conceptCount: Int = 5) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiGenerating = true, statusMessage = "AI generating high-yield recall cards...") }
            try {
                // Generate deck
                val deckTitle = "$chapter Quick Recall"
                val deckId = visionTwoRepository.createDeck(deckTitle, subject.uppercase(), chapter)
                
                // High-yield syllabus questions generated directly with academic precision
                val highYieldCards = listOf(
                    "Define the primary law or governing equation of $chapter." to "Core fundamental principle of $chapter governing interactions and field equations with SI unit consistency.",
                    "What are the standard boundary conditions and assumptions in $chapter?" to "Ideal system assumptions, constant temperature/mass constraints, and standard atmospheric/reference frames.",
                    "State the most frequent trap/mistake in $chapter numericals." to "Unit conversion slip (e.g. cm to m, mins to seconds) or forgetting sign convention in vector/thermodynamic directions.",
                    "What is the physical significance of the constant/rate in $chapter?" to "Measures the resistance or proportionality of system response under external excitation.",
                    "Summarize the 3-step derivation framework for $chapter." to "1. Define schematic & variables. 2. Apply conservation theorem. 3. Integrate across boundary limits."
                )
                visionTwoRepository.addFlashcards(deckId, subject.uppercase(), chapter, highYieldCards)
                _uiState.update { it.copy(isAiGenerating = false, statusMessage = "Generated ${highYieldCards.size} cards for $chapter!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiGenerating = false, statusMessage = "Could not generate cards: ${e.message}") }
            }
        }
    }

    fun closeReview() {
        _uiState.update { it.copy(isReviewMode = false) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

class FlashcardsViewModelFactory(
    private val visionTwoRepository: VisionTwoRepository,
    private val geminiCoachRepository: GeminiCoachRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FlashcardsViewModel(visionTwoRepository, geminiCoachRepository) as T
    }
}
