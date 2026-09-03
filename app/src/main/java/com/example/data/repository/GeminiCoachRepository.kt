package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AiPlanCacheEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.TaskType
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AiPlanType(val key: String, val title: String, val promptTitle: String) {
    DAILY_SCHEDULE("DAILY_SCHEDULE", "Daily Schedule", "1. Daily Schedule with Realistic Time Blocking"),
    WEEKLY_PLAN("WEEKLY_PLAN", "Weekly Plan", "2. 7-Day Mastery Weekly Plan"),
    REVISION_PLAN("REVISION_PLAN", "Revision Plan", "3. Spaced Repetition Active Recall Revision Plan"),
    WORKOUT_PLAN("WORKOUT_PLAN", "Workout Plan", "4. Physical Training & Cardio Plan"),
    RECOVERY_PLAN("RECOVERY_PLAN", "Recovery Plan", "5. Fatigue Reset & Sleep Recovery Plan"),
    EXAM_STRATEGY("EXAM_STRATEGY", "Exam Strategy", "6. Target Board Exam Strategy"),
    TIME_BLOCKING("TIME_BLOCKING", "Time Blocking", "7. Deep Work Hour-by-Hour Time Blocking"),
    FOCUS_SESSIONS("FOCUS_SESSIONS", "Focus Sessions", "8. High-Yield Focus & Pomodoro Sessions"),
    PRIORITY_TASKS("PRIORITY_TASKS", "Priority Tasks", "9. Top Non-Negotiable Priority Missions")
}

class GeminiCoachRepository(
    private val db: AppDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getAllCachedPlans(): Flow<List<AiPlanCacheEntity>> = db.aiPlanDao().getAllCachedPlans()

    fun getCachedPlan(planType: String): Flow<AiPlanCacheEntity?> = db.aiPlanDao().getCachedPlan(planType)

    suspend fun getCachedPlanDirect(planType: String): AiPlanCacheEntity? = db.aiPlanDao().getCachedPlanDirect(planType)

    suspend fun isPlanCachedForToday(planType: AiPlanType): Boolean = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val cached = db.aiPlanDao().getCachedPlanDirect(planType.key)
        cached != null && cached.content.isNotBlank() && cached.generatedDate == today
    }

    suspend fun clearExpiredAiPlans(daysToKeep: Int = 7) = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L)
        db.aiPlanDao().deleteOldPlans(cutoff)
    }

    suspend fun saveCustomPlan(plan: AiPlanCacheEntity) = withContext(Dispatchers.IO) {
        db.aiPlanDao().insertOrUpdatePlan(plan)
    }

    suspend fun deletePlan(planType: String) = withContext(Dispatchers.IO) {
        db.aiPlanDao().deletePlan(planType)
    }

    suspend fun generateLocalOfflinePlan(type: AiPlanType, prompt: String = ""): String = withContext(Dispatchers.IO) {
        val profile = db.userProfileDao().getUserProfileDirect()
        val basePlan = getOfflineFallbackPlan(type, profile)
        val fullPlan = if (prompt.isNotBlank()) {
            "$basePlan\n\n---\n### 💡 Custom Focus Directive:\n*\"$prompt\"*\n> Local Planner calibrated to target this priority while maintaining your Bihar Board Class 12 syllabus balance and school schedule."
        } else {
            basePlan
        }
        val today = dateFormat.format(Date())
        db.aiPlanDao().insertOrUpdatePlan(
            AiPlanCacheEntity(
                planType = type.key,
                title = type.title,
                content = fullPlan,
                generatedDate = today,
                timestamp = System.currentTimeMillis()
            )
        )
        fullPlan
    }

    suspend fun getEffectiveApiKey(): String {
        val profile = db.userProfileDao().getUserProfileDirect()
        if (profile != null && profile.geminiApiKey.isNotBlank()) {
            return profile.geminiApiKey.trim()
        }
        val userKey = userPreferencesRepository.geminiApiKey.firstOrNull() ?: ""
        if (userKey.isNotBlank()) return userKey.trim()
        val buildKey = BuildConfig.GEMINI_API_KEY
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun buildAppContextSnapshot(): String = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        val profile = db.userProfileDao().getUserProfileDirect()

        // 1. Profile Info
        val studentName = profile?.name ?: "Student"
        val studentClass = profile?.studentClass ?: "Class 12"
        val studentStream = profile?.stream ?: "Science (PCM)"
        val board = profile?.board ?: "CBSE"
        val targetPercentage = profile?.targetPercentage ?: 95
        val coachingStyle = profile?.coachingStyle ?: "Monk Mode (Strict Discipline)"
        val wakeUp = profile?.wakeUpTime ?: "06:00"
        val sleepTime = profile?.sleepTime ?: "22:30"
        val studyGoal = profile?.dailyStudyGoalHours ?: 6.0f
        val workoutGoal = "${profile?.workoutType ?: "Calisthenics"} (${profile?.workoutDurationMinutes ?: 30} mins at ${profile?.workoutTime ?: "17:00"})"

        // 2. Board Exam
        val examConfig = db.boardExamDao().getBoardExamConfigDirect()
        val examName = examConfig?.examName ?: profile?.targetExamName ?: "Board Exam"
        val daysLeft = if (examConfig != null) {
            try {
                val examDate = dateFormat.parse(examConfig.examDate) ?: Date()
                val diff = examDate.time - System.currentTimeMillis()
                maxOf(0L, diff / (1000 * 60 * 60 * 24))
            } catch (e: Exception) { 120L }
        } else 120L

        // 3. Winter Arc
        val winterArc = db.winterArcDao().getWinterArcStateDirect()
        val winterDay = winterArc?.currentDay ?: 1
        val winterLevel = winterArc?.level ?: 1
        val winterStreak = winterArc?.streak ?: 0
        val xp = winterArc?.xp ?: 0

        // 4. School status & timings
        val school = db.schoolStatusDao().getStatusForDateDirect(today)
        val schoolState = school?.currentState?.name ?: "HOME"
        val hasSchool = profile?.hasSchool ?: true
        val schoolHours = if (hasSchool) "${profile?.schoolStartTime ?: "09:45"} Departure - ${profile?.schoolEndTime ?: "13:00"} Return" else "No Regular School (Full Day Self Study)"

        // 5. Subjects & Chapter Progress
        val subjects = db.subjectDao().getAllSubjectsDirect()
        val subjectSummaries = mutableListOf<String>()
        var totalChaptersCount = 0
        var totalCompletedChaptersCount = 0

        for (sub in subjects) {
            val chapters = db.subjectDao().getChaptersForSubjectDirect(sub.id)
            val completed = chapters.count { it.isCompleted }
            totalChaptersCount += chapters.size
            totalCompletedChaptersCount += completed
            val pendingChapters = chapters.filter { !it.isCompleted }.take(3).map { it.title }
            subjectSummaries.add(
                "- ${sub.name}: $completed/${chapters.size} completed. Next up: ${if (pendingChapters.isEmpty()) "All Done!" else pendingChapters.joinToString(", ")}"
            )
        }

        // 6. Habits & Streaks
        val habits = db.habitDao().getAllHabitsDirect()
        val habitLogs = db.habitDao().getLogsForDateDirect(today)
        val habitLogsMap = habitLogs.associateBy { it.habitId }
        val habitSummaries = habits.map { h ->
            val doneToday = habitLogsMap[h.id]?.isCompleted ?: false
            "${h.name} (Streak: ${h.streak}d, Done Today: $doneToday)"
        }

        // 7. Discipline Score
        val discipline = db.disciplineDao().getDisciplineForDateDirect(today)
        val discScore = discipline?.totalScore ?: 0

        // 8. Workouts
        val workouts = db.workoutDao().getWorkoutsForDateDirect(today)
        val workoutSummaries = workouts.map { "${it.exerciseName} (${if (it.isCompleted) "Done" else "Pending"})" }

        // 9. Holidays & Indian Festivals
        val monthDay = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date())
        val holiday = db.holidayDao().getHolidayForDate(today, monthDay)
        val holidayNote = if (holiday != null) "TODAY IS A HOLIDAY/FESTIVAL: ${holiday.name} (Workload adjustment: -${holiday.workloadReductionPercent}%)" else "Regular Day ($schoolHours)"

        // 10. Daily Reflection
        val reflection = db.reflectionDao().getReflectionForDateDirect(today)
        val reflectionSummary = if (reflection != null) {
            "Recent Reflection: Score ${reflection.dailyScore}/10. Wins: ${reflection.whatWentWell}. Hurdles: ${reflection.whatHeldMeBack}. Tomorrow: ${reflection.tomorrowGoal}"
        } else "No reflection logged yet."

        buildString {
            appendLine("=== REBUILD PERSONAL TELEMETRY ===")
            appendLine("Student: $studentName | $studentClass ($board) | Stream: $studentStream | Target: $targetPercentage%")
            appendLine("Coaching Persona: $coachingStyle")
            appendLine("Date: $today ($dayOfWeek) | Status: $holidayNote")
            appendLine("Exam Target: $examName in $daysLeft days. Target: $targetPercentage%")
            appendLine("Syllabus Mastery: $totalCompletedChaptersCount/$totalChaptersCount total chapters completed.")
            appendLine("Winter Arc: Day $winterDay of 90, Level $winterLevel, Streak: $winterStreak days, XP: $xp, Discipline: $discScore/100")
            appendLine("Daily Routine: Wake $wakeUp | Sleep $sleepTime | Daily Study Target: ${studyGoal}h | Workout: $workoutGoal")
            appendLine("School Flow: $schoolHours (Current Live State: $schoolState)")
            appendLine("Active Academic Subjects:")
            if (subjectSummaries.isEmpty()) appendLine("- Initializing subjects") else subjectSummaries.forEach { appendLine(it) }
            appendLine("Habits Matrix:")
            if (habitSummaries.isEmpty()) appendLine("- Initializing habits") else habitSummaries.forEach { appendLine("- $it") }
            appendLine("Workouts Logged Today:")
            if (workoutSummaries.isEmpty()) appendLine("- Pending daily session") else workoutSummaries.forEach { appendLine("- $it") }
            appendLine(reflectionSummary)
            appendLine("==================================")
        }
    }

    suspend fun generatePlan(
        planType: AiPlanType,
        customInstruction: String = "",
        forceRefresh: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val today = dateFormat.format(Date())
            // Fast cache check: Return existing local plan without API calls
            if (!forceRefresh && customInstruction.isBlank()) {
                val cached = db.aiPlanDao().getCachedPlanDirect(planType.key)
                if (cached != null && cached.content.isNotBlank() && cached.generatedDate == today) {
                    return@withContext Result.success(cached.content)
                }
            }

            val apiKey = getEffectiveApiKey()
            val telemetry = buildAppContextSnapshot()
            val profile = db.userProfileDao().getUserProfileDirect()
            val coachingStyle = profile?.coachingStyle ?: "Monk Mode (Strict Discipline)"

            val systemInstruction = """
                You are REBUILD's "Personal Board & Competitive Exam AI Mentor".
                Your coaching tone is strictly calibrated to: $coachingStyle.
                
                CORE INSTRUCTIONS:
                • NEVER overload the schedule. Be realistic, disciplined, and personalized.
                • Adapt strictly to the student's wake up time (${profile?.wakeUpTime ?: "06:00"}), sleep time (${profile?.sleepTime ?: "22:30"}), and school schedule (${if (profile?.hasSchool == true) "${profile.schoolStartTime} to ${profile.schoolEndTime}" else "Full day self study"}).
                • Account for travel time and post-commute cognitive recovery.
                • Prioritize pending chapters in the student's enrolled subjects.
                • Integrate the student's chosen workout (${profile?.workoutType ?: "Calisthenics"}, ${profile?.workoutDurationMinutes ?: 30} mins).
                • Output formatted with clean headers, markdown bullet points, time blocks, and actionable micro-steps.
            """.trimIndent()

            val prompt = """
                $telemetry
                
                TASK: Generate a comprehensive, high-precision "${planType.promptTitle}" for the student.
                ${if (customInstruction.isNotBlank()) "STUDENT SPECIFIC INSTRUCTION: $customInstruction" else ""}
                
                Format with:
                1. 🎯 Strategic Focus & Mindset
                2. ⏱️ Chronological Time Blocking (tailored to user's school and wake/sleep times)
                3. 📚 Core Subject Targets (enrolled subjects & pending chapters)
                4. ⚡ Physical Training / Fitness Block
                5. 🛡️ Discipline & Abstinence Protocols
                6. 💡 Actionable Rule for Today
            """.trimIndent()

            val response = if (apiKey.isNotBlank()) {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.7f),
                    systemInstruction = GeminiContent(
                        role = "system",
                        parts = listOf(GeminiPart(text = systemInstruction))
                    )
                )
                val res = RetrofitClient.geminiService.generateContent(apiKey, request)
                val text = res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    text
                } else {
                    getOfflineFallbackPlan(planType, profile)
                }
            } else {
                getOfflineFallbackPlan(planType, profile)
            }

            db.aiPlanDao().insertOrUpdatePlan(
                AiPlanCacheEntity(
                    planType = planType.key,
                    title = planType.title,
                    content = response,
                    generatedDate = today,
                    timestamp = System.currentTimeMillis()
                )
            )

            Result.success(response)
        } catch (e: Exception) {
            val profile = db.userProfileDao().getUserProfileDirect()
            val fallback = getOfflineFallbackPlan(planType, profile)
            val todayStr = dateFormat.format(Date())
            db.aiPlanDao().insertOrUpdatePlan(
                AiPlanCacheEntity(
                    planType = planType.key,
                    title = planType.title,
                    content = fallback,
                    generatedDate = todayStr,
                    timestamp = System.currentTimeMillis()
                )
            )
            Result.success(fallback)
        }
    }

    suspend fun askCoach(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getEffectiveApiKey()
            val telemetry = buildAppContextSnapshot()
            val profile = db.userProfileDao().getUserProfileDirect()
            val coachingStyle = profile?.coachingStyle ?: "Monk Mode (Strict Discipline)"

            val systemInstruction = """
                You are REBUILD's "Personal AI Mentor".
                Coaching Persona: $coachingStyle.
                You have full access to the student's real-time telemetry: Class, Stream, Target %, School Timings, Syllabus chapters, Habits, and Winter Arc level.
                Provide concise, tactical, highly motivating, and realistic guidance. Reference their actual subjects and data.
            """.trimIndent()

            val prompt = """
                $telemetry
                
                STUDENT'S QUERY:
                "$userMessage"
                
                Give a clear, actionable, and encouraging coaching response.
            """.trimIndent()

            val response = if (apiKey.isNotBlank()) {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.7f),
                    systemInstruction = GeminiContent(
                        role = "system",
                        parts = listOf(GeminiPart(text = systemInstruction))
                    )
                )
                val res = RetrofitClient.geminiService.generateContent(apiKey, request)
                res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: getOfflineCoachAdvice(userMessage, profile)
            } else {
                getOfflineCoachAdvice(userMessage, profile)
            }

            Result.success(response)
        } catch (e: Exception) {
            val profile = db.userProfileDao().getUserProfileDirect()
            Result.success(getOfflineCoachAdvice(userMessage, profile))
        }
    }

    suspend fun applyAiPlanToLocalSchedule(planText: String): Int = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val profile = db.userProfileDao().getUserProfileDirect()
        val subjects = db.subjectDao().getAllSubjectsDirect()
        val generatedTasks = mutableListOf<DailyPlanTaskEntity>()
        var order = 0

        val sessionDuration = profile?.preferredSessionDurationMinutes ?: 50

        if (subjects.isNotEmpty()) {
            for (sub in subjects.take(3)) {
                val pending = db.subjectDao().getChaptersForSubjectDirect(sub.id).filter { !it.isCompleted }
                val chapTitle = pending.firstOrNull()?.title ?: "Chapter Revision"
                generatedTasks.add(
                    DailyPlanTaskEntity(
                        date = today,
                        subject = sub.name,
                        title = "${sub.name}: $chapTitle Deep Work",
                        type = TaskType.LECTURE,
                        details = "AI Scheduled: High-focus concept mastery & NCERT numericals",
                        targetMinutes = sessionDuration,
                        orderIndex = order++,
                        xpReward = 70
                    )
                )
            }
        } else {
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = today,
                    subject = "Study",
                    title = "Deep Work Session 1",
                    type = TaskType.LECTURE,
                    details = "AI Scheduled: High-yield syllabus study block",
                    targetMinutes = sessionDuration,
                    orderIndex = order++,
                    xpReward = 70
                )
            )
        }

        val workoutType = profile?.workoutType ?: "Calisthenics"
        val workoutDur = profile?.workoutDurationMinutes ?: 30
        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "Workout",
                title = "$workoutType Training Block",
                type = TaskType.WORKOUT,
                details = "AI Scheduled: $workoutDur min session to eliminate mental fatigue",
                targetMinutes = workoutDur,
                orderIndex = order++,
                xpReward = 40
            )
        )

        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "General",
                title = "Evening Reflection & Review",
                type = TaskType.CUSTOM,
                details = "AI Scheduled: Daily discipline score audit & active recall summary",
                targetMinutes = 15,
                orderIndex = order++,
                xpReward = 30
            )
        )

        val existing = db.dailyPlanDao().getTasksForDateDirect(today)
        for (task in existing) {
            if (!task.isCompleted) {
                db.dailyPlanDao().deleteTask(task)
            }
        }
        db.dailyPlanDao().insertTasks(generatedTasks)
        generatedTasks.size
    }

    private fun getOfflineFallbackPlan(planType: AiPlanType, profile: com.example.data.local.entity.UserProfileEntity?): String {
        val name = profile?.name ?: "Student"
        val studentClass = profile?.studentClass ?: "Class 12"
        val stream = profile?.stream ?: "Science (PCM)"
        val wake = profile?.wakeUpTime ?: "06:00"
        val sleep = profile?.sleepTime ?: "22:30"
        val hasSchool = profile?.hasSchool ?: true
        val schStart = profile?.schoolStartTime ?: "09:45"
        val schEnd = profile?.schoolEndTime ?: "13:00"
        val workoutType = profile?.workoutType ?: "Calisthenics"
        val workoutTime = profile?.workoutTime ?: "17:00"
        val workoutDur = profile?.workoutDurationMinutes ?: 30

        return when (planType) {
            AiPlanType.DAILY_SCHEDULE, AiPlanType.TIME_BLOCKING -> """
# 🎯 REBUILD Master Schedule • $studentClass ($stream)

**Student:** $name • **Mode:** High Performance Calibration

---

### ⏱️ Dynamic Time-Blocked Structure

• **$wake – Wake Up & Hydration Reset**
  Cold water hydration (500ml), light mobility, zero screens.

• **Morning Focus Block (1.5 Hours)**
  High cognitive clarity window for your highest priority theoretical subject.

${if (hasSchool) "• **$schStart – School Departure & Commute**\n  Depart for school. Active recall formula revision during transit.\n\n• **$schStart – $schEnd | In-School Classes & Practicals**\n  Max focus in practicals and core lectures.\n\n• **$schEnd – Commute Home & Recovery**\n  Post-school warm lunch, hydrate, 20-min power rest." else "• **Full Day Self Study Window**\n  Dedicated morning and afternoon Pomodoro blocks."}

• **Afternoon Deep Study Block**
  2 x 50-min Pomodoro sessions. Focus on problem solving and numericals.

• **$workoutTime | Physical Training ($workoutType - $workoutDur min)**
  Forge physical discipline, elevate dopamine, and dissolve study fatigue.

• **Evening Core Syllabus Mastery**
  NCERT line-by-line reading + Previous Year Questions.

• **Night Revision & Reflection (30 min)**
  Spaced repetition review and evening journal audit in REBUILD Notes.

• **$sleep | Sleep Protocol & Recovery**
  Phone away, dark cool room, 7.5 hours deep sleep.

---
💡 **Rule:** Protect your afternoon and evening deep study blocks. High consistency builds elite exam results.
            """.trimIndent()

            AiPlanType.WEEKLY_PLAN -> """
# 📅 7-Day Mastery Blueprint • $studentClass

• **Monday & Tuesday:** Core Heavyweight Subjects (Foundation & Formulas).
• **Wednesday & Thursday:** Problem Solving, PYQs & Derivations.
• **Friday:** Secondary Subjects & Revision Sweep.
• **Saturday (High Intensity):** Full Timed Mock Test Simulation + Error Log Analysis.
• **Sunday (Active Recovery & Spaced Repetition):** 
  - Morning: Active recall flashcards across all subjects.
  - Afternoon: Review weak chapters and update formula sheets.
  - Evening: Workout reset and AI schedule generation for next week.
            """.trimIndent()

            AiPlanType.REVISION_PLAN -> """
# 🔄 Spaced Repetition Protocol

### Optimal Interval Cycle:
• **Day 1 (Immediate):** 10-minute formula mapping right after studying a chapter.
• **Day 3 (Active Recall):** Solve 5 subjective questions without looking at notes.
• **Day 7 (PYQ Benchmark):** Solve 10 past exam questions under a countdown timer.
• **Day 21 (Mastery Lockdown):** Explain the core concept aloud (Feynman Technique).
            """.trimIndent()

            AiPlanType.WORKOUT_PLAN -> """
# 🏃 Physical Training & Cardio Engine ($workoutType)

• **Warmup (5 min):** Dynamic mobility, arm circles, leg swings.
• **Main Workout ($workoutDur min):**
  1. Pushup Variation: 3 sets of 12-20 reps
  2. Deep Bodyweight Squats: 3 sets of 20 reps
  3. Core Plank Hold: 3 sets of 45-60 seconds
  4. Cardio / Outdoor Cadence Run: 15-20 min
• **Cooldown (5 min):** Deep stretching & box breathing (4-4-4-4).
            """.trimIndent()

            AiPlanType.RECOVERY_PLAN -> """
# 🛡️ Fatigue Reset & Sleep Recovery Protocol

• **Circadian Rhythm:** Morning sunlight exposure within 15 minutes of waking at $wake.
• **Hydration:** Minimum 3.5 Liters daily.
• **Power Rest:** 20-minute Non-Sleep Deep Rest (NSDR) post school/study block.
• **Curfew:** Zero blue light 45 minutes before $sleep.
• **Rest:** 7.5 hours uninterrupted rest for cognitive memory consolidation.
            """.trimIndent()

            AiPlanType.EXAM_STRATEGY -> """
# 🎯 Target Exam Strategy (${profile?.targetPercentage ?: 95}%+ Target)

### Phase 1: Syllabus Mastery
• Complete all remaining chapters with clear concept notes and exemplar questions.
• Benchmark: Aim for ${profile?.dailyStudyGoalHours ?: 6.0f} hours of focused daily study.

### Phase 2: Previous Year Questions & Derivations
• Solve past 10 years question papers under timed conditions.
• Create high-yield formula sheets for rapid daily recall.

### Phase 3: Timed Mock Simulations
• Complete full-length timed mock exams in actual exam slots.
• Eliminate recurring errors and refine presentation.
            """.trimIndent()

            AiPlanType.FOCUS_SESSIONS -> """
# ⏱️ High-Yield Focus & Deep Work Protocol

• **Session 1 (Morning):** 50-min Deep Work • Theory & Difficult Concepts.
• **Session 2 (Afternoon):** 50-min Deep Work • Numericals & Problems.
• **Session 3 (Evening):** 50-min Deep Work • Past Year Questions.
• **Session 4 (Night):** 30-min Rapid Fire Revision & Flashcards.
            """.trimIndent()

            AiPlanType.PRIORITY_TASKS -> """
# ⚡ Today's Non-Negotiable Missions

1. ✅ **Core Subject 1:** Complete targeted chapter section & solve 5 textbook questions.
2. ✅ **Core Subject 2:** 45 minutes of focused problem solving & formula review.
3. ✅ **Workout:** $workoutDur min $workoutType session.
4. ✅ **Discipline:** Zero mindless scrolling & complete evening reflection.
            """.trimIndent()
        }
    }

    suspend fun sendChatMessage(
        userMessage: String,
        persona: com.example.data.local.entity.AiCoachPersona,
        history: List<com.example.data.local.entity.ChatMessageEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getEffectiveApiKey()
            val telemetry = buildAppContextSnapshot()
            val profile = db.userProfileDao().getUserProfileDirect()

            val personaPrompt = when (persona) {
                com.example.data.local.entity.AiCoachPersona.BOARD_EXAM_COACH -> """
                    You are REBUILD's "Senior Board Exam Specialist & Academic Master Coach".
                    You specialize in CBSE & State Board examinations, NCERT mastery, marking scheme blueprints, derivations, formula memorization, and chapter prioritization.
                    Always reference the student's actual enrolled subjects, pending chapters, and study hours from their telemetry data.
                """.trimIndent()

                com.example.data.local.entity.AiCoachPersona.WINTER_ARC_COACH -> """
                    You are REBUILD's "Winter Arc Commander & Monk Mode Architect".
                    Your tone is uncompromising, disciplined, stoic, motivating, and focused on self-mastery, abstinence from digital distractions, cold consistency, physical training, and building undeniable momentum.
                """.trimIndent()

                com.example.data.local.entity.AiCoachPersona.PRODUCTIVITY_MENTOR -> """
                    You are REBUILD's "Elite Cognitive Performance & Productivity Mentor".
                    You specialize in deep work, Pomodoro blocks, time blocking, energy management, active recall, spaced repetition, circadian rhythm alignment, and anti-procrastination systems.
                """.trimIndent()

                com.example.data.local.entity.AiCoachPersona.ACCOUNTABILITY_PARTNER -> """
                    You are REBUILD's "Vigilant Accountability Partner".
                    You review the student's completed tasks, missed goals, wake-up times, and study streaks. You give direct, constructive feedback, call out excuses gently but firmly, celebrate genuine wins, and prescribe exact next actions.
                """.trimIndent()
            }

            val systemInstruction = """
                $personaPrompt
                
                REAL APP TELEMETRY OF THE STUDENT:
                $telemetry
                
                GUIDELINES:
                - Give direct, highly tailored answers using the real numbers from their telemetry (subjects, pending chapters, exam countdown, wake/sleep schedule, etc.).
                - Use clean markdown, bold highlights, concise bullet points, and actionable steps.
                - Keep responses crisp, impactful, and easy to read on mobile.
            """.trimIndent()

            if (apiKey.isNotBlank()) {
                val contents = mutableListOf<GeminiContent>()

                // Add past 6 conversation turns
                history.takeLast(6).forEach { msg ->
                    contents.add(
                        GeminiContent(
                            role = if (msg.role == "user") "user" else "model",
                            parts = listOf(GeminiPart(text = msg.content))
                        )
                    )
                }

                // Add current user message
                contents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessage))
                    )
                )

                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemInstruction))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.7f,
                        topP = 0.95f
                    )
                )

                val response = RetrofitClient.geminiService.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return@withContext Result.success(reply.trim())
                }
            }

            // High Quality Offline Dynamic Fallback
            val offlineReply = generateOfflineChatReply(userMessage, persona, profile)
            Result.success(offlineReply)
        } catch (e: Exception) {
            val fallback = generateOfflineChatReply(userMessage, persona, null)
            Result.success(fallback)
        }
    }

    private suspend fun generateOfflineChatReply(
        userMessage: String,
        persona: com.example.data.local.entity.AiCoachPersona,
        profile: com.example.data.local.entity.UserProfileEntity?
    ): String = withContext(Dispatchers.IO) {
        val lower = userMessage.lowercase()
        val name = profile?.name ?: "Student"
        val exam = profile?.targetExamName ?: "Board Exam"
        val target = profile?.targetPercentage ?: 95
        val studyGoal = profile?.dailyStudyGoalHours ?: 6.0f
        val wake = profile?.wakeUpTime ?: "06:00"
        val sleep = profile?.sleepTime ?: "22:30"
        val workout = profile?.workoutType ?: "Calisthenics"

        val allUnits = db.syllabusDao().getAllUnits().firstOrNull() ?: emptyList()
        val incompletedChapters = db.syllabusDao().getAllChapters().firstOrNull()?.filter { 
            it.status != com.example.data.local.entity.SyllabusStatus.COMPLETED && 
            it.status != com.example.data.local.entity.SyllabusStatus.MASTERED 
        } ?: emptyList()
        val nextChapter = incompletedChapters.firstOrNull()?.title ?: "Electrostatics & Solutions"

        when {
            lower.contains("what should i study") || lower.contains("study today") || lower.contains("chapter") -> {
                """
### 🎯 Recommended Study Block Today:
1. **Primary Focus:** **$nextChapter**
   - 45 min: Core Theory & Formula Derivations
   - 30 min: 10 NCERT Exemplar Numericals / Questions
2. **Secondary Review:** Quick 20-min active recall flashcard sweep before $sleep.
3. **Daily Benchmark:** Maintain your goal of ${studyGoal.toInt()} hours deep work.
                """.trimIndent()
            }

            lower.contains("tomorrow") || lower.contains("plan for tomorrow") -> {
                """
### 📋 Tactical Blueprint for Tomorrow:
• **$wake:** Wake up on first alarm, 500ml hydration + 15 min morning mobility.
• **Morning Slot:** 90 min deep study on **$nextChapter**.
• **Afternoon:** Complete school/coaching assignments & summarize key formulas.
• **17:00:** 30 min $workout session.
• **Night:** 45 min revision + evening reflection before $sleep curfew.
                """.trimIndent()
            }

            lower.contains("behind") || lower.contains("schedule") || lower.contains("progress") -> {
                val completedCount = incompletedChapters.size
                """
### 📊 Syllabus Status & Schedule Analysis:
• **Target:** $target% in $exam
• **Chapters Pending:** ${incompletedChapters.size} total across your enrolled syllabus.
• **Assessment:** You have ample runway if you lock in 2 focused chapters per week. Maintain your ${studyGoal}h daily pace and avoid skipping weekend revision blocks.
                """.trimIndent()
            }

            lower.contains("workout") || lower.contains("fitness") || lower.contains("exercise") -> {
                """
### ⚡ $workout Protocol for Today:
• **Warm-up (5 min):** Arm swings, torso twists, high knees.
• **Circuit (3 Rounds):**
  1. Standard / Incline Pushups: 15-20 reps
  2. Air Squats / Lunges: 20 reps
  3. Plank Hold: 45 seconds
  4. Jumping Jacks / Shadow Boxing: 60 seconds
• **Cooldown:** Deep breathing and hydration.
                """.trimIndent()
            }

            else -> {
                when (persona) {
                    com.example.data.local.entity.AiCoachPersona.BOARD_EXAM_COACH ->
                        "**Board Coach:** $name, focus on mastering the concepts of **$nextChapter**. Solve 5 PYQs today to solidify the marking scheme understanding. What specific question or topic can I break down for you?"

                    com.example.data.local.entity.AiCoachPersona.WINTER_ARC_COACH ->
                        "**Winter Arc Commander:** Zero excuses, $name. The 90-day arc requires relentless execution. Wake up at $wake, hit your ${studyGoal}h study block, and finish your $workout session. Stay disciplined."

                    com.example.data.local.entity.AiCoachPersona.PRODUCTIVITY_MENTOR ->
                        "**Productivity Mentor:** Eliminate digital noise. Set a 50-minute Pomodoro timer, place your phone in another room, and dive deep into your highest priority chapter right now."

                    com.example.data.local.entity.AiCoachPersona.ACCOUNTABILITY_PARTNER ->
                        "**Accountability Partner:** Checking in on your daily goals! Have you logged your study session and habits today? Let's keep your streak intact."
                }
            }
        }
    }

    private fun getOfflineCoachAdvice(userMessage: String, profile: com.example.data.local.entity.UserProfileEntity?): String {
        val lower = userMessage.lowercase()
        val name = profile?.name ?: "Student"
        return when {
            lower.contains("tired") || lower.contains("fatigue") || lower.contains("exhaust") ->
                "Coach: $name, fatigue is completely natural. Take a 20-minute power nap or do 5 minutes of box breathing with 500ml cold water. When you return to your desk, start with a 15-minute easy review before tackling complex problems."

            lower.contains("formula") || lower.contains("derivation") || lower.contains("physics") || lower.contains("math") ->
                "Coach: The best way to lock in formulas is active derivation and daily flashcard sweeps. Write out every key formula with units on a dedicated sheet every morning."

            lower.contains("exam") || lower.contains("score") || lower.contains("target") || lower.contains("95") ->
                "Coach: Target ${profile?.targetPercentage ?: 95}%+ is built day by day. Stick to your ${profile?.dailyStudyGoalHours ?: 6.0f} hours study goal and complete your daily missions with full discipline."

            lower.contains("habit") || lower.contains("streak") || lower.contains("porn") || lower.contains("scroll") ->
                "Coach: Stay locked in. When you feel the urge to procrastinate or scroll, immediately drop for 10 pushups or drink a glass of water. Guard your mental focus."

            else ->
                "Coach: Stay consistent and execute today's time blocks. You have all the tools in REBUILD to achieve your goals. What subject are we tackling next?"
        }
    }
}
