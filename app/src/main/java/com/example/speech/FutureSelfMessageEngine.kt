package com.example.speech

import com.example.data.model.futureself.VoiceLanguage

object FutureSelfMessageEngine {

    /**
     * Returns task-specific motivational quote from Future Self based on subject or title.
     */
    fun getCategoryQuote(subject: String, title: String, customQuote: String = ""): String {
        if (customQuote.isNotBlank()) {
            return customQuote.trim()
        }

        val text = "${subject.trim()} ${title.trim()}".lowercase()
        return when {
            text.contains("physic") ->
                "Every derivation mastered today becomes easy marks tomorrow. Start now."
            text.contains("chem") ->
                "Consistency beats cramming. Start now."
            text.contains("bio") ->
                "NCERT lines learned today become rank tomorrow."
            text.contains("math") ->
                "Precision under pressure is built through daily practice. Do not hesitate."
            text.contains("revis") ->
                "Revision today prevents panic tomorrow."
            text.contains("run") ->
                "Your future body is built by today's run."
            text.contains("workout") || text.contains("gym") || text.contains("calisthenic") || text.contains("exercise") ->
                "Strength comes from completed sessions, not planned sessions."
            text.contains("code") || text.contains("program") || text.contains("dev") ->
                "Clean architecture and daily commits separate the amateur from the master."
            text.contains("mock") || text.contains("test") || text.contains("pyq") ->
                "Every mistake analyzed in test practice is a mark saved on exam day."
            else ->
                "Every chapter you skip today becomes stress tomorrow. Start now."
        }
    }

    /**
     * Speech prompt when student accepts the call (Swipes Right).
     */
    fun buildIgnitionSpeechText(
        subject: String,
        title: String,
        durationMinutes: Int,
        language: VoiceLanguage
    ): String {
        val cleanSubject = subject.ifBlank { "Task" }
        val cleanTitle = title.ifBlank { "Session" }
        return when (language) {
            VoiceLanguage.ENGLISH ->
                "Message from your future self. $cleanSubject $cleanTitle session has started. Duration $durationMinutes minutes. Focus now. Your future depends on today's actions."
            VoiceLanguage.HINDI ->
                "Aapke bhavishya ka sandesh. $cleanSubject $cleanTitle ka session shuru ho gaya hai. Samay $durationMinutes minute. Abhi dhyan lagayein. Aaj ki mehnat kal ka bhavishya banayegi."
            VoiceLanguage.HINGLISH ->
                "Message from future self. $cleanSubject $cleanTitle session start ho chuka hai. Duration $durationMinutes minutes. Pura focus daalo, procrastination band karo."
        }
    }

    /**
     * Speech prompt when student delays the call (Swipes Left).
     */
    fun buildDelaySpeechText(
        delayMinutes: Int,
        newStartTime: String,
        language: VoiceLanguage
    ): String {
        val timeLabel = if (newStartTime.isNotBlank()) newStartTime else "later"
        return when (language) {
            VoiceLanguage.ENGLISH ->
                "Task delayed by $delayMinutes minutes. New start time is $timeLabel. Do not let delay become avoidance."
            VoiceLanguage.HINDI ->
                "Task ko $delayMinutes minute delay kiya gaya hai. Naya samay $timeLabel hai. Delay ko aadat mat banne do."
            VoiceLanguage.HINGLISH ->
                "Task $delayMinutes minutes delay ho gaya. New start time $timeLabel hai. Do not let delay become avoidance."
        }
    }

    /**
     * Speech prompt when Focus Mode finishes.
     */
    fun buildCompletionSpeechText(
        xpReward: Int,
        language: VoiceLanguage = VoiceLanguage.ENGLISH
    ): String {
        return when (language) {
            VoiceLanguage.ENGLISH ->
                "Session Complete. You earned $xpReward XP. Small wins repeated daily become extraordinary results."
            VoiceLanguage.HINDI ->
                "Session Poora hua. Aapne $xpReward XP kamaya. Har din ka chhota prayas bada parinaam lata hai."
            VoiceLanguage.HINGLISH ->
                "Session Complete. You earned $xpReward XP. Small wins repeated daily become extraordinary results."
        }
    }

    /**
     * Speech prompt when a scheduled task is missed.
     */
    fun buildMissedTaskSpeechText(
        subject: String,
        title: String,
        language: VoiceLanguage = VoiceLanguage.ENGLISH
    ): String {
        val cleanSubject = subject.ifBlank { "Task" }
        val cleanTitle = title.ifBlank { "Session" }
        return when (language) {
            VoiceLanguage.ENGLISH ->
                "You missed $cleanSubject $cleanTitle. Missed tasks become future pressure. Choose your next action."
            VoiceLanguage.HINDI ->
                "Aapne $cleanSubject $cleanTitle miss kar diya. Chhuta hua task bhavishya mein dabav banega. Agla kadam chunein."
            VoiceLanguage.HINGLISH ->
                "You missed $cleanSubject $cleanTitle. Missed tasks become future pressure. Choose your next action."
        }
    }

    /**
     * Speech prompt when maximum delay is reached or triggered automatically.
     */
    fun buildMaxDelaySpeechText(
        subject: String,
        title: String,
        durationMinutes: Int,
        language: VoiceLanguage = VoiceLanguage.ENGLISH
    ): String {
        val cleanSubject = subject.ifBlank { "Task" }
        val cleanTitle = title.ifBlank { "Session" }
        return when (language) {
            VoiceLanguage.ENGLISH ->
                "Maximum delay threshold reached. No more excuses. Message from your future self. $cleanSubject $cleanTitle session has started now. Duration $durationMinutes minutes. Focus now. Your future depends on today's actions."
            VoiceLanguage.HINDI ->
                "Maximum delay limit poori ho chuki hai. Ab koi bahaana nahi chalega. Aapke bhavishya ka aadesh: $cleanSubject $cleanTitle session abhi shuru ho chuka hai. Samay $durationMinutes minute. Turant focus karein."
            VoiceLanguage.HINGLISH ->
                "Maximum delay reached! No more delay allowed. Message from future self: $cleanSubject $cleanTitle session has started right now. Duration $durationMinutes minutes. Direct focus mode start ho raha hai, get to work."
        }
    }
}
