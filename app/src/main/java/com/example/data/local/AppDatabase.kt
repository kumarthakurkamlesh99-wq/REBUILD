package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AiPlanDao
import com.example.data.local.dao.BoardExamDao
import com.example.data.local.dao.DailyPlanDao
import com.example.data.local.dao.DisciplineDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HolidayDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.ReflectionDao
import com.example.data.local.dao.SchoolStatusDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.WinterArcDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.entity.AiPlanCacheEntity
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.DailyReflectionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HabitType
import com.example.data.local.entity.HolidayEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        SchoolStatusEntity::class,
        DailyPlanTaskEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        StudySessionEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        WorkoutLogEntity::class,
        HolidayEntity::class,
        DailyDisciplineEntity::class,
        WinterArcStateEntity::class,
        BoardExamConfigEntity::class,
        NoteEntity::class,
        DailyReflectionEntity::class,
        AiPlanCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolStatusDao(): SchoolStatusDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun subjectDao(): SubjectDao
    abstract fun habitDao(): HabitDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun holidayDao(): HolidayDao
    abstract fun disciplineDao(): DisciplineDao
    abstract fun winterArcDao(): WinterArcDao
    abstract fun boardExamDao(): BoardExamDao
    abstract fun noteDao(): NoteDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun aiPlanDao(): AiPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add habitType column with default value 'CUSTOM'
                db.execSQL("ALTER TABLE habits ADD COLUMN habitType TEXT NOT NULL DEFAULT 'CUSTOM'")
                
                // Backfill existing rows based on current names one time
                db.execSQL("UPDATE habits SET habitType = 'SLEEP' WHERE LOWER(name) LIKE '%wake%' OR LOWER(name) LIKE '%sleep%'")
                db.execSQL("UPDATE habits SET habitType = 'WORKOUT' WHERE LOWER(name) LIKE '%workout%' OR LOWER(name) LIKE '%exercise%' OR LOWER(name) LIKE '%gym%'")
                db.execSQL("UPDATE habits SET habitType = 'DEEP_STUDY' WHERE LOWER(name) LIKE '%study%'")
                db.execSQL("UPDATE habits SET habitType = 'READING' WHERE LOWER(name) LIKE '%reading%' OR LOWER(name) LIKE '%book%'")
                db.execSQL("UPDATE habits SET habitType = 'NO_PORN' WHERE LOWER(name) LIKE '%porn%' OR LOWER(name) LIKE '%nofap%'")
                db.execSQL("UPDATE habits SET habitType = 'NO_REELS' WHERE LOWER(name) LIKE '%reels%' OR LOWER(name) LIKE '%doomscroll%'")
                db.execSQL("UPDATE habits SET habitType = 'MEDITATION' WHERE LOWER(name) LIKE '%meditation%' OR LOWER(name) LIKE '%mindfulness%'")
                db.execSQL("UPDATE habits SET habitType = 'HYDRATION' WHERE LOWER(name) LIKE '%water%' OR LOWER(name) LIKE '%hydration%'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rebuild_os_database"
                )
                .addMigrations(MIGRATION_2_3)
                .addCallback(DatabasePrepopulationCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulationCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Initial Winter Arc State
            db.winterArcDao().insertOrUpdate(
                WinterArcStateEntity(
                    id = 1,
                    startDate = "2026-08-01",
                    targetDays = 90,
                    currentDay = 27,
                    xp = 5420,
                    level = 14,
                    streak = 11,
                    bestStreak = 21,
                    transformationScore = 86,
                    targetDailyDeepWorkHours = 6.0f
                )
            )

            // 2. Initial Board Exam Configuration
            db.boardExamDao().insertOrUpdate(
                BoardExamConfigEntity(
                    id = 1,
                    examName = "Class 12 Board Exam",
                    examDate = "2027-01-24", // ~148 days from reference
                    totalSyllabusChapters = 70,
                    completedChapters = 38,
                    targetPercentage = 98,
                    dailyTargetLectures = 2,
                    dailyTargetRevisions = 2
                )
            )

            // 3. Initial Discipline Score
            db.disciplineDao().insertOrUpdate(
                DailyDisciplineEntity(
                    date = today,
                    studyScore = 35,
                    workoutScore = 20,
                    noPornScore = 15,
                    sleepScore = 12,
                    readingScore = 0,
                    totalScore = 82,
                    xpEarned = 320
                )
            )

            // 4. Populate Subjects & Chapters
            val physicsId = db.subjectDao().insertSubject(
                SubjectEntity(
                    name = "Physics",
                    code = "PHY",
                    iconName = "bolt",
                    colorHex = "#38E1FF",
                    totalChapters = 14,
                    completedChapters = 8,
                    targetHours = 120,
                    orderIndex = 0
                )
            )
            val chemistryId = db.subjectDao().insertSubject(
                SubjectEntity(
                    name = "Chemistry",
                    code = "CHEM",
                    iconName = "science",
                    colorHex = "#70B8FF",
                    totalChapters = 16,
                    completedChapters = 9,
                    targetHours = 110,
                    orderIndex = 1
                )
            )
            val biologyId = db.subjectDao().insertSubject(
                SubjectEntity(
                    name = "Biology",
                    code = "BIO",
                    iconName = "eco",
                    colorHex = "#00E676",
                    totalChapters = 16,
                    completedChapters = 11,
                    targetHours = 130,
                    orderIndex = 2
                )
            )
            val englishId = db.subjectDao().insertSubject(
                SubjectEntity(
                    name = "English",
                    code = "ENG",
                    iconName = "menu_book",
                    colorHex = "#FFB300",
                    totalChapters = 12,
                    completedChapters = 6,
                    targetHours = 50,
                    orderIndex = 3
                )
            )
            val hindiId = db.subjectDao().insertSubject(
                SubjectEntity(
                    name = "Hindi",
                    code = "HIN",
                    iconName = "translate",
                    colorHex = "#B388FF",
                    totalChapters = 12,
                    completedChapters = 4,
                    targetHours = 40,
                    orderIndex = 4
                )
            )

            // Populate Chapters
            db.subjectDao().insertChapters(
                listOf(
                    ChapterEntity(subjectId = physicsId, chapterNumber = 1, title = "Electric Charges & Fields", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 4, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 2, title = "Electrostatic Potential & Capacitance", totalLectures = 5, completedLectures = 5, notesDone = true, pyqsDone = true, revisionCount = 3, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 3, title = "Current Electricity", totalLectures = 7, completedLectures = 7, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 4, title = "Moving Charges & Magnetism", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 5, title = "Magnetism and Matter", totalLectures = 3, completedLectures = 3, notesDone = true, pyqsDone = true, revisionCount = 1, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 6, title = "Electromagnetic Induction", totalLectures = 5, completedLectures = 5, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 7, title = "Alternating Current", totalLectures = 5, completedLectures = 5, notesDone = true, pyqsDone = true, revisionCount = 1, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 8, title = "Electromagnetic Waves", totalLectures = 2, completedLectures = 2, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 9, title = "Ray Optics & Optical Instruments", totalLectures = 8, completedLectures = 4, notesDone = true, pyqsDone = false, revisionCount = 1, isCompleted = false, completionPercentage = 60),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 10, title = "Wave Optics", totalLectures = 6, completedLectures = 0, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 0),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 11, title = "Dual Nature of Radiation & Matter", totalLectures = 4, completedLectures = 2, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 35),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 12, title = "Atoms", totalLectures = 3, completedLectures = 0, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 0),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 13, title = "Nuclei", totalLectures = 3, completedLectures = 1, notesDone = true, pyqsDone = false, revisionCount = 1, isCompleted = false, completionPercentage = 45),
                    ChapterEntity(subjectId = physicsId, chapterNumber = 14, title = "Semiconductor Electronics", totalLectures = 5, completedLectures = 0, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 0)
                )
            )

            db.subjectDao().insertChapters(
                listOf(
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 1, title = "Solutions", totalLectures = 5, completedLectures = 5, notesDone = true, pyqsDone = true, revisionCount = 3, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 2, title = "Electrochemistry", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 3, title = "Chemical Kinetics", totalLectures = 5, completedLectures = 5, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 4, title = "d and f Block Elements", totalLectures = 4, completedLectures = 4, notesDone = true, pyqsDone = true, revisionCount = 1, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 5, title = "Coordination Compounds", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 6, title = "Haloalkanes and Haloarenes", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 7, title = "Alcohols, Phenols and Ethers", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 1, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 8, title = "Aldehydes, Ketones & Carboxylic Acids", totalLectures = 7, completedLectures = 7, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 9, title = "Amines", totalLectures = 4, completedLectures = 4, notesDone = true, pyqsDone = true, revisionCount = 1, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 10, title = "Biomolecules", totalLectures = 4, completedLectures = 1, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 25),
                    ChapterEntity(subjectId = chemistryId, chapterNumber = 11, title = "p-Block Elements Revision", totalLectures = 5, completedLectures = 3, notesDone = true, pyqsDone = false, revisionCount = 2, isCompleted = false, completionPercentage = 70)
                )
            )

            db.subjectDao().insertChapters(
                listOf(
                    ChapterEntity(subjectId = biologyId, chapterNumber = 1, title = "Sexual Reproduction in Flowering Plants", totalLectures = 5, completedLectures = 5, notesDone = true, pyqsDone = true, revisionCount = 3, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = biologyId, chapterNumber = 2, title = "Human Reproduction", totalLectures = 6, completedLectures = 6, notesDone = true, pyqsDone = true, revisionCount = 3, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = biologyId, chapterNumber = 3, title = "Reproductive Health", totalLectures = 3, completedLectures = 3, notesDone = true, pyqsDone = true, revisionCount = 2, isCompleted = true, completionPercentage = 100),
                    ChapterEntity(subjectId = biologyId, chapterNumber = 4, title = "Principles of Inheritance and Variation (Genetics)", totalLectures = 8, completedLectures = 4, notesDone = true, pyqsDone = false, revisionCount = 1, isCompleted = false, completionPercentage = 55),
                    ChapterEntity(subjectId = biologyId, chapterNumber = 5, title = "Molecular Basis of Inheritance", totalLectures = 8, completedLectures = 0, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 0),
                    ChapterEntity(subjectId = biologyId, chapterNumber = 6, title = "Evolution", totalLectures = 5, completedLectures = 0, notesDone = false, pyqsDone = false, revisionCount = 0, isCompleted = false, completionPercentage = 0)
                )
            )

            // 5. Populate Default Habits
            val defaultHabits = listOf(
                HabitEntity(name = "Wake Early (06:00 AM)", iconName = "alarm", colorHex = "#FFB300", weight = 15, isNegativeHabit = false, targetUnit = "Time", targetNumeric = 1, streak = 11, bestStreak = 21, orderIndex = 0, habitType = HabitType.SLEEP),
                HabitEntity(name = "Daily Workout", iconName = "fitness_center", colorHex = "#00E676", weight = 20, isNegativeHabit = false, targetUnit = "Session", targetNumeric = 1, streak = 9, bestStreak = 18, orderIndex = 1, habitType = HabitType.WORKOUT),
                HabitEntity(name = "Deep Study (6h+)", iconName = "menu_book", colorHex = "#38E1FF", weight = 40, isNegativeHabit = false, targetUnit = "Hours", targetNumeric = 6, streak = 11, bestStreak = 21, orderIndex = 2, habitType = HabitType.DEEP_STUDY),
                HabitEntity(name = "Book Reading", iconName = "auto_stories", colorHex = "#B388FF", weight = 10, isNegativeHabit = false, targetUnit = "Pages", targetNumeric = 10, streak = 7, bestStreak = 14, orderIndex = 3, habitType = HabitType.READING),
                HabitEntity(name = "No Porn (Discipline)", iconName = "shield", colorHex = "#FF5722", weight = 15, isNegativeHabit = true, targetUnit = "Days Clean", targetNumeric = 1, streak = 27, bestStreak = 27, orderIndex = 4, habitType = HabitType.NO_PORN),
                HabitEntity(name = "No Reels / Doomscroll", iconName = "do_not_disturb", colorHex = "#FF3D71", weight = 10, isNegativeHabit = true, targetUnit = "Days Clean", targetNumeric = 1, streak = 14, bestStreak = 14, orderIndex = 5, habitType = HabitType.NO_REELS),
                HabitEntity(name = "Meditation", iconName = "self_improvement", colorHex = "#70B8FF", weight = 5, isNegativeHabit = false, targetUnit = "Minutes", targetNumeric = 15, streak = 6, bestStreak = 12, orderIndex = 6, habitType = HabitType.MEDITATION),
                HabitEntity(name = "Water Intake (3.5L)", iconName = "water_drop", colorHex = "#00C2FF", weight = 5, isNegativeHabit = false, targetUnit = "Liters", targetNumeric = 3, streak = 15, bestStreak = 20, orderIndex = 7, habitType = HabitType.HYDRATION)
            )
            val insertedHabitIds = defaultHabits.map { db.habitDao().insertHabit(it) }

            // Log today's habit completions
            if (insertedHabitIds.isNotEmpty()) {
                db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = insertedHabitIds[0], date = today, isCompleted = true))
                db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = insertedHabitIds[1], date = today, isCompleted = true))
                db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = insertedHabitIds[2], date = today, isCompleted = true))
                db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = insertedHabitIds[4], date = today, isCompleted = true)) // No porn
                db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = insertedHabitIds[5], date = today, isCompleted = true)) // No reels
                db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = insertedHabitIds[7], date = today, isCompleted = true)) // Water
            }

            // 6. Indian Festivals & Holidays
            db.holidayDao().insertHolidays(
                listOf(
                    HolidayEntity(name = "Holi", date = "03-25", isIndianFestival = true, workloadReductionPercent = 50, note = "Festival of Colors"),
                    HolidayEntity(name = "Diwali", date = "11-01", isIndianFestival = true, workloadReductionPercent = 75, note = "Deepawali Celebration"),
                    HolidayEntity(name = "Chhath Puja", date = "11-07", isIndianFestival = true, workloadReductionPercent = 75, note = "Sun God Mahaparv"),
                    HolidayEntity(name = "Durga Puja", date = "10-10", isIndianFestival = true, workloadReductionPercent = 50, note = "Navratri Festival"),
                    HolidayEntity(name = "Raksha Bandhan", date = "08-19", isIndianFestival = true, workloadReductionPercent = 50, note = "Sibling Festival"),
                    HolidayEntity(name = "Independence Day", date = "08-15", isIndianFestival = true, workloadReductionPercent = 50, note = "National Holiday"),
                    HolidayEntity(name = "Republic Day", date = "01-26", isIndianFestival = true, workloadReductionPercent = 50, note = "National Republic Day")
                )
            )

            // 7. Initial Smart Daily Plan Tasks for Today
            db.dailyPlanDao().insertTasks(
                listOf(
                    DailyPlanTaskEntity(date = today, subject = "Physics", title = "Nuclei Lecture", type = com.example.data.local.entity.TaskType.LECTURE, details = "Chapter 13 - Binding Energy curve and mass defect", targetMinutes = 45, isCompleted = true, orderIndex = 0, xpReward = 60),
                    DailyPlanTaskEntity(date = today, subject = "Physics", title = "Nuclei Notes", type = com.example.data.local.entity.TaskType.NOTES, details = "Formulate high-yield formula sheets and diagrams", targetMinutes = 30, isCompleted = true, orderIndex = 1, xpReward = 40),
                    DailyPlanTaskEntity(date = today, subject = "Chemistry", title = "P Block Revision", type = com.example.data.local.entity.TaskType.REVISION, details = "Group 15, 16 elements reactions and anomalous properties", targetMinutes = 50, isCompleted = true, orderIndex = 2, xpReward = 50),
                    DailyPlanTaskEntity(date = today, subject = "Biology", title = "Genetics Lecture", type = com.example.data.local.entity.TaskType.LECTURE, details = "Mendelian inheritance laws and test cross analysis", targetMinutes = 60, isCompleted = false, orderIndex = 3, xpReward = 60),
                    DailyPlanTaskEntity(date = today, subject = "Workout", title = "Running 20 min", type = com.example.data.local.entity.TaskType.WORKOUT, details = "Zone 2 cardio warmup", targetMinutes = 20, isCompleted = true, orderIndex = 4, xpReward = 30),
                    DailyPlanTaskEntity(date = today, subject = "Workout", title = "Pushups 3 x 15", type = com.example.data.local.entity.TaskType.WORKOUT, details = "Standard + Diamond pushups", targetMinutes = 15, isCompleted = true, orderIndex = 5, xpReward = 25),
                    DailyPlanTaskEntity(date = today, subject = "Workout", title = "Squats 3 x 20", type = com.example.data.local.entity.TaskType.WORKOUT, details = "Bodyweight explosive squats", targetMinutes = 15, isCompleted = false, orderIndex = 6, xpReward = 25)
                )
            )

            // 8. Sample Past School Status Logs for rich Analytics
            db.schoolStatusDao().insertOrUpdate(
                SchoolStatusEntity(
                    date = today,
                    currentState = com.example.data.local.entity.SchoolState.ARRIVED_HOME,
                    dispatchSchoolTime = System.currentTimeMillis() - 7 * 3600 * 1000,
                    arrivedSchoolTime = System.currentTimeMillis() - 6 * 3600 * 1000 - 35 * 60 * 1000,
                    dispatchHomeTime = System.currentTimeMillis() - 2 * 3600 * 1000,
                    arrivedHomeTime = System.currentTimeMillis() - 1 * 3600 * 1000 - 30 * 60 * 1000,
                    travelToSchoolMinutes = 25,
                    travelHomeMinutes = 30,
                    inSchoolMinutes = 275,
                    isPresent = true
                )
            )

            // 9. Initial Study Sessions for today
            db.subjectDao().insertStudySession(
                StudySessionEntity(
                    subjectName = "Physics",
                    chapterName = "Nuclei",
                    sessionType = com.example.data.local.entity.SessionType.POMODORO_50_10,
                    durationMinutes = 50,
                    date = today,
                    xpEarned = 100
                )
            )
            db.subjectDao().insertStudySession(
                StudySessionEntity(
                    subjectName = "Chemistry",
                    chapterName = "P Block Elements",
                    sessionType = com.example.data.local.entity.SessionType.DEEP_WORK,
                    durationMinutes = 60,
                    date = today,
                    xpEarned = 120
                )
            )

            // 10. Initial Workouts
            db.workoutDao().insertWorkouts(
                listOf(
                    WorkoutLogEntity(date = today, exerciseName = "Running", exerciseType = com.example.data.local.entity.ExerciseType.RUNNING, level = com.example.data.local.entity.WorkoutLevel.INTERMEDIATE, durationMinutes = 20, distanceKm = 3.2f, caloriesBurned = 180, isCompleted = true),
                    WorkoutLogEntity(date = today, exerciseName = "Pushups", exerciseType = com.example.data.local.entity.ExerciseType.PUSHUPS, level = com.example.data.local.entity.WorkoutLevel.INTERMEDIATE, sets = 3, reps = 15, durationMinutes = 15, isCompleted = true),
                    WorkoutLogEntity(date = today, exerciseName = "Squats", exerciseType = com.example.data.local.entity.ExerciseType.SQUATS, level = com.example.data.local.entity.WorkoutLevel.INTERMEDIATE, sets = 3, reps = 20, durationMinutes = 15, isCompleted = false)
                )
            )

            // 11. Initial Study Notes & Formulas
            db.noteDao().insertNote(
                NoteEntity(
                    title = "Modern Physics High-Yield Formulas",
                    content = "1. E = hc/λ = 1240 eV·nm / λ\n2. Nuclear radius R = R₀·A^(1/3) where R₀ = 1.2 fm\n3. Binding Energy per nucleon peak around Fe-56 (~8.75 MeV)\n4. Half life T₁/₂ = 0.693 / λ\n5. de Broglie wavelength λ = h/p = h/√(2mE)",
                    subjectTag = "Physics",
                    date = today,
                    isPinned = true,
                    colorHex = "#7C8CFF"
                )
            )
            db.noteDao().insertNote(
                NoteEntity(
                    title = "Class 12 Boards 95%+ Non-Negotiable Protocol",
                    content = "• Prioritize NCERT back exercises & exemplar questions.\n• Master organic chemistry named reactions (Aldol, Cannizzaro, Reimer-Tiemann, Kolbe).\n• Spaced repetition revision cycle: Day 1, Day 3, Day 7, Day 21.\n• Solve at least 10 previous year CBSE question papers under strict 3-hour exam simulation timer.",
                    subjectTag = "Strategy",
                    date = today,
                    isPinned = true,
                    colorHex = "#4ADE80"
                )
            )

            // 12. Initial Reflection
            db.reflectionDao().insertOrUpdate(
                DailyReflectionEntity(
                    date = today,
                    dailyScore = 9,
                    whatWentWell = "Cleared entire Nuclei chapter concept and completed 3 sets of pushups without breaking form. Kept phone away during 50-minute Pomodoro blocks.",
                    whatHeldMeBack = "Slight fatigue post-school commute; need to hydrate more immediately upon reaching home at 01:00 PM.",
                    gratitude = "Grateful for mental clarity, good health, and the discipline to stay on the Winter Arc.",
                    tomorrowGoal = "Finish Genetics dihybrid crosses lecture and solve 15 PYQs from P-Block elements.",
                    mood = "Unstoppable"
                )
            )
        }
    }
}
