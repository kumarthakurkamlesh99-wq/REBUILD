package com.example.data.model

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CertificateData(
    val studentName: String = "Kamlesh Kumar Thakur",
    val studentClass: String = "Class 12 • Science (PCM)",
    val winterArcDay: Int = 1,
    val level: Int = 12,
    val rankTitle: String = "Alpha",
    val xp: Int = 14850,
    val streak: Int = 18,
    val dateAchieved: String = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date()),
    val certificateId: String = generateCertificateId(12),
    val verificationHash: String = generateVerificationHash("Kamlesh Kumar Thakur", 12, 14850),
    val aiEvaluation: String = "Exemplary adherence to the REBUILD Protocol. Subject exhibits relentless cognitive endurance, unwavering discipline, and structured academic dominance under intense timeline pressure."
) {
    fun getAchievementText(): String {
        return "This certificate is proudly awarded to $studentName for successfully achieving Level $level – $rankTitle through demonstrated discipline, consistency, commitment, and continuous self-improvement within the REBUILD protocol."
    }

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
            return when {
                level >= 22 -> "Unmatched mental discipline. Subject has transcended civilian habits and operates with peak biological and cognitive dominance within the REBUILD protocol."
                level >= 16 -> "High-tier execution demonstrated. Relentless consistency in deep work, structured physical conditioning, and rigorous academic mastery."
                level >= 10 -> "Substantial transformation recorded. Habit loops stabilized, distractions curtailed, and target performance consistently delivered."
                level >= 6 -> "Active commitment verified. Breaking out of comfort zones and building foundational momentum towards the target examination."
                else -> "Initial protocol compliance initiated. Urgency must be elevated to maintain required trajectory toward academic victory."
            }
        }
    }
}
