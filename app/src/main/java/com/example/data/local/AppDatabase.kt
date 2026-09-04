package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AiPlanDao
import com.example.data.local.dao.AlarmDao
import com.example.data.local.dao.BoardExamDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.DailyPlanDao
import com.example.data.local.dao.DisciplineDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HolidayDao
import com.example.data.local.dao.LevelPurchaseDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.ReflectionDao
import com.example.data.local.dao.SchoolStatusDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.SyllabusDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.dao.WinterArcDao
import com.example.data.local.dao.WinterArcObjectivesDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.dao.XpTransactionDao
import com.example.data.local.entity.AiPlanCacheEntity
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.AlarmLogEntity
import com.example.data.local.entity.ArcGoalPlanItemEntity
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.DailyReflectionEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HabitType
import com.example.data.local.entity.HolidayEntity
import com.example.data.local.entity.LevelPurchaseEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.SchoolStatusEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.SyllabusChapterEntity
import com.example.data.local.entity.SyllabusTopicEntity
import com.example.data.local.entity.SyllabusUnitEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WinterArcObjectiveEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.local.entity.XpTransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserProfileEntity::class,
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
        AiPlanCacheEntity::class,
        GoalEntity::class,
        SyllabusUnitEntity::class,
        SyllabusChapterEntity::class,
        SyllabusTopicEntity::class,
        AlarmEntity::class,
        AlarmLogEntity::class,
        WinterArcObjectiveEntity::class,
        ArcGoalPlanItemEntity::class,
        ChatMessageEntity::class,
        XpTransactionEntity::class,
        LevelPurchaseEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
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
    abstract fun goalDao(): GoalDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun alarmDao(): AlarmDao
    abstract fun winterArcObjectivesDao(): WinterArcObjectivesDao
    abstract fun chatDao(): ChatDao
    abstract fun xpTransactionDao(): XpTransactionDao
    abstract fun levelPurchaseDao(): LevelPurchaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN habitType TEXT NOT NULL DEFAULT 'CUSTOM'")
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `goals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `targetDate` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `progressPercentage` INTEGER NOT NULL,
                        `reminderEnabled` INTEGER NOT NULL,
                        `reminderHour` INTEGER,
                        `reminderMinute` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE daily_plan_tasks ADD COLUMN reminderHour INTEGER")
                db.execSQL("ALTER TABLE daily_plan_tasks ADD COLUMN reminderMinute INTEGER")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `studentClass` TEXT NOT NULL,
                        `board` TEXT NOT NULL,
                        `stream` TEXT NOT NULL,
                        `targetPercentage` INTEGER NOT NULL,
                        `targetExamName` TEXT NOT NULL,
                        `targetExamDate` TEXT NOT NULL,
                        `selectedSubjectsJson` TEXT NOT NULL,
                        `strongSubjectsJson` TEXT NOT NULL,
                        `weakSubjectsJson` TEXT NOT NULL,
                        `preparationLevel` TEXT NOT NULL,
                        `hasSchool` INTEGER NOT NULL,
                        `schoolStartTime` TEXT NOT NULL,
                        `schoolEndTime` TEXT NOT NULL,
                        `travelTimeMinutes` INTEGER NOT NULL,
                        `weeklyOffDaysJson` TEXT NOT NULL,
                        `wakeUpTime` TEXT NOT NULL,
                        `sleepTime` TEXT NOT NULL,
                        `dailyStudyGoalHours` REAL NOT NULL,
                        `preferredSessionDurationMinutes` INTEGER NOT NULL,
                        `workoutGoal` TEXT NOT NULL,
                        `workoutType` TEXT NOT NULL,
                        `workoutTime` TEXT NOT NULL,
                        `workoutDurationMinutes` INTEGER NOT NULL,
                        `coachingStyle` TEXT NOT NULL,
                        `geminiApiKey` TEXT NOT NULL,
                        `notifyWakeUp` INTEGER NOT NULL,
                        `notifySchoolDeparture` INTEGER NOT NULL,
                        `notifySchoolArrival` INTEGER NOT NULL,
                        `notifyReturnHome` INTEGER NOT NULL,
                        `notifyStudySessions` INTEGER NOT NULL,
                        `notifyWorkout` INTEGER NOT NULL,
                        `notifyRevision` INTEGER NOT NULL,
                        `notifyReflection` INTEGER NOT NULL,
                        `notifySleep` INTEGER NOT NULL,
                        `createdAtTimestamp` INTEGER NOT NULL,
                        `updatedAtTimestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rebuild_os_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .addCallback(DatabasePrepopulationCallback(scope))
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
                    populateInitialSystemData(database)
                }
            }
        }

        private suspend fun populateInitialSystemData(db: AppDatabase) {
            // ONLY populate standard Indian Festivals and Holidays.
            // NO hardcoded user tasks, chapters, XP, streak, scores or mock logs are shipped.
            // All user data will be dynamically initialized when the user completes Onboarding!
            db.holidayDao().insertHolidays(
                listOf(
                    HolidayEntity(name = "Republic Day", date = "01-26", isIndianFestival = true, workloadReductionPercent = 50, note = "National Republic Day"),
                    HolidayEntity(name = "Maha Shivratri", date = "03-08", isIndianFestival = true, workloadReductionPercent = 50, note = "Auspicious Festival"),
                    HolidayEntity(name = "Holi", date = "03-25", isIndianFestival = true, workloadReductionPercent = 60, note = "Festival of Colors"),
                    HolidayEntity(name = "Eid ul-Fitr", date = "04-11", isIndianFestival = true, workloadReductionPercent = 50, note = "Celebration of Joy"),
                    HolidayEntity(name = "Independence Day", date = "08-15", isIndianFestival = true, workloadReductionPercent = 50, note = "National Independence Day"),
                    HolidayEntity(name = "Raksha Bandhan", date = "08-19", isIndianFestival = true, workloadReductionPercent = 50, note = "Sibling Festival"),
                    HolidayEntity(name = "Janmashtami", date = "08-26", isIndianFestival = true, workloadReductionPercent = 50, note = "Lord Krishna Birthday"),
                    HolidayEntity(name = "Gandhi Jayanti", date = "10-02", isIndianFestival = true, workloadReductionPercent = 50, note = "National Holiday"),
                    HolidayEntity(name = "Durga Puja / Dussehra", date = "10-12", isIndianFestival = true, workloadReductionPercent = 60, note = "Victory of Good over Evil"),
                    HolidayEntity(name = "Diwali", date = "11-01", isIndianFestival = true, workloadReductionPercent = 75, note = "Deepawali Celebration"),
                    HolidayEntity(name = "Chhath Puja", date = "11-07", isIndianFestival = true, workloadReductionPercent = 75, note = "Sun God Mahaparv"),
                    HolidayEntity(name = "Christmas", date = "12-25", isIndianFestival = true, workloadReductionPercent = 50, note = "Christmas Day")
                )
            )
        }
    }
}
