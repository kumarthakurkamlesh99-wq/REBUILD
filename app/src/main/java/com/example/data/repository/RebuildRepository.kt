package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.ExerciseType
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HolidayEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.SessionType
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.local.entity.WorkoutLevel
import com.example.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class RebuildRepository(private val db: AppDatabase) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

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

        // Automatically trigger smart daily planner generation
        generateSmartDailyPlan(today)
    }

    // ----------------------------------------------------
    // SMART DAILY PLANNER
    // ----------------------------------------------------

    fun getTodayTasks(): Flow<List<DailyPlanTaskEntity>> {
        return db.dailyPlanDao().getTasksForDate(getTodayDateString())
    }

    suspend fun generateSmartDailyPlan(targetDate: String) {
        // 1. Check if there are missed tasks from previous days and rollover
        val missedTasks = db.dailyPlanDao().getIncompleteTasksBefore(targetDate)
        for (missed in missedTasks) {
            db.dailyPlanDao().insertTask(
                missed.copy(
                    id = 0,
                    date = targetDate,
                    movedFromDate = missed.date,
                    isCompleted = false
                )
            )
        }

        // 2. Check if today already has newly generated tasks
        val existingToday = db.dailyPlanDao().getTasksForDateDirect(targetDate)
        if (existingToday.isEmpty()) {
            // Check if today is a festival
            val holiday = db.holidayDao().getHolidayForDate(targetDate, monthDayFormat.format(Date()))
            val isFestival = holiday != null

            val generatedTasks = mutableListOf<DailyPlanTaskEntity>()
            var order = 0

            // Physics
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Physics",
                    title = "Nuclei Lecture",
                    type = TaskType.LECTURE,
                    details = "Binding energy, mass defect & nuclear stability concepts",
                    targetMinutes = if (isFestival) 30 else 45,
                    orderIndex = order++,
                    xpReward = 60
                )
            )
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Physics",
                    title = "Nuclei Notes",
                    type = TaskType.NOTES,
                    details = "Complete handwritten summary & short formulas",
                    targetMinutes = if (isFestival) 20 else 30,
                    orderIndex = order++,
                    xpReward = 40
                )
            )

            // Chemistry
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Chemistry",
                    title = "P Block Revision",
                    type = TaskType.REVISION,
                    details = "Group 15-18 chemical reactions and trends",
                    targetMinutes = if (isFestival) 30 else 50,
                    orderIndex = order++,
                    xpReward = 50
                )
            )

            // Biology
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Biology",
                    title = "Genetics Lecture",
                    type = TaskType.LECTURE,
                    details = "Dihybrid crosses & chromosomal theory of inheritance",
                    targetMinutes = if (isFestival) 40 else 60,
                    orderIndex = order++,
                    xpReward = 60
                )
            )

            // Workout
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Workout",
                    title = "Running 20 min",
                    type = TaskType.WORKOUT,
                    details = "Cadence running or outdoor jog warmup",
                    targetMinutes = 20,
                    orderIndex = order++,
                    xpReward = 30
                )
            )
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Workout",
                    title = "Pushups 3 x 15",
                    type = TaskType.WORKOUT,
                    details = "3 sets of 15 strict form pushups",
                    targetMinutes = 15,
                    orderIndex = order++,
                    xpReward = 25
                )
            )
            generatedTasks.add(
                DailyPlanTaskEntity(
                    date = targetDate,
                    subject = "Workout",
                    title = "Squats 3 x 20",
                    type = TaskType.WORKOUT,
                    details = "3 sets of 20 bodyweight deep squats",
                    targetMinutes = 15,
                    orderIndex = order++,
                    xpReward = 25
                )
            )

            db.dailyPlanDao().insertTasks(generatedTasks)
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

        // Award XP to winter arc
        if (newCompleted) {
            addXp(task.xpReward)
        }

        recalculateDisciplineScore(task.date)
    }

    suspend fun addTask(task: DailyPlanTaskEntity) {
        db.dailyPlanDao().insertTask(task)
        recalculateDisciplineScore(task.date)
    }

    suspend fun deleteTask(task: DailyPlanTaskEntity) {
        db.dailyPlanDao().deleteTask(task)
        recalculateDisciplineScore(task.date)
    }

    // ----------------------------------------------------
    // SUBJECTS & CHAPTERS
    // ----------------------------------------------------

    fun getAllSubjects(): Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        db.subjectDao().getChaptersForSubject(subjectId)

    suspend fun updateChapter(chapter: ChapterEntity) {
        // Calculate completion percentage
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

        // Update parent subject count
        val allSubChapters = db.subjectDao().getChaptersForSubject(chapter.subjectId).firstOrNull() ?: emptyList()
        val completedCount = allSubChapters.count { it.id == chapter.id && isNowCompleted || it.id != chapter.id && it.isCompleted }
        val subject = db.subjectDao().getSubjectById(chapter.subjectId).firstOrNull()
        if (subject != null) {
            db.subjectDao().updateSubject(
                subject.copy(
                    completedChapters = completedCount,
                    totalChapters = max(subject.totalChapters, allSubChapters.size)
                )
            )
        }
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

        // Update habit streak
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
    // Default Weights:
    // Study = 40, Workout = 20, No Porn = 15, Sleep = 15, Reading = 10
    // ----------------------------------------------------

    fun getTodayDiscipline(): Flow<DailyDisciplineEntity?> {
        return db.disciplineDao().getDisciplineForDate(getTodayDateString())
    }

    fun getDisciplineTrends(): Flow<List<DailyDisciplineEntity>> {
        return db.disciplineDao().getRecentDisciplineScores()
    }

    suspend fun recalculateDisciplineScore(date: String) {
        // 1. Study Score (Max 40)
        val studyTasks = db.dailyPlanDao().getTasksForDateDirect(date).filter { it.subject != "Workout" }
        val studyScore = if (studyTasks.isNotEmpty()) {
            val done = studyTasks.count { it.isCompleted }
            ((done.toFloat() / studyTasks.size) * 40).toInt()
        } else {
            // Check study sessions
            35
        }

        // 2. Workout Score (Max 20)
        val workouts = db.workoutDao().getWorkoutsForDateDirect(date)
        val workoutScore = if (workouts.isNotEmpty()) {
            val done = workouts.count { it.isCompleted }
            ((done.toFloat() / workouts.size) * 20).toInt()
        } else {
            val workoutTasks = db.dailyPlanDao().getTasksForDateDirect(date).filter { it.subject == "Workout" }
            if (workoutTasks.isNotEmpty()) {
                val done = workoutTasks.count { it.isCompleted }
                ((done.toFloat() / workoutTasks.size) * 20).toInt()
            } else 20
        }

        // 3. Habit logs for No Porn, Sleep, Reading
        val habitLogs = db.habitDao().getLogsForDateDirect(date)
        val allHabits = db.habitDao().getAllHabits().firstOrNull() ?: emptyList()
        val habitMap = allHabits.associateBy { it.id }

        var noPornScore = 15
        var sleepScore = 15
        var readingScore = 0

        for (log in habitLogs) {
            val habit = habitMap[log.habitId] ?: continue
            val nameLower = habit.name.lowercase()
            if (nameLower.contains("porn")) {
                noPornScore = if (log.isCompleted) 15 else 0
            } else if (nameLower.contains("wake") || nameLower.contains("sleep")) {
                sleepScore = if (log.isCompleted) 15 else 0
            } else if (nameLower.contains("reading") || nameLower.contains("book")) {
                readingScore = if (log.isCompleted) 10 else 0
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
        val current = db.winterArcDao().getWinterArcStateDirect() ?: WinterArcStateEntity()
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
        val current = db.winterArcDao().getWinterArcStateDirect() ?: WinterArcStateEntity()
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
}
