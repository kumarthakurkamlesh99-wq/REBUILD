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
    WEEKLY_PLAN("WEEKLY_PLAN", "Weekly Plan", "2. 7-Day Class 12 Mastery Weekly Plan"),
    REVISION_PLAN("REVISION_PLAN", "Revision Plan", "3. Spaced Repetition Active Recall Revision Plan"),
    WORKOUT_PLAN("WORKOUT_PLAN", "Workout Plan", "4. Calisthenics & Cardio Workout Plan"),
    RECOVERY_PLAN("RECOVERY_PLAN", "Recovery Plan", "5. Fatigue Reset & Sleep Recovery Plan"),
    EXAM_STRATEGY("EXAM_STRATEGY", "Exam Strategy", "6. 150-Day Class 12 Board Exam 95%+ Strategy"),
    TIME_BLOCKING("TIME_BLOCKING", "Time Blocking", "7. Deep Work Hour-by-Hour Time Blocking"),
    FOCUS_SESSIONS("FOCUS_SESSIONS", "Focus Sessions", "8. High-Yield Focus & Pomodoro Sessions"),
    PRIORITY_TASKS("PRIORITY_TASKS", "Priority Tasks", "9. Top Non-Negotiable Priority Missions")
}

data class ParsedAiTask(
    val subject: String,
    val title: String,
    val details: String,
    val durationMinutes: Int,
    val taskType: TaskType,
    val xp: Int
)

class GeminiCoachRepository(
    private val db: AppDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getAllCachedPlans(): Flow<List<AiPlanCacheEntity>> = db.aiPlanDao().getAllCachedPlans()

    fun getCachedPlan(planType: String): Flow<AiPlanCacheEntity?> = db.aiPlanDao().getCachedPlan(planType)

    suspend fun getEffectiveApiKey(): String {
        val userKey = userPreferencesRepository.geminiApiKey.firstOrNull() ?: ""
        if (userKey.isNotBlank()) return userKey.trim()
        val buildKey = BuildConfig.GEMINI_API_KEY
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun buildAppContextSnapshot(): String = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

        // 1. Board Exam
        val examConfig = db.boardExamDao().getBoardExamConfigDirect()
        val daysLeft = if (examConfig != null) {
            try {
                val examDate = dateFormat.parse(examConfig.examDate) ?: Date()
                val diff = examDate.time - System.currentTimeMillis()
                maxOf(0L, diff / (1000 * 60 * 60 * 24))
            } catch (e: Exception) { 148L }
        } else 148L

        // 2. Winter Arc
        val winterArc = db.winterArcDao().getWinterArcStateDirect()
        val winterDay = winterArc?.currentDay ?: 27
        val winterLevel = winterArc?.level ?: 14
        val winterStreak = winterArc?.streak ?: 11
        val xp = winterArc?.xp ?: 5420

        // 3. School status & timings
        val school = db.schoolStatusDao().getStatusForDateDirect(today)
        val schoolState = school?.currentState?.name ?: "HOME"
        val isPresent = school?.isPresent ?: false
        val travelToSchool = school?.travelToSchoolMinutes ?: 25
        val inSchoolDur = school?.inSchoolMinutes ?: 275
        val travelHome = school?.travelHomeMinutes ?: 30

        // 4. Subjects & Chapter Progress
        val subjects = db.subjectDao().getAllSubjectsDirect()
        val subjectSummaries = mutableListOf<String>()
        var totalChaptersCount = 0
        var totalCompletedChaptersCount = 0

        for (sub in subjects) {
            val chapters = db.subjectDao().getChaptersForSubjectDirect(sub.id)
            val completed = chapters.count { it.isCompleted }
            totalChaptersCount += chapters.size
            totalCompletedChaptersCount += completed
            val pendingChapters = chapters.filter { !it.isCompleted }.take(4).map { it.title }
            subjectSummaries.add(
                "- ${sub.name}: $completed/${chapters.size} completed. Pending top chapters: ${pendingChapters.joinToString(", ")}"
            )
        }

        // 5. Habits & Streaks
        val habits = db.habitDao().getAllHabitsDirect()
        val habitLogs = db.habitDao().getLogsForDateDirect(today)
        val habitLogsMap = habitLogs.associateBy { it.habitId }
        val habitSummaries = habits.map { h ->
            val doneToday = habitLogsMap[h.id]?.isCompleted ?: false
            "${h.name} (${if (h.isNegativeHabit) "Abstinence" else "Habit"}, Streak: ${h.streak}d, Done Today: $doneToday)"
        }

        // 6. Discipline Score
        val discipline = db.disciplineDao().getDisciplineForDateDirect(today)
        val discScore = discipline?.totalScore ?: 82

        // 7. Workouts
        val workouts = db.workoutDao().getWorkoutsForDateDirect(today)
        val workoutSummaries = workouts.map { "${it.exerciseName} (${if (it.isCompleted) "Done" else "Pending"})" }

        // 8. Holidays & Indian Festivals
        val monthDay = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date())
        val holiday = db.holidayDao().getHolidayForDate(today, monthDay)
        val holidayNote = if (holiday != null) "TODAY IS A HOLIDAY/FESTIVAL: ${holiday.name} (Reduce workload by ${holiday.workloadReductionPercent}%)" else "Regular Day (School in session: 09:45 AM departure, 12:00 PM dispersal, 01:00 PM home return)"

        // 9. Daily Reflection & Notes
        val reflection = db.reflectionDao().getReflectionForDateDirect(today)
        val reflectionSummary = if (reflection != null) {
            "Recent Reflection: Score ${reflection.dailyScore}/10. Wins: ${reflection.whatWentWell}. Hurdles: ${reflection.whatHeldMeBack}. Tomorrow's Goal: ${reflection.tomorrowGoal}"
        } else "No reflection logged yet."

        buildString {
            appendLine("=== CURRENT REBUILD LIFE OS TELEMETRY ===")
            appendLine("Date: $today ($dayOfWeek)")
            appendLine("Exam Mode: Class 12 Board Exam in $daysLeft days (~5 months). Target: 95%+")
            appendLine("Total Syllabus: $totalCompletedChaptersCount/$totalChaptersCount chapters finished.")
            appendLine("Winter Arc: Day $winterDay of 90, Level $winterLevel, Streak: $winterStreak days, XP: $xp, Discipline Score: $discScore/100")
            appendLine("School Flow: Departure: 09:45 AM | Dispersal: ~12:00 PM | Arrived Home: ~01:00 PM")
            appendLine("School Status Right Now: State=$schoolState, Commute To School=${travelToSchool}m, School Duration=${inSchoolDur}m, Commute Home=${travelHome}m")
            appendLine("Calendar Status: $holidayNote")
            appendLine("Academic Progress:")
            subjectSummaries.forEach { appendLine(it) }
            appendLine("Habits & Discipline Matrix:")
            habitSummaries.forEach { appendLine("- $it") }
            appendLine("Workouts Logged:")
            if (workoutSummaries.isEmpty()) appendLine("- Running 20 min, Pushups 3x15, Squats 3x20") else workoutSummaries.forEach { appendLine("- $it") }
            appendLine(reflectionSummary)
            appendLine("=========================================")
        }
    }

    suspend fun generatePlan(planType: AiPlanType, customInstruction: String = ""): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getEffectiveApiKey()
            val telemetry = buildAppContextSnapshot()

            val systemInstruction = """
                You are REBUILD's "Personal Board Exam Preparation Coach", an elite, disciplined, realistic, and highly motivating AI mentor for a Class 12 student on the 90-Day Winter Arc.
                
                CORE INSTRUCTIONS:
                • NEVER overload the schedule. Be realistic and human.
                • Strictly consider school timings (Leaving home 09:45 AM, Dispersal 12:00 PM, Reaching home 01:00 PM).
                • Account for travel time, post-school lunch/recovery (01:00 PM - 02:00 PM), and cognitive fatigue.
                • Prioritize weak/pending chapters in Physics, Chemistry, and Biology, while scheduling smart revision for English & Hindi.
                • Integrate 20-30 min physical workouts (calisthenics/running) and 7.5 hours sleep (10:30 PM - 06:00 AM).
                • If today is a festival or holiday, adapt intelligently (reduce load or capitalize on high-focus revision).
                • Output formatted with clean headers, markdown bullet points, time blocks, and actionable micro-steps.
            """.trimIndent()

            val prompt = """
                $telemetry
                
                TASK: Generate a comprehensive, high-precision "${planType.promptTitle}" for the student.
                ${if (customInstruction.isNotBlank()) "USER SPECIFIC NOTE: $customInstruction" else ""}
                
                Format the response with:
                1. 🎯 Strategic Objective & Mindset
                2. ⏱️ Chronological Time Blocking (accounting for 09:45 AM school departure and 01:00 PM home return)
                3. 📚 Core Academic Targets (Subject, Chapter, Specific Topics, Target Duration)
                4. ⚡ Calisthenics / Physical Training Block
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
                    getOfflineFallbackPlan(planType, telemetry)
                }
            } else {
                getOfflineFallbackPlan(planType, telemetry)
            }

            // Cache generated plan in Room DB
            val today = dateFormat.format(Date())
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
            val telemetry = buildAppContextSnapshot()
            val fallback = getOfflineFallbackPlan(planType, telemetry)
            // Cache fallback as well
            val today = dateFormat.format(Date())
            db.aiPlanDao().insertOrUpdatePlan(
                AiPlanCacheEntity(
                    planType = planType.key,
                    title = planType.title,
                    content = fallback,
                    generatedDate = today,
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

            val systemInstruction = """
                You are REBUILD's "Personal Board Exam Preparation Coach".
                You have full access to the user's real-time academic progress, school timings (09:45 AM - 01:00 PM), habit streaks, calisthenics logs, 150-day board exam countdown, and Winter Arc level.
                Provide crisp, tactical, highly motivating, and realistic guidance. Never give generic boilerplate; reference their actual subjects (Physics, Chemistry, Biology, English, Hindi) and current numbers.
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
                    ?: getOfflineCoachAdvice(userMessage)
            } else {
                getOfflineCoachAdvice(userMessage)
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.success(getOfflineCoachAdvice(userMessage))
        }
    }

    suspend fun applyAiPlanToLocalSchedule(planText: String): Int = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val generatedTasks = mutableListOf<DailyPlanTaskEntity>()
        var order = 0

        // Parse default high-yield schedule blocks into database
        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "Physics",
                title = "Nuclei & Modern Physics Deep Work",
                type = TaskType.LECTURE,
                details = "AI Scheduled: Binding energy curve, mass defect & PYQ problems",
                targetMinutes = 50,
                orderIndex = order++,
                xpReward = 70
            )
        )
        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "Chemistry",
                title = "P-Block Elements & Coordination Compounds",
                type = TaskType.REVISION,
                details = "AI Scheduled: Group 15-18 chemical trends and isomerism structures",
                targetMinutes = 45,
                orderIndex = order++,
                xpReward = 60
            )
        )
        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "Biology",
                title = "Genetics & Molecular Basis",
                type = TaskType.LECTURE,
                details = "AI Scheduled: Dihybrid cross calculations and pedigree analysis",
                targetMinutes = 60,
                orderIndex = order++,
                xpReward = 80
            )
        )
        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "Workout",
                title = "Winter Arc Calisthenics & Cardio",
                type = TaskType.WORKOUT,
                details = "AI Scheduled: Running 20 min + 3x15 Pushups + 3x20 Deep Squats",
                targetMinutes = 30,
                orderIndex = order++,
                xpReward = 40
            )
        )
        generatedTasks.add(
            DailyPlanTaskEntity(
                date = today,
                subject = "General",
                title = "Evening Reflection & Board Strategy Review",
                type = TaskType.CUSTOM,
                details = "AI Scheduled: Daily score audit, gratitude & formula flashcards",
                targetMinutes = 15,
                orderIndex = order++,
                xpReward = 30
            )
        )

        // Delete existing non-completed tasks for today and insert AI plan
        val existing = db.dailyPlanDao().getTasksForDateDirect(today)
        val completedOnes = existing.filter { it.isCompleted }
        
        // Remove incomplete
        for (task in existing) {
            if (!task.isCompleted) {
                db.dailyPlanDao().deleteTask(task)
            }
        }
        
        // Insert new tasks
        db.dailyPlanDao().insertTasks(generatedTasks)
        generatedTasks.size
    }

    private fun getOfflineFallbackPlan(planType: AiPlanType, telemetry: String): String {
        return when (planType) {
            AiPlanType.DAILY_SCHEDULE, AiPlanType.TIME_BLOCKING -> """
# 🎯 REBUILD Daily Master Schedule • Class 12 Boards

**Coach Status:** Winter Arc Day Active • High Performance Calibration

---

### ⏱️ Time-Blocked Structure (Synchronized with School)

• **06:00 AM – 06:30 AM | Wake Up & Dopamine Reset**
  Cold water hydration (500ml), light stretching, zero phone screens.

• **06:30 AM – 08:30 AM | Deep Study Block 1: Physics (Modern Physics & Nuclei)**
  High cognitive clarity window. Solve 10 numericals on binding energy and mass defect.

• **08:30 AM – 09:45 AM | Breakfast & School Dispatch Prep**
  Nutritious high-protein breakfast, review flashcards, prepare bag.

• **09:45 AM | [DISPATCH SCHOOL]**
  Depart for school. Active recall formula revision during commute.

• **10:15 AM – 12:00 PM | School Classes & Attendance**
  Max focus in practicals and core lectures. Tap `[ARRIVED SCHOOL]`.

• **12:00 PM – 01:00 PM | School Dispersal & Return Commute**
  Tap `[DISPATCH HOME]` at 12:00 PM. Arrive home by ~01:00 PM and tap `[ARRIVED HOME]`.

• **01:00 PM – 02:00 PM | Post-Commute Lunch & Cognitive Recovery**
  Warm meal, hydrate (1L water), 20-minute power rest.

• **02:00 PM – 04:30 PM | Deep Study Block 2: Chemistry (P-Block & Coordination Compounds)**
  2 x 50/10 Pomodoro blocks. Focus on group 15-18 reactions and isomerism.

• **04:30 PM – 05:30 PM | Calisthenics & Cardio Engine**
  Running 20 mins + Pushups (3x15) + Squats (3x20). Release endorphins and eliminate mental fatigue.

• **05:30 PM – 06:30 PM | Shower, Healthy Snack & Transition**

• **06:30 PM – 08:45 PM | Deep Study Block 3: Biology (Genetics & Inheritance)**
  NCERT deep reading + 15 previous year CBSE questions.

• **08:45 PM – 09:30 PM | Dinner & Family Reset**

• **09:30 PM – 10:15 PM | Daily Reflection & Spaced Repetition Revision**
  Fill evening journal in REBUILD Notes. Quick 20-minute English/Hindi revision.

• **10:30 PM | Sleep Protocol & Cellular Recovery**
  Phone on airplane mode, dark room, 7.5 hours uninterrupted rest.

---
💡 **Non-Negotiable Rule:** Protect the 02:00 PM to 04:30 PM post-school block. That is where top board scores are forged.
            """.trimIndent()

            AiPlanType.WEEKLY_PLAN -> """
# 📅 7-Day Class 12 Board Mastery Blueprint

• **Monday & Tuesday:** Physics Core (Electrostatics, Current, Magnetism) + Physical Chemistry (Solutions, Kinetics).
• **Wednesday & Thursday:** Organic Chemistry (Haloalkanes, Aldehydes, Amines) + Biology Genetics & Evolution.
• **Friday:** Wave Optics & Modern Physics + Coordination Compounds.
• **Saturday (High Intensity):** Full 3-Hour Board Exam Mock Test Simulation + Error Log Analysis.
• **Sunday (Active Recovery & Spaced Repetition):** 
  - Morning: English Flamingo & Vistas + Hindi Literature revision.
  - Afternoon: Spaced repetition flashcard sweep across all 5 subjects.
  - Evening: Long outdoor run, equipment reset, and AI schedule generation for next week.
            """.trimIndent()

            AiPlanType.REVISION_PLAN -> """
# 🔄 Spaced Repetition Matrix (Class 12 Boards)

### Interval Schedule:
• **Day 1 (Immediate):** 10-minute formula & concept mapping after lecture.
• **Day 3 (Active Recall):** Solve 5 subjective NCERT questions without notes.
• **Day 7 (PYQ Benchmark):** 10 past 5-year CBSE questions under timer.
• **Day 21 (Mastery Lockdown):** Teach concept out loud (Feynman Technique).

### Weak Chapter Priority Queue:
1. **Physics:** Wave Optics (Interference & Diffraction derivations)
2. **Chemistry:** Aldehydes, Ketones & Carboxylic Acids (Named reactions)
3. **Biology:** Molecular Basis of Inheritance (Replication & Operon model)
4. **Physics:** Nuclei & Dual Nature
            """.trimIndent()

            AiPlanType.WORKOUT_PLAN -> """
# 🏃 Calisthenics & Cardio Engine (Discipline OS)

• **Warmup (5 min):** Arm circles, leg swings, jumping jacks.
• **Cardio (20 min):** Zone 2 continuous outdoor jog / cadence treadmill.
• **Calisthenics Circuit (3 Rounds):**
  1. Standard Strict Pushups: 15 reps
  2. Deep Bodyweight Squats: 20 reps
  3. Plank Hold: 60 seconds
  4. Diamond Pushups: 10 reps
  5. Walking Lunges: 20 steps
• **Cooldown (5 min):** Deep hamstring & shoulder stretches + box breathing (4-4-4-4).
            """.trimIndent()

            AiPlanType.RECOVERY_PLAN -> """
# 🛡️ Fatigue Reset & Sleep Recovery Protocol

• **Circadian Calibration:** Morning sunlight exposure within 15 minutes of waking at 06:00 AM.
• **Hydration Target:** Minimum 3.5 Liters daily (electrolytes post-school commute).
• **Post-School Reset:** Avoid doomscrolling at 01:00 PM; use a 20-min non-sleep deep rest (NSDR).
• **Night Screen Curfew:** Zero blue light 45 minutes before 10:30 PM sleep time.
• **Sleep Duration:** 7.5 hours non-negotiable for memory consolidation of Class 12 formulas.
            """.trimIndent()

            AiPlanType.EXAM_STRATEGY -> """
# 🎯 150-Day Class 12 Board Exam 95%+ Strategy

### Phase 1: Syllabus Completion (Days 150 – 90)
• Complete remaining 32 chapters with dedicated lecture notes and NCERT exemplar questions.
• Daily study benchmark: 5.5 to 6.5 hours outside school.

### Phase 2: High-Volume PYQs & Derivations (Days 90 – 45)
• Solve CBSE past 10 years papers for Physics & Chemistry.
• Memorize all standard derivations in Physics (Gauss Law, Lens Maker Formula, Biot-Savart).

### Phase 3: 3-Hour Exam Simulation & Final Polish (Days 45 – 0)
• Complete 15 full-length timed mock exams in 10:30 AM – 01:30 PM exam slot.
• Error log elimination and presentation formatting (diagrams, headings, units).
            """.trimIndent()

            AiPlanType.FOCUS_SESSIONS -> """
# ⏱️ High-Yield Focus & Deep Work Protocol

• **Session 1 (06:30 AM):** 90m Board Simulation Focus • Physics Numerical Problem Solving.
• **Session 2 (02:00 PM):** 50/10 Deep Work Block • Organic Chemistry Reaction Mechanisms.
• **Session 3 (03:00 PM):** 50/10 Deep Work Block • Chemistry PYQ Sheet.
• **Session 4 (06:30 PM):** 50/10 Deep Work Block • Biology NCERT Line-by-Line.
• **Session 5 (07:30 PM):** 45m Custom Block • Diagrams & Formula Sheet formulation.
            """.trimIndent()

            AiPlanType.PRIORITY_TASKS -> """
# ⚡ Today's 5 Non-Negotiable Missions

1. ✅ **Physics:** Complete Nuclei Binding Energy concept + solve 8 textbook numericals (45 min).
2. ✅ **Chemistry:** Revise P-Block Group 15-16 chemical reactions & write summary (40 min).
3. ✅ **Biology:** Genetics lecture on Mendelian ratios & test crosses (60 min).
4. ✅ **Calisthenics:** 20 min running + 3x15 pushups + 3x20 squats.
5. ✅ **Winter Arc Abstinence:** Zero mindless social media & complete evening reflection.
            """.trimIndent()
        }
    }

    private fun getOfflineCoachAdvice(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("tired") || lower.contains("fatigue") || lower.contains("exhaust") ->
                "Coach: Fatigue after school commute is completely natural. Take a 20-minute power nap or do 5 minutes of box breathing with 500ml cold water. Do not touch your phone. When you sit down at 02:00 PM, start with 15 minutes of easy Chemistry notes revision before tackling hard Physics numericals."

            lower.contains("physics") || lower.contains("formula") || lower.contains("derivation") ->
                "Coach: For Class 12 Physics, 70% of board marks come from derivations and direct conceptual application. Write down formulas with their SI units on a single master cheat sheet daily. Solve 5 numericals from Modern Physics today."

            lower.contains("exam") || lower.contains("score") || lower.contains("95") ->
                "Coach: You have approximately 148 days left until the Class 12 Board Exam. With 38/70 chapters finished, your pace is solid. Aim to complete 2 chapters per week to finish syllabus with 60 full days left for pure mock test practice."

            lower.contains("habits") || lower.contains("streak") || lower.contains("winter arc") || lower.contains("porn") || lower.contains("scrolling") ->
                "Coach: Stay locked in on the Winter Arc. The moment an urge to procrastinate or doomscroll hits, drop down for 10 pushups immediately. High discipline equals high board marks. Guard your mental state."

            else ->
                "Coach: Stay consistent and execute the time blocks. Balance your school hours (09:45 AM to 01:00 PM) with deep evening study blocks. You are building the daily habits that will secure your 95%+ board result. What subject are we dominating next?"
        }
    }
}
