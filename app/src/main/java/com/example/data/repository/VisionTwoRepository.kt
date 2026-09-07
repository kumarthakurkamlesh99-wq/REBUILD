package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisionTwoRepository(
    private val db: AppDatabase
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // --- Syllabus Subtopics (5th Tier: Subject → Unit → Chapter → Topic → Subtopic) ---
    fun getSubtopicsForTopic(topicId: Long): Flow<List<SyllabusSubtopicEntity>> =
        db.syllabusSubtopicDao().getSubtopicsForTopic(topicId)

    fun getSubtopicsForSubject(subjectCode: String): Flow<List<SyllabusSubtopicEntity>> =
        db.syllabusSubtopicDao().getSubtopicsForSubject(subjectCode)

    suspend fun saveSubtopic(subtopic: SyllabusSubtopicEntity): Long = withContext(Dispatchers.IO) {
        db.syllabusSubtopicDao().insertSubtopic(subtopic)
    }

    suspend fun toggleSubtopic(subtopic: SyllabusSubtopicEntity) = withContext(Dispatchers.IO) {
        val updated = subtopic.copy(
            isCompleted = !subtopic.isCompleted,
            lastStudiedTimestamp = System.currentTimeMillis()
        )
        db.syllabusSubtopicDao().updateSubtopic(updated)
    }

    suspend fun updateSubtopicConfidence(subtopic: SyllabusSubtopicEntity, confidence: Int) = withContext(Dispatchers.IO) {
        val updated = subtopic.copy(
            confidenceLevel = confidence.coerceIn(1, 5),
            lastStudiedTimestamp = System.currentTimeMillis()
        )
        db.syllabusSubtopicDao().updateSubtopic(updated)
    }

    // --- Flashcards ---
    fun getAllFlashcardDecks(): Flow<List<FlashcardDeckEntity>> =
        db.flashcardDao().getAllDecks()

    fun getCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>> =
        db.flashcardDao().getCardsForDeck(deckId)

    fun getDueFlashcards(): Flow<List<FlashcardEntity>> =
        db.flashcardDao().getDueCards(System.currentTimeMillis())

    suspend fun createDeck(title: String, subjectCode: String, chapterTitle: String = ""): Long = withContext(Dispatchers.IO) {
        val deck = FlashcardDeckEntity(
            title = title,
            subjectCode = subjectCode,
            chapterTitle = chapterTitle
        )
        db.flashcardDao().insertDeck(deck)
    }

    suspend fun addFlashcards(deckId: Long, subjectCode: String, chapterTitle: String, cards: List<Pair<String, String>>) = withContext(Dispatchers.IO) {
        val entities = cards.map { (q, a) ->
            FlashcardEntity(
                deckId = deckId,
                subjectCode = subjectCode,
                chapterTitle = chapterTitle,
                question = q,
                answer = a,
                nextReviewTimestamp = System.currentTimeMillis()
            )
        }
        db.flashcardDao().insertCards(entities)
        // Update deck count
        val allDeckCards = db.flashcardDao().getCardsForDeck(deckId)
    }

    suspend fun reviewFlashcard(card: FlashcardEntity, knewAnswer: Boolean) = withContext(Dispatchers.IO) {
        val newRepNumber = if (knewAnswer) card.repetitionNumber + 1 else 0
        val newEase = if (knewAnswer) card.easeFactor + 0.1f else (card.easeFactor - 0.2f).coerceAtLeast(1.3f)
        val newIntervalDays = when (newRepNumber) {
            0 -> 1
            1 -> 1
            2 -> 3
            3 -> 6
            else -> (card.intervalDays * newEase).toInt().coerceAtLeast(card.intervalDays + 1)
        }
        val nextTime = System.currentTimeMillis() + (newIntervalDays * 24L * 60L * 60L * 1000L)
        val isMastered = newRepNumber >= 4

        val updated = card.copy(
            repetitionNumber = newRepNumber,
            easeFactor = newEase,
            intervalDays = newIntervalDays,
            nextReviewTimestamp = nextTime,
            totalReviews = card.totalReviews + 1,
            correctReviews = if (knewAnswer) card.correctReviews + 1 else card.correctReviews,
            isMastered = isMastered
        )
        db.flashcardDao().updateCard(updated)
    }

    suspend fun deleteCard(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        db.flashcardDao().deleteCard(card)
    }

    suspend fun deleteDeck(deck: FlashcardDeckEntity) = withContext(Dispatchers.IO) {
        db.flashcardDao().deleteCardsForDeck(deck.id)
        db.flashcardDao().deleteDeck(deck)
    }

    // --- Mistake Notebook ---
    fun getAllMistakes(): Flow<List<MistakeEntity>> = db.mistakeDao().getAllMistakes()

    fun getUnresolvedMistakes(): Flow<List<MistakeEntity>> = db.mistakeDao().getUnresolvedMistakes()

    suspend fun addMistake(
        subjectCode: String,
        chapterTitle: String,
        topicTitle: String = "",
        questionOrContext: String,
        studentMistake: String,
        correctSolution: String,
        coreConcept: String,
        severity: MistakeSeverity = MistakeSeverity.MODERATE,
        whyMade: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val entity = MistakeEntity(
            subjectCode = subjectCode,
            chapterTitle = chapterTitle,
            topicTitle = topicTitle,
            questionOrContext = questionOrContext,
            studentMistake = studentMistake,
            correctSolution = correctSolution,
            coreConcept = coreConcept,
            severity = severity,
            whyMade = whyMade,
            dateCreated = dateFormat.format(Date())
        )
        db.mistakeDao().insertMistake(entity)
    }

    suspend fun toggleMistakeResolved(mistake: MistakeEntity) = withContext(Dispatchers.IO) {
        val updated = mistake.copy(
            isResolved = !mistake.isResolved,
            reviewCount = mistake.reviewCount + 1
        )
        db.mistakeDao().updateMistake(updated)
    }

    suspend fun deleteMistake(id: Long) = withContext(Dispatchers.IO) {
        db.mistakeDao().deleteMistakeById(id)
    }

    // --- Focus Mode App Distraction Logging ---
    fun getAllDistractions(): Flow<List<DistractionLogEntity>> = db.distractionDao().getAllDistractions()

    fun getTodayDistractions(): Flow<List<DistractionLogEntity>> =
        db.distractionDao().getDistractionsForDate(dateFormat.format(Date()))

    suspend fun logDistraction(appName: String, packageName: String = "", urge: String = "", sessionId: Long = 0L) = withContext(Dispatchers.IO) {
        val log = DistractionLogEntity(
            sessionId = sessionId,
            attemptedAppName = appName,
            appPackageName = packageName,
            reasonOrUrge = urge,
            date = dateFormat.format(Date())
        )
        db.distractionDao().insertDistraction(log)
    }
}
