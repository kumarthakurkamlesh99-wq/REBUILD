package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.ExerciseType
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HabitType
import com.example.data.local.entity.HolidayEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.SessionType
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.local.entity.WorkoutLevel
import com.example.data.local.entity.WorkoutLogEntity
import com.example.notification.AlarmScheduler
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.AlarmLogEntity
import com.example.data.local.entity.ArcGoalPlanItemEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.SyllabusChapterEntity
import com.example.data.local.entity.SyllabusStatus
import com.example.data.local.entity.SyllabusTopicEntity
import com.example.data.local.entity.SyllabusUnitEntity
import com.example.data.local.entity.WinterArcObjectiveEntity
import com.example.data.master.MasterSyllabusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class RebuildRepository(
    private val db: AppDatabase,
    private val context: Context? = null
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

    // ----------------------------------------------------
    // USER PROFILE & ONBOARDING SYSTEM
    // ----------------------------------------------------

    fun getUserProfile(): Flow<UserProfileEntity?> = db.userProfileDao().getUserProfile()

    suspend fun getUserProfileDirect(): UserProfileEntity? = db.userProfileDao().getUserProfileDirect()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdate(profile)
        if (context != null && profile.isCompleted) {
            AlarmScheduler.scheduleProfileAlarms(context, profile)
        }
    }

    suspend fun initializeUserSystem(profile: UserProfileEntity) {
        val today = getTodayDateString()
        val completedProfile = profile.copy(
            isCompleted = true,
            updatedAtTimestamp = System.currentTimeMillis()
        )
        db.userProfileDao().insertOrUpdate(completedProfile)

        // 1. Initial Clean Winter Arc (Day 1, 0 XP, Level 1, 0 streak)
        db.winterArcDao().insertOrUpdate(
            WinterArcStateEntity(
                id = 1,
                startDate = today,
                targetDays = 90,
                currentDay = 1,
                xp = 0,
                level = 1,
                streak = 0,
                bestStreak = 0,
                transformationScore = 0,
                targetDailyDeepWorkHours = completedProfile.dailyStudyGoalHours
            )
        )

        // 2. Generate Syllabus & Subjects based on Stream
        val templates = SyllabusTemplateProvider.getTemplatesForStream(
            completedProfile.studentClass,
            completedProfile.stream
        )

        var totalChaptersCount = 0
        var subjectOrder = 0

        for (template in templates) {
            val subjectId = db.subjectDao().insertSubject(
                SubjectEntity(
                    name = template.name,
                    code = template.code,
                    iconName = template.iconName,
                    colorHex = template.colorHex,
                    totalChapters = template.chapters.size,
                    completedChapters = 0,
                    targetHours = template.targetHours,
                    orderIndex = subjectOrder++
                )
            )

            val chapterEntities = template.chapters.mapIndexed { index, title ->
                totalChaptersCount++
                ChapterEntity(
                    subjectId = subjectId,
                    chapterNumber = index + 1,
                    title = title,
                    totalLectures = 5,
                    completedLectures = 0,
                    notesDone = false,
                    pyqsDone = false,
                    revisionCount = 0,
                    isCompleted = false,
                    completionPercentage = 0
                )
            }
            db.subjectDao().insertChapters(chapterEntities)
        }

        // 3. Initial Clean Board Exam Configuration
        db.boardExamDao().insertOrUpdate(
            BoardExamConfigEntity(
                id = 1,
                examName = completedProfile.targetExamName,
                examDate = completedProfile.targetExamDate,
                totalSyllabusChapters = max(1, totalChaptersCount),
                completedChapters = 0,
                targetPercentage = completedProfile.targetPercentage,
                dailyTargetLectures = 2,
                dailyTargetRevisions = 2
            )
        )

        // 4. Initial Clean Discipline Matrix (0 Score initially)
        db.disciplineDao().insertOrUpdate(
            DailyDisciplineEntity(
                date = today,
                studyScore = 0,
                workoutScore = 0,
                noPornScore = 0,
                sleepScore = 0,
                readingScore = 0,
                totalScore = 0,
                xpEarned = 0
            )
        )

        // 5. Initial Clean Habits (Customized with user's wake time and study goal, 0 streak)
        val defaultHabits = listOf(
            HabitEntity(
                name = "Wake Early (${completedProfile.wakeUpTime})",
                iconName = "alarm",
                colorHex = "#FFB300",
                weight = 15,
                isNegativeHabit = false,
                targetUnit = "Time",
                targetNumeric = 1,
                streak = 0,
                bestStreak = 0,
                orderIndex = 0,
                habitType = HabitType.SLEEP
            ),
            HabitEntity(
                name = "Daily Workout (${completedProfile.workoutType})",
                iconName = "fitness_center",
                colorHex = "#00E676",
                weight = 20,
                isNegativeHabit = false,
                targetUnit = "Session",
                targetNumeric = 1,
                streak = 0,
                bestStreak = 0,
                orderIndex = 1,
                habitType = HabitType.WORKOUT
            ),
            HabitEntity(
                name = "Deep Study (${completedProfile.dailyStudyGoalHours.toInt()}h+ Goal)",
                iconName = "menu_book",
                colorHex = "#38E1FF",
                weight = 40,
                isNegativeHabit = false,
                targetUnit = "Hours",
                targetNumeric = completedProfile.dailyStudyGoalHours.toInt(),
                streak = 0,
                bestStreak = 0,
                orderIndex = 2,
                habitType = HabitType.DEEP_STUDY
            ),
            HabitEntity(
                name = "Book Reading",
                iconName = "auto_stories",
                colorHex = "#B388FF",
                weight = 10,
                isNegativeHabit = false,
                targetUnit = "Pages",
                targetNumeric = 10,
                streak = 0,
                bestStreak = 0,
                orderIndex = 3,
                habitType = HabitType.READING
            ),
            HabitEntity(
                name = "No Porn (Discipline)",
                iconName = "shield",
                colorHex = "#FF5722",
                weight = 15,
                isNegativeHabit = true,
                targetUnit = "Days Clean",
                targetNumeric = 1,
                streak = 0,
                bestStreak = 0,
                orderIndex = 4,
                habitType = HabitType.NO_PORN
            ),
            HabitEntity(
                name = "No Reels / Doomscroll",
                iconName = "do_not_disturb",
                colorHex = "#FF3D71",
                weight = 10,
                isNegativeHabit = true,
                targetUnit = "Days Clean",
                targetNumeric = 1,
                streak = 0,
                bestStreak = 0,
                orderIndex = 5,
                habitType = HabitType.NO_REELS
            ),
            HabitEntity(
                name = "Meditation",
                iconName = "self_improvement",
                colorHex = "#70B8FF",
                weight = 5,
                isNegativeHabit = false,
                targetUnit = "Minutes",
                targetNumeric = 15,
                streak = 0,
                bestStreak = 0,
                orderIndex = 6,
                habitType = HabitType.MEDITATION
            ),
            HabitEntity(
                name = "Water Intake (3.5L)",
                iconName = "water_drop",
                colorHex = "#00C2FF",
                weight = 5,
                isNegativeHabit = false,
                targetUnit = "Liters",
                targetNumeric = 3,
                streak = 0,
                bestStreak = 0,
                orderIndex = 7,
                habitType = HabitType.HYDRATION
            )
        )
        for (habit in defaultHabits) {
            db.habitDao().insertHabit(habit)
        }

        // 6. Generate Dynamic Tasks for Today
        generateSmartDailyPlan(today)

        // 7. Schedule Alarms
        if (context != null) {
            AlarmScheduler.scheduleProfileAlarms(context, completedProfile)
        }
    }

    suspend fun resetAllUserData() {
        db.userProfileDao().clearProfile()
        db.dailyPlanDao().clearAll()
        db.subjectDao().clearAll()
        db.habitDao().clearAll()
        db.workoutDao().clearAll()
        db.disciplineDao().clearAll()
        db.winterArcDao().clearAll()
        db.boardExamDao().clearAll()
        db.noteDao().clearAll()
        db.reflectionDao().clearAll()
        db.goalDao().clearAll()
        db.schoolStatusDao().clearAll()
    }

    // ----------------------------------------------------
    // SCHOOL STATUS SYSTEM
    // ----------------------------------------------------

    fun getTodaySchoolStatus(): Flow<SchoolStatusEntity?> {
        return db.schoolStatusDao().getStatusForDate(getTodayDateString())
    }

    fun getAllSchoolLogs(): Flow<List<SchoolStatusEntity>> = db.schoolStatusDao().getAllLogs()

    suspend fun dispatchSchool() {
        val today = getTodayDateString()
        val current = db.schoolStatusDao().getStatusForDateDirect(today)
            ?: SchoolStatusEntity(date = today)

        val updated = current.copy(
            currentState = SchoolState.TRAVELLING_TO_SCHOOL,
            dispatchSchoolTime = System.currentTimeMillis()
        )
        db.schoolStatusDao().insertOrUpdate(updated)
    }

    suspend fun arrivedSchool() {
        val today = getTodayDateString()
        val current = db.schoolStatusDao().getStatusForDateDirect(today)
            ?: SchoolStatusEntity(date = today)

        val now = System.currentTimeMillis()
        val travelDuration = if (current.dispatchSchoolTime != null) {
            max(1, ((now - current.dispatchSchoolTime) / (1000 * 60)).toInt())
        } else {
            25
        }

        val updated = current.copy(
            currentState = SchoolState.IN_SCHOOL,
            arrivedSchoolTime = now,
            travelToSchoolMinutes = travelDuration,
            isPresent = true
        )
        db.schoolStatusDao().insertOrUpdate(updated)
    }

    suspend fun dispatchHome() {
        val today = getTodayDateString()
        val current = db.schoolStatusDao().getStatusForDateDirect(today)
            ?: SchoolStatusEntity(date = today)

        val now = System.currentTimeMillis()
        val inSchoolDur = if (current.arrivedSchoolTime != null) {
            max(10, ((now - current.arrivedSchoolTime) / (1000 * 60)).toInt())
        } else {
            300
        }

        val updated = current.copy(
            currentState = SchoolState.TRAVELLING_HOME,
            dispatchHomeTime = now,
            inSchoolMinutes = inSchoolDur
        )
        db.schoolStatusDao().insertOrUpdate(updated)
    }

    suspend fun arrivedHome() {
        val today = getTodayDateString()
        val current = db.schoolStatusDao().getStatusForDateDirect(today)
            ?: SchoolStatusEntity(date = today)

        val now = System.currentTimeMillis()
        val travelHomeDur = if (current.dispatchHomeTime != null) {
            max(1, ((now - current.dispatchHomeTime) / (1000 * 60)).toInt())
        } else {
            30
        }

        val updated = current.copy(
            currentState = SchoolState.ARRIVED_HOME,
            arrivedHomeTime = now,
            travelHomeMinutes = travelHomeDur
        )
        db.schoolStatusDao().insertOrUpdate(updated)

        try {
            generateSmartDailyPlan(today)
        } catch (e: Exception) {
            Log.e("RebuildRepository", "Failed to generate smart daily plan on arrival home", e)
        }
    }

    // ----------------------------------------------------
    // SMART DAILY PLANNER
    // ----------------------------------------------------

    fun getTodayTasks(): Flow<List<DailyPlanTaskEntity>> {
        return db.dailyPlanDao().getTasksForDate(getTodayDateString())
    }

    suspend fun generateSmartDailyPlan(targetDate: String) {
        val missedTasks = db.dailyPlanDao().getIncompleteTasksBefore(targetDate)
        val existingToday = db.dailyPlanDao().getTasksForDateDirect(targetDate).toMutableList()

        for (missed in missedTasks) {
            val isDuplicate = existingToday.any { ex ->
                ex.subject.equals(missed.subject, ignoreCase = true) &&
                ex.title.equals(missed.title, ignoreCase = true)
            }
            if (!isDuplicate) {
                val newId = db.dailyPlanDao().insertTask(
                    missed.copy(
                        id = 0,
                        date = targetDate,
                        movedFromDate = missed.date,
                        isCompleted = false
                    )
                )
                existingToday.add(missed.copy(id = newId, date = targetDate, isCompleted = false))
            }
        }

        if (existingToday.isEmpty()) {
            val holiday = db.holidayDao().getHolidayForDate(targetDate, monthDayFormat.format(Date()))
            val isFestival = holiday != null
            val profile = db.userProfileDao().getUserProfileDirect()
            val subjects = db.subjectDao().getAllSubjectsDirect()

            val generatedList = mutableListOf<DailyPlanTaskEntity>()
            var orderIndex = 0

            // Add dynamic tasks based on user's actual enrolled subjects
            if (subjects.isNotEmpty()) {
                for (sub in subjects.take(3)) {
                    val pendingChapters = db.subjectDao().getChaptersForSubjectDirect(sub.id).filter { !it.isCompleted }
                    val targetChapter = pendingChapters.firstOrNull()?.title ?: "Chapter 1 Review"

                    generatedList.add(
                        DailyPlanTaskEntity(
                            date = targetDate,
                            subject = sub.name,
                            title = "${sub.name} Study Session",
                            type = TaskType.LECTURE,
                            details = "Target: $targetChapter • Theory & Numerical Problem Solving",
                            targetMinutes = if (isFestival) 30 else profile?.preferredSessionDurationMinutes ?: 50,
                            orderIndex = orderIndex++,
                            xpReward = 60
                        )
                    )
                    generatedList.add(
                        DailyPlanTaskEntity(
                            date = targetDate,
                            subject = sub.name,
                            title = "${sub.name} Notes & Revision",
                            type = TaskType.NOTES,
                            details = "Formula mapping and NCERT PYQ review for $targetChapter",
                            targetMinutes = if (isFestival) 20 else 30,
                            orderIndex = orderIndex++,
                            xpReward = 40
                        )
                    )
                }
            } else {
                generatedList.add(
                    DailyPlanTaskEntity(
                        date = targetDate,
                        subject = "Core Study",
                        title = "Deep Work Study Block 1",
                        type = TaskType.LECTURE,
                        details = "High-focus deep work study session",
                        targetMinutes = 50,
                        orderIndex = orderIndex++,
                        xpReward = 60
                    )
                )
            }

            // Add user's workout
            val workoutType = profile?.workoutType ?: "Calisthenics"
            val workoutDuration = profile?.workoutDurationMinutes ?: 30
            generatedList.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Workout",
                    title = "Daily Workout ($workoutType)",
                    type = TaskType.WORKOUT,
                    details = "$workoutDuration min of physical exercise & cardio",
                    targetMinutes = workoutDuration,
                    orderIndex = orderIndex++,
                    xpReward = 40
                )
            )

            // Add evening reflection task
            generatedList.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "General",
                    title = "Evening Reflection & Planning",
                    type = TaskType.CUSTOM,
                    details = "Audit today's score and prepare tomorrow's schedule",
                    targetMinutes = 15,
                    orderIndex = orderIndex++,
                    xpReward = 25
                )
            )

            db.dailyPlanDao().insertTasks(generatedList)
        }

        recalculateDisciplineScore(targetDate)
    }

    suspend fun toggleTaskCompleted(task: DailyPlanTaskEntity) {
        val newCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null
        )
        db.dailyPlanDao().updateTask(updated)

        if (context != null) {
            if (newCompleted) {
                AlarmScheduler.cancelTaskAlarm(context, task.id)
            } else if (task.reminderHour != null && task.reminderMinute != null) {
                AlarmScheduler.scheduleTaskAlarm(context, task.id, task.reminderHour, task.reminderMinute, task.title, task.subject)
            }
        }

        if (newCompleted) {
            addXp(task.xpReward)
        }

        recalculateDisciplineScore(task.date)
    }

    suspend fun addTask(task: DailyPlanTaskEntity): Long {
        val id = db.dailyPlanDao().insertTask(task)
        if (context != null && task.reminderHour != null && task.reminderMinute != null && !task.isCompleted) {
            AlarmScheduler.scheduleTaskAlarm(context, id, task.reminderHour, task.reminderMinute, task.title, task.subject)
        }
        recalculateDisciplineScore(task.date)
        return id
    }

    suspend fun updateTask(task: DailyPlanTaskEntity) {
        db.dailyPlanDao().updateTask(task)
        if (context != null) {
            if (task.isCompleted || task.reminderHour == null || task.reminderMinute == null) {
                AlarmScheduler.cancelTaskAlarm(context, task.id)
            } else {
                AlarmScheduler.scheduleTaskAlarm(context, task.id, task.reminderHour, task.reminderMinute, task.title, task.subject)
            }
        }
        recalculateDisciplineScore(task.date)
    }

    suspend fun deleteTask(task: DailyPlanTaskEntity) {
        db.dailyPlanDao().deleteTask(task)
        if (context != null) {
            AlarmScheduler.cancelTaskAlarm(context, task.id)
        }
        recalculateDisciplineScore(task.date)
    }

    // ----------------------------------------------------
    // SUBJECTS & CHAPTERS
    // ----------------------------------------------------

    fun getAllSubjects(): Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        db.subjectDao().getChaptersForSubject(subjectId)

    suspend fun addCustomSubject(subject: SubjectEntity): Long {
        val id = db.subjectDao().insertSubject(subject)
        updateBoardExamChapterCount()
        return id
    }

    suspend fun addCustomChapter(chapter: ChapterEntity) {
        db.subjectDao().insertChapters(listOf(chapter))
        val allChapters = db.subjectDao().getChaptersForSubjectDirect(chapter.subjectId)
        val subject = db.subjectDao().getSubjectByIdDirect(chapter.subjectId)
        if (subject != null) {
            db.subjectDao().updateSubject(
                subject.copy(
                    totalChapters = allChapters.size,
                    completedChapters = allChapters.count { it.isCompleted }
                )
            )
        }
        updateBoardExamChapterCount()
    }

    private suspend fun updateBoardExamChapterCount() {
        val subjects = db.subjectDao().getAllSubjectsDirect()
        var total = 0
        var completed = 0
        for (s in subjects) {
            val chapters = db.subjectDao().getChaptersForSubjectDirect(s.id)
            total += chapters.size
            completed += chapters.count { it.isCompleted }
        }
        val config = db.boardExamDao().getBoardExamConfigDirect()
        if (config != null) {
            db.boardExamDao().insertOrUpdate(
                config.copy(
                    totalSyllabusChapters = max(1, total),
                    completedChapters = completed
                )
            )
        }
    }

    suspend fun updateChapter(chapter: ChapterEntity) {
        val lectureProgress = if (chapter.totalLectures > 0) {
            (chapter.completedLectures.toFloat() / chapter.totalLectures * 40).toInt()
        } else 0
        val notesProgress = if (chapter.notesDone) 20 else 0
        val pyqProgress = if (chapter.pyqsDone) 20 else 0
        val revisionProgress = min(20, chapter.revisionCount * 10)
        val totalPerc = min(100, lectureProgress + notesProgress + pyqProgress + revisionProgress)
        val isNowCompleted = totalPerc >= 80

        val updated = chapter.copy(
            completionPercentage = totalPerc,
            isCompleted = isNowCompleted
        )
        db.subjectDao().updateChapter(updated)

        val allSubChapters = db.subjectDao().getChaptersForSubjectDirect(chapter.subjectId)
        val completedCount = allSubChapters.count { it.id == chapter.id && isNowCompleted || it.id != chapter.id && it.isCompleted }
        val subject = db.subjectDao().getSubjectByIdDirect(chapter.subjectId)
        if (subject != null) {
            db.subjectDao().updateSubject(
                subject.copy(
                    completedChapters = completedCount,
                    totalChapters = max(subject.totalChapters, allSubChapters.size)
                )
            )
        }
        updateBoardExamChapterCount()
    }

    suspend fun incrementChapterRevision(chapterId: Long) {
        val chapter = db.subjectDao().getChapterById(chapterId) ?: return
        val updated = chapter.copy(
            revisionCount = chapter.revisionCount + 1,
            lastRevisionDate = getTodayDateString()
        )
        updateChapter(updated)
        addXp(30)
    }

    // ----------------------------------------------------
    // POMODORO & STUDY TRACKER
    // ----------------------------------------------------

    fun getTodayStudySessions(): Flow<List<StudySessionEntity>> {
        return db.subjectDao().getStudySessionsForDate(getTodayDateString())
    }

    fun getAllStudySessions(): Flow<List<StudySessionEntity>> = db.subjectDao().getAllStudySessions()

    fun getTodayStudyMinutes(): Flow<Int> = db.subjectDao().getDailyStudyMinutes(getTodayDateString())

    suspend fun recordStudySession(
        subjectName: String,
        chapterName: String,
        durationMinutes: Int,
        sessionType: SessionType,
        xpReward: Int
    ) {
        val today = getTodayDateString()
        val session = StudySessionEntity(
            subjectName = subjectName,
            chapterName = chapterName,
            durationMinutes = durationMinutes,
            sessionType = sessionType,
            date = today,
            xpEarned = xpReward
        )
        db.subjectDao().insertStudySession(session)
        addXp(xpReward)
        recalculateDisciplineScore(today)
    }

    // ----------------------------------------------------
    // FITNESS MODULE
    // ----------------------------------------------------

    fun getTodayWorkouts(): Flow<List<WorkoutLogEntity>> {
        return db.workoutDao().getWorkoutsForDate(getTodayDateString())
    }

    fun getAllWorkouts(): Flow<List<WorkoutLogEntity>> = db.workoutDao().getAllWorkouts()

    suspend fun toggleWorkoutCompleted(workout: WorkoutLogEntity) {
        val updated = workout.copy(isCompleted = !workout.isCompleted)
        db.workoutDao().updateWorkout(updated)
        if (updated.isCompleted) {
            addXp(40)
        }
        recalculateDisciplineScore(workout.date)
    }

    suspend fun generateDailyWorkoutPlan(level: WorkoutLevel) {
        val today = getTodayDateString()
        val workouts = when (level) {
            WorkoutLevel.BEGINNER -> listOf(
                WorkoutLogEntity(date = today, exerciseName = "Brisk Walking", exerciseType = ExerciseType.WALKING, level = level, durationMinutes = 15, distanceKm = 1.5f, caloriesBurned = 90),
                WorkoutLogEntity(date = today, exerciseName = "Knee Pushups", exerciseType = ExerciseType.PUSHUPS, level = level, sets = 3, reps = 10, durationMinutes = 10),
                WorkoutLogEntity(date = today, exerciseName = "Bodyweight Squats", exerciseType = ExerciseType.SQUATS, level = level, sets = 3, reps = 12, durationMinutes = 10)
            )
            WorkoutLevel.INTERMEDIATE -> listOf(
                WorkoutLogEntity(date = today, exerciseName = "Outdoor Running", exerciseType = ExerciseType.RUNNING, level = level, durationMinutes = 20, distanceKm = 3.0f, caloriesBurned = 200),
                WorkoutLogEntity(date = today, exerciseName = "Pushups", exerciseType = ExerciseType.PUSHUPS, level = level, sets = 3, reps = 15, durationMinutes = 15),
                WorkoutLogEntity(date = today, exerciseName = "Squats", exerciseType = ExerciseType.SQUATS, level = level, sets = 3, reps = 20, durationMinutes = 15)
            )
            WorkoutLevel.ADVANCED -> listOf(
                WorkoutLogEntity(date = today, exerciseName = "Tempo Running 5K", exerciseType = ExerciseType.RUNNING, level = level, durationMinutes = 30, distanceKm = 5.0f, caloriesBurned = 380),
                WorkoutLogEntity(date = today, exerciseName = "Diamond & Decline Pushups", exerciseType = ExerciseType.PUSHUPS, level = level, sets = 4, reps = 25, durationMinutes = 20),
                WorkoutLogEntity(date = today, exerciseName = "Jump Squats & Lunges", exerciseType = ExerciseType.SQUATS, level = level, sets = 4, reps = 30, durationMinutes = 20)
            )
        }
        db.workoutDao().insertWorkouts(workouts)
    }

    suspend fun addWorkout(workout: WorkoutLogEntity) {
        db.workoutDao().insertWorkout(workout)
        recalculateDisciplineScore(workout.date)
    }

    // ----------------------------------------------------
    // HABIT MATRIX
    // ----------------------------------------------------

    fun getAllHabits(): Flow<List<HabitEntity>> = db.habitDao().getAllHabits()

    fun getTodayHabitLogs(): Flow<List<HabitLogEntity>> {
        return db.habitDao().getLogsForDate(getTodayDateString())
    }

    suspend fun toggleHabit(habit: HabitEntity) {
        val today = getTodayDateString()
        val currentLog = db.habitDao().getLogForHabitAndDate(habit.id, today)
        val isNowCompleted = !(currentLog?.isCompleted ?: false)

        val log = (currentLog ?: HabitLogEntity(habitId = habit.id, date = today))
            .copy(isCompleted = isNowCompleted)
        db.habitDao().insertOrUpdateLog(log)

        val newStreak = if (isNowCompleted) habit.streak + 1 else max(0, habit.streak - 1)
        val best = max(newStreak, habit.bestStreak)
        db.habitDao().updateHabit(habit.copy(streak = newStreak, bestStreak = best))

        if (isNowCompleted) {
            addXp(30)
        }
        recalculateDisciplineScore(today)
    }

    suspend fun addCustomHabit(habit: HabitEntity) {
        db.habitDao().insertHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        db.habitDao().deleteHabit(habit)
    }

    // ----------------------------------------------------
    // DISCIPLINE SCORE ENGINE
    // ----------------------------------------------------

    fun getTodayDiscipline(): Flow<DailyDisciplineEntity?> {
        return db.disciplineDao().getDisciplineForDate(getTodayDateString())
    }

    fun getDisciplineTrends(): Flow<List<DailyDisciplineEntity>> {
        return db.disciplineDao().getRecentDisciplineScores()
    }

    suspend fun recalculateDisciplineScore(date: String) {
        val studyTasks = db.dailyPlanDao().getTasksForDateDirect(date).filter { it.subject != "Workout" }
        val studyScore = if (studyTasks.isNotEmpty()) {
            val done = studyTasks.count { it.isCompleted }
            ((done.toFloat() / studyTasks.size) * 40).toInt()
        } else {
            0
        }

        val workouts = db.workoutDao().getWorkoutsForDateDirect(date)
        val workoutScore = if (workouts.isNotEmpty()) {
            val done = workouts.count { it.isCompleted }
            ((done.toFloat() / workouts.size) * 20).toInt()
        } else {
            val workoutTasks = db.dailyPlanDao().getTasksForDateDirect(date).filter { it.subject == "Workout" }
            if (workoutTasks.isNotEmpty()) {
                val done = workoutTasks.count { it.isCompleted }
                ((done.toFloat() / workoutTasks.size) * 20).toInt()
            } else {
                0
            }
        }

        val habitLogs = db.habitDao().getLogsForDateDirect(date)
        val allHabits = db.habitDao().getAllHabitsDirect()
        val habitMap = allHabits.associateBy { it.id }

        var noPornScore = 0
        var sleepScore = 0
        var readingScore = 0

        for (log in habitLogs) {
            val habit = habitMap[log.habitId] ?: continue
            when (habit.habitType) {
                HabitType.NO_PORN -> {
                    noPornScore = if (log.isCompleted) 15 else 0
                }
                HabitType.SLEEP -> {
                    sleepScore = if (log.isCompleted) 15 else 0
                }
                HabitType.READING -> {
                    readingScore = if (log.isCompleted) 10 else 0
                }
                else -> {}
            }
        }

        val total = min(100, studyScore + workoutScore + noPornScore + sleepScore + readingScore)

        val entity = DailyDisciplineEntity(
            date = date,
            studyScore = studyScore,
            workoutScore = workoutScore,
            noPornScore = noPornScore,
            sleepScore = sleepScore,
            readingScore = readingScore,
            totalScore = total,
            xpEarned = total * 5
        )
        db.disciplineDao().insertOrUpdate(entity)
    }

    // ----------------------------------------------------
    // WINTER ARC & XP SYSTEM
    // ----------------------------------------------------

    fun getWinterArcState(): Flow<WinterArcStateEntity?> = db.winterArcDao().getWinterArcState()

    suspend fun addXp(amount: Int) {
        val current = db.winterArcDao().getWinterArcStateDirect() ?: WinterArcStateEntity(id = 1)
        val newXp = current.xp + amount
        val newLevel = max(1, newXp / 400 + 1)
        val updated = current.copy(
            xp = newXp,
            level = newLevel,
            transformationScore = min(100, current.transformationScore + 1)
        )
        db.winterArcDao().insertOrUpdate(updated)
    }

    suspend fun updateWinterArcDay(day: Int, streak: Int) {
        val current = db.winterArcDao().getWinterArcStateDirect() ?: WinterArcStateEntity(id = 1)
        val updated = current.copy(
            currentDay = day,
            streak = streak,
            bestStreak = max(streak, current.bestStreak)
        )
        db.winterArcDao().insertOrUpdate(updated)
    }

    // ----------------------------------------------------
    // BOARD EXAM CALCULATOR
    // ----------------------------------------------------

    fun getBoardExamConfig(): Flow<BoardExamConfigEntity?> = db.boardExamDao().getBoardExamConfig()

    suspend fun updateBoardExamConfig(config: BoardExamConfigEntity) {
        db.boardExamDao().insertOrUpdate(config)
    }

    fun calculateDaysUntilBoardExam(examDateStr: String): Long {
        return try {
            val examDate = dateFormat.parse(examDateStr) ?: Date()
            val diffMs = examDate.time - System.currentTimeMillis()
            max(0, TimeUnit.MILLISECONDS.toDays(diffMs))
        } catch (e: Exception) {
            148L
        }
    }

    // ----------------------------------------------------
    // FESTIVALS & HOLIDAYS
    // ----------------------------------------------------

    fun getAllHolidays(): Flow<List<HolidayEntity>> = db.holidayDao().getAllHolidays()

    suspend fun addHoliday(holiday: HolidayEntity) {
        db.holidayDao().insertHoliday(holiday)
    }

    suspend fun deleteHoliday(holiday: HolidayEntity) {
        db.holidayDao().deleteHoliday(holiday)
    }

    // ----------------------------------------------------
    // NOTES & FORMULAS
    // ----------------------------------------------------

    fun getAllNotes(): Flow<List<com.example.data.local.entity.NoteEntity>> = db.noteDao().getAllNotes()

    fun getNotesBySubject(tag: String): Flow<List<com.example.data.local.entity.NoteEntity>> =
        if (tag == "All") db.noteDao().getAllNotes() else db.noteDao().getNotesBySubject(tag)

    suspend fun saveNote(note: com.example.data.local.entity.NoteEntity) {
        if (note.id == 0L) {
            db.noteDao().insertNote(note)
        } else {
            db.noteDao().updateNote(note)
        }
    }

    suspend fun deleteNote(note: com.example.data.local.entity.NoteEntity) {
        db.noteDao().deleteNote(note)
    }

    // ----------------------------------------------------
    // DAILY REFLECTIONS
    // ----------------------------------------------------

    fun getTodayReflection(): Flow<com.example.data.local.entity.DailyReflectionEntity?> =
        db.reflectionDao().getReflectionForDate(getTodayDateString())

    fun getRecentReflections(): Flow<List<com.example.data.local.entity.DailyReflectionEntity>> =
        db.reflectionDao().getRecentReflections()

    suspend fun saveReflection(reflection: com.example.data.local.entity.DailyReflectionEntity) {
        db.reflectionDao().insertOrUpdate(reflection)
        addXp(25)
    }

    // ----------------------------------------------------
    // GOALS SYSTEM
    // ----------------------------------------------------

    fun getAllGoals(): Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    fun getActiveGoals(): Flow<List<GoalEntity>> = db.goalDao().getActiveGoals()
    fun getCompletedGoals(): Flow<List<GoalEntity>> = db.goalDao().getCompletedGoals()

    suspend fun createGoal(goal: GoalEntity): Long {
        val id = db.goalDao().insertGoal(goal)
        if (context != null && goal.reminderEnabled && goal.reminderHour != null && goal.reminderMinute != null && !goal.isCompleted) {
            AlarmScheduler.scheduleGoalAlarm(context, id, goal.reminderHour, goal.reminderMinute, goal.title)
        }
        return id
    }

    suspend fun updateGoal(goal: GoalEntity) {
        db.goalDao().updateGoal(goal)
        if (context != null) {
            if (goal.isCompleted || !goal.reminderEnabled || goal.reminderHour == null || goal.reminderMinute == null) {
                AlarmScheduler.cancelGoalAlarm(context, goal.id)
            } else {
                AlarmScheduler.scheduleGoalAlarm(context, goal.id, goal.reminderHour, goal.reminderMinute, goal.title)
            }
        }
    }

    suspend fun toggleGoalCompleted(goal: GoalEntity) {
        val newCompleted = !goal.isCompleted
        val updated = goal.copy(
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null,
            progressPercentage = if (newCompleted) 100 else goal.progressPercentage
        )
        updateGoal(updated)
        if (newCompleted) {
            addXp(150)
        }
    }

    suspend fun updateGoalProgress(goal: GoalEntity, progress: Int) {
        val clampedProgress = progress.coerceIn(0, 100)
        val isNowCompleted = clampedProgress >= 100
        val updated = goal.copy(
            progressPercentage = clampedProgress,
            isCompleted = isNowCompleted,
            completedAt = if (isNowCompleted && !goal.isCompleted) System.currentTimeMillis() else if (!isNowCompleted) null else goal.completedAt
        )
        updateGoal(updated)
        if (isNowCompleted && !goal.isCompleted) {
            addXp(150)
        }
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        db.goalDao().deleteGoal(goal)
        if (context != null) {
            AlarmScheduler.cancelGoalAlarm(context, goal.id)
        }
    }

    // ----------------------------------------------------
    // ADVANCED SYLLABUS TRACKER ENGINE
    // ----------------------------------------------------

    suspend fun initializeMasterSyllabusIfEmpty() {
        val existingUnits = db.syllabusDao().getAllUnits().firstOrNull() ?: emptyList()
        if (existingUnits.isNotEmpty()) return

        val masterSubjects = MasterSyllabusProvider.getAllMasterSubjects()
        var order = 0

        for (subject in masterSubjects) {
            for (unit in subject.units) {
                var unitTopicsTotal = 0
                val unitEntity = SyllabusUnitEntity(
                    subjectCode = subject.code,
                    subjectName = subject.name,
                    unitNumber = unit.unitNumber,
                    unitTitle = unit.unitTitle,
                    description = unit.description,
                    totalTopicsCount = unit.chapters.sumOf { it.topics.size },
                    completedTopicsCount = 0,
                    completionPercentage = 0,
                    orderIndex = order++
                )
                val unitId = db.syllabusDao().insertUnit(unitEntity)

                for (ch in unit.chapters) {
                    val chapterEntity = SyllabusChapterEntity(
                        unitId = unitId,
                        subjectCode = subject.code,
                        chapterNumber = ch.chapterNumber,
                        title = ch.title,
                        description = ch.description,
                        status = SyllabusStatus.NOT_STARTED,
                        totalTopicsCount = ch.topics.size,
                        completedTopicsCount = 0,
                        completionPercentage = 0
                    )
                    val chapterId = db.syllabusDao().insertChapter(chapterEntity)

                    val topicEntities = ch.topics.mapIndexed { idx, topicTitle ->
                        SyllabusTopicEntity(
                            chapterId = chapterId,
                            unitId = unitId,
                            subjectCode = subject.code,
                            topicNumber = idx + 1,
                            title = topicTitle,
                            status = SyllabusStatus.NOT_STARTED
                        )
                    }
                    db.syllabusDao().insertTopics(topicEntities)
                }
            }
        }
    }

    fun getSyllabusUnits(subjectCode: String): Flow<List<SyllabusUnitEntity>> =
        db.syllabusDao().getUnitsForSubject(subjectCode)

    fun getSyllabusChapters(unitId: Long): Flow<List<SyllabusChapterEntity>> =
        db.syllabusDao().getChaptersForUnit(unitId)

    fun getSyllabusTopics(chapterId: Long): Flow<List<SyllabusTopicEntity>> =
        db.syllabusDao().getTopicsForChapter(chapterId)

    fun getAllSyllabusChapters(): Flow<List<SyllabusChapterEntity>> =
        db.syllabusDao().getAllChapters()

    fun getAllSyllabusTopics(): Flow<List<SyllabusTopicEntity>> =
        db.syllabusDao().getAllTopics()

    suspend fun updateTopicStatus(topicId: Long, newStatus: SyllabusStatus) {
        val topic = db.syllabusDao().getTopicById(topicId) ?: return
        val updated = topic.copy(
            status = newStatus,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.syllabusDao().updateTopic(updated)
        recalculateSyllabusProgressForChapter(topic.chapterId)
        if (newStatus == SyllabusStatus.COMPLETED || newStatus == SyllabusStatus.MASTERED) {
            addXp(30)
        }
    }

    suspend fun updateChapterStatus(chapterId: Long, newStatus: SyllabusStatus) {
        val chapter = db.syllabusDao().getChapterById(chapterId) ?: return
        val topics = db.syllabusDao().getTopicsForChapterDirect(chapterId)
        val updatedTopics = topics.map { it.copy(status = newStatus) }
        db.syllabusDao().insertTopics(updatedTopics)

        val updatedChapter = chapter.copy(
            status = newStatus,
            completedTopicsCount = if (newStatus == SyllabusStatus.NOT_STARTED) 0 else chapter.totalTopicsCount,
            completionPercentage = if (newStatus == SyllabusStatus.NOT_STARTED) 0 else 100,
            notesDone = if (newStatus == SyllabusStatus.MASTERED) true else chapter.notesDone,
            pyqsDone = if (newStatus == SyllabusStatus.MASTERED) true else chapter.pyqsDone,
            revisionCount = if (newStatus == SyllabusStatus.REVISED_ONCE) 1 else if (newStatus == SyllabusStatus.REVISED_TWICE || newStatus == SyllabusStatus.MASTERED) 2 else chapter.revisionCount,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.syllabusDao().updateChapter(updatedChapter)
        recalculateSyllabusProgressForUnit(chapter.unitId)
        addXp(100)
    }

    private suspend fun recalculateSyllabusProgressForChapter(chapterId: Long) {
        val chapter = db.syllabusDao().getChapterById(chapterId) ?: return
        val topics = db.syllabusDao().getTopicsForChapterDirect(chapterId)
        val total = topics.size
        if (total == 0) return

        val completedCount = topics.count { it.status == SyllabusStatus.COMPLETED || it.status == SyllabusStatus.REVISED_ONCE || it.status == SyllabusStatus.REVISED_TWICE || it.status == SyllabusStatus.MASTERED }
        val masteredCount = topics.count { it.status == SyllabusStatus.MASTERED }
        val inProgressCount = topics.count { it.status != SyllabusStatus.NOT_STARTED }
        val pct = (completedCount * 100) / total

        val derivedStatus = when {
            masteredCount == total -> SyllabusStatus.MASTERED
            completedCount == total -> SyllabusStatus.COMPLETED
            inProgressCount > 0 -> SyllabusStatus.IN_PROGRESS
            else -> SyllabusStatus.NOT_STARTED
        }

        db.syllabusDao().updateChapter(
            chapter.copy(
                totalTopicsCount = total,
                completedTopicsCount = completedCount,
                completionPercentage = pct,
                status = derivedStatus
            )
        )
        recalculateSyllabusProgressForUnit(chapter.unitId)
    }

    private suspend fun recalculateSyllabusProgressForUnit(unitId: Long) {
        val unit = db.syllabusDao().getUnitById(unitId) ?: return
        val chapters = db.syllabusDao().getChaptersForUnitDirect(unitId)
        val totalTopics = chapters.sumOf { it.totalTopicsCount }
        val completedTopics = chapters.sumOf { it.completedTopicsCount }
        val pct = if (totalTopics > 0) (completedTopics * 100) / totalTopics else 0

        db.syllabusDao().updateUnit(
            unit.copy(
                totalTopicsCount = totalTopics,
                completedTopicsCount = completedTopics,
                completionPercentage = pct
            )
        )
    }

    // ----------------------------------------------------
    // REAL ALARMS & DISMISSAL ENGINE
    // ----------------------------------------------------

    fun getAllAlarms(): Flow<List<AlarmEntity>> = db.alarmDao().getAllAlarms()

    fun getAlarmByIdFlow(id: Long): Flow<AlarmEntity?> = db.alarmDao().getAlarmByIdFlow(id)

    suspend fun getAlarmById(id: Long): AlarmEntity? = db.alarmDao().getAlarmById(id)

    suspend fun saveAlarm(alarm: AlarmEntity): Long {
        val id = if (alarm.id == 0L) {
            db.alarmDao().insertAlarm(alarm)
        } else {
            db.alarmDao().updateAlarm(alarm)
            alarm.id
        }
        if (context != null && alarm.isEnabled) {
            AlarmScheduler.scheduleCustomAlarm(context, alarm.copy(id = id))
        } else if (context != null) {
            AlarmScheduler.cancelCustomAlarm(context, id)
        }
        return id
    }

    suspend fun toggleAlarm(alarm: AlarmEntity) {
        val newEnabled = !alarm.isEnabled
        val updated = alarm.copy(isEnabled = newEnabled)
        db.alarmDao().updateAlarm(updated)
        if (context != null) {
            if (newEnabled) {
                AlarmScheduler.scheduleCustomAlarm(context, updated)
            } else {
                AlarmScheduler.cancelCustomAlarm(context, alarm.id)
            }
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        db.alarmDao().deleteAlarm(alarm)
        if (context != null) {
            AlarmScheduler.cancelCustomAlarm(context, alarm.id)
        }
    }

    suspend fun logAlarmDismissal(log: AlarmLogEntity) {
        db.alarmDao().insertAlarmLog(log)
        if (log.solvedSuccessfully) {
            addXp(50)
        }
    }

    fun getRecentAlarmLogs(): Flow<List<AlarmLogEntity>> = db.alarmDao().getRecentAlarmLogs()

    // ----------------------------------------------------
    // WINTER ARC OBJECTIVES & MISSION CONTROL
    // ----------------------------------------------------

    fun getWinterArcObjectives(): Flow<List<WinterArcObjectiveEntity>> =
        db.winterArcObjectivesDao().getAllObjectives()

    suspend fun saveWinterArcObjective(objective: WinterArcObjectiveEntity): Long {
        return if (objective.id == 0L) {
            db.winterArcObjectivesDao().insertObjective(objective)
        } else {
            db.winterArcObjectivesDao().updateObjective(objective)
            objective.id
        }
    }

    suspend fun toggleWinterArcObjective(objective: WinterArcObjectiveEntity) {
        val newCompleted = !objective.isCompleted
        val updated = objective.copy(
            isCompleted = newCompleted,
            progressPercentage = if (newCompleted) 100 else 0,
            currentValue = if (newCompleted) objective.targetValue else "0%"
        )
        db.winterArcObjectivesDao().updateObjective(updated)
        if (newCompleted) {
            addXp(200)
        }
    }

    suspend fun deleteWinterArcObjective(objective: WinterArcObjectiveEntity) {
        db.winterArcObjectivesDao().deleteObjective(objective)
    }

    fun getArcGoalsPlan(horizon: String): Flow<List<ArcGoalPlanItemEntity>> =
        db.winterArcObjectivesDao().getArcGoalsByHorizon(horizon)

    fun getAllArcGoalsPlan(): Flow<List<ArcGoalPlanItemEntity>> =
        db.winterArcObjectivesDao().getAllArcGoals()

    suspend fun saveArcGoal(goal: ArcGoalPlanItemEntity): Long {
        return if (goal.id == 0L) {
            db.winterArcObjectivesDao().insertArcGoal(goal)
        } else {
            db.winterArcObjectivesDao().updateArcGoal(goal)
            goal.id
        }
    }

    suspend fun toggleArcGoal(goal: ArcGoalPlanItemEntity) {
        val newCompleted = !goal.isCompleted
        val updated = goal.copy(isCompleted = newCompleted)
        db.winterArcObjectivesDao().updateArcGoal(updated)
        if (newCompleted) {
            addXp(goal.xpReward)
        }
    }

    suspend fun deleteArcGoal(goal: ArcGoalPlanItemEntity) {
        db.winterArcObjectivesDao().deleteArcGoal(goal)
    }

    suspend fun initializeWinterArcObjectivesIfEmpty(profile: UserProfileEntity) {
        val existing = db.winterArcObjectivesDao().getAllObjectivesDirect()
        if (existing.isNotEmpty()) return

        val defaultObjectives = listOf(
            WinterArcObjectiveEntity(
                title = "Board Exam Score Mastery",
                description = "Achieve ${profile.targetPercentage}% in ${profile.targetExamName}",
                category = com.example.data.local.entity.ObjectiveCategory.ACADEMIC,
                targetValue = "${profile.targetPercentage}%",
                currentValue = "0%",
                progressPercentage = 0,
                orderIndex = 0
            ),
            WinterArcObjectiveEntity(
                title = "Daily Deep Study Consistency",
                description = "Clock minimum ${profile.dailyStudyGoalHours.toInt()}h deep focus daily",
                category = com.example.data.local.entity.ObjectiveCategory.ACADEMIC,
                targetValue = "${profile.dailyStudyGoalHours.toInt()} Hours",
                currentValue = "0 Hours",
                progressPercentage = 0,
                orderIndex = 1
            ),
            WinterArcObjectiveEntity(
                title = "Physical Transformation Protocol",
                description = "Complete ${profile.workoutType} sessions consistently",
                category = com.example.data.local.entity.ObjectiveCategory.FITNESS,
                targetValue = "90 Sessions",
                currentValue = "0 Sessions",
                progressPercentage = 0,
                orderIndex = 2
            ),
            WinterArcObjectiveEntity(
                title = "Zero-Relapse Monk Discipline",
                description = "Complete 90 days clean from digital dopamine & adult content",
                category = com.example.data.local.entity.ObjectiveCategory.DISCIPLINE,
                targetValue = "90 Days",
                currentValue = "0 Days",
                progressPercentage = 0,
                orderIndex = 3
            ),
            WinterArcObjectiveEntity(
                title = "Precision Sleep & Wake Rhythm",
                description = "Strict wake up at ${profile.wakeUpTime} & sleep at ${profile.sleepTime}",
                category = com.example.data.local.entity.ObjectiveCategory.RESTORATION,
                targetValue = "90 Days",
                currentValue = "0 Days",
                progressPercentage = 0,
                orderIndex = 4
            )
        )
        db.winterArcObjectivesDao().insertObjectives(defaultObjectives)

        // Seed initial dynamic plan
        val defaultGoals = listOf(
            ArcGoalPlanItemEntity(timeHorizon = "DAILY", title = "Complete 2 Focus Sessions & Core Habit Matrix", description = "Maintain 100% discipline score today", xpReward = 100, priority = "CRITICAL", orderIndex = 0),
            ArcGoalPlanItemEntity(timeHorizon = "DAILY", title = "Hit ${profile.workoutType} Training Block", description = "30 mins intense physical stimulation", xpReward = 75, priority = "HIGH", orderIndex = 1),
            ArcGoalPlanItemEntity(timeHorizon = "WEEKLY", title = "Master 2 Pending Physics & Chemistry Chapters", description = "Complete lectures, notes and 25 PYQs", xpReward = 300, priority = "CRITICAL", orderIndex = 0),
            ArcGoalPlanItemEntity(timeHorizon = "WEEKLY", title = "Zero Missed Wake-Up Alarms", description = "Solve morning challenge without snooze", xpReward = 250, priority = "HIGH", orderIndex = 1),
            ArcGoalPlanItemEntity(timeHorizon = "MONTHLY", title = "Complete 35% Full Board Syllabus", description = "Move 6 major chapters into 'Mastered' status", xpReward = 1000, priority = "CRITICAL", orderIndex = 0),
            ArcGoalPlanItemEntity(timeHorizon = "MONTHLY", title = "Reach Winter Arc Level 10 (4,000 XP)", description = "Sustain consistent daily progress", xpReward = 800, priority = "HIGH", orderIndex = 1)
        )
        db.winterArcObjectivesDao().insertArcGoals(defaultGoals)
    }

    // ----------------------------------------------------
    // AI CHAT PERSISTENCE
    // ----------------------------------------------------

    fun getChatMessages(): Flow<List<ChatMessageEntity>> = db.chatDao().getAllMessages()

    suspend fun saveChatMessage(message: ChatMessageEntity): Long = db.chatDao().insertMessage(message)

    suspend fun clearChatHistory() = db.chatDao().clearChatHistory()
}

