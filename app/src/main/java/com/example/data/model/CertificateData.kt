package com.example.data.model

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CertificateData(
    val studentName: String = "Kamlesh Kumar Thakur",
    val studentClass: String = "Class 12 • Science (PCM)",
    val level: Int = 12,
    val rankTitle: String = "Alpha",
    val levelName: String = "Level $level - $rankTitle",
    val xp: Int = 14850,
    val totalXP: String = String.format(Locale.US, "%,d XP", xp),
    val winterArcDay: Int = 1,
    val arcDay: String = "Day $winterArcDay",
    val streak: Int = 18,
    val issueDate: String = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date()),
    val dateAchieved: String = issueDate,
    val certificateId: String = generateCertificateId(12),
    val verificationHash: String = generateVerificationHash("Kamlesh Kumar Thakur", 12, 14850),
    val aiEvaluation: String = "\"The protocol rewards action,\nnot intention.\"",
    val achievementDescription: String = "IN RECOGNITION OF DEDICATED ACHIEVEMENT",
    val achievementParagraph: String = "For successfully unlocking and mastering $levelName through demonstrated consistency, self-discipline, and daily focus in the REBUILD protocol."
) {
    /**
     * Achievement Statement (Strict 4-Line Layout):
     * "This certificate is awarded to {studentName}
     * for successfully unlocking
     * {levelName}
     * through demonstrated discipline,
     * consistency and self-improvement."
     */
    fun getAchievementLines(): List<String> = listOf(
        "This certificate is awarded to $studentName",
        "for successfully unlocking $levelName",
        "through demonstrated discipline,",
        "consistency and self-improvement."
    )

    fun getAchievementText(): String = getAchievementLines().joinToString("\n")

    val levelInfoLine: String
        get() = "Level: $levelName   •   XP: $totalXP   •   Arc Day: $arcDay"

    val quoteLines: List<String>
        get() = listOf(
            "\"The protocol rewards action,",
            "not intention.\""
        )

    companion object {
        fun generateCertificateId(level: Int): String {
            val randomSuffix = (1000..9999).random().toString(16).uppercase(Locale.US)
            return "RBLD-CERT-2026-L$level-$randomSuffix"
        }

        fun generateVerificationHash(name: String, level: Int, xp: Int): String {
            val input = "$name-REBUILD-L$level-XP$xp-${System.currentTimeMillis()}"
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }.take(32).uppercase(Locale.US)
        }

        fun defaultEvaluationForLevel(level: Int, rankTitle: String): String {
            return "\"The protocol rewards action,\nnot intention.\""
        }
    }
}

