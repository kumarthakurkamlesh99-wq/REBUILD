package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HabitType
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.math.ceil

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DisciplineScoreUnitTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RebuildRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RebuildRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Bug 3 Fix Verification:
     * Toggling exactly one study task's isCompleted status must change the total score
     * by at most (40 / number_of_study_tasks_that_day) points, proving one task's contribution
     * is strictly bounded and cannot cause wild swings.
     */
    @Test
    fun togglingOneStudyTaskChangesScoreByAtMostWeightPerTask() = runBlocking {
        val testDate = "2026-09-01"
        val totalStudyTasksCount = 5
        val maxStudyPoints = 40
        val maxExpectedDelta = ceil(maxStudyPoints.toDouble() / totalStudyTasksCount).toInt() // 8 points

        // 1. Seed 5 study tasks
        val tasks = listOf(
            DailyPlanTaskEntity(id = 1, date = testDate, subject = "Physics", title = "Lecture 1", type = TaskType.LECTURE, isCompleted = true),
            DailyPlanTaskEntity(id = 2, date = testDate, subject = "Physics", title = "Notes 1", type = TaskType.NOTES, isCompleted = true),
            DailyPlanTaskEntity(id = 3, date = testDate, subject = "Chemistry", title = "Revision 1", type = TaskType.REVISION, isCompleted = false),
            DailyPlanTaskEntity(id = 4, date = testDate, subject = "Biology", title = "Lecture 2", type = TaskType.LECTURE, isCompleted = false),
            DailyPlanTaskEntity(id = 5, date = testDate, subject = "English", title = "Chapter 1", type = TaskType.NOTES, isCompleted = false)
        )
        db.dailyPlanDao().insertTasks(tasks)

        // 2. Seed workout and habits to test total score stability across all categories
        db.workoutDao().insertWorkout(WorkoutLogEntity(id = 1, date = testDate, exerciseName = "Pushups", isCompleted = true))

        val sleepHabitId = db.habitDao().insertHabit(
            HabitEntity(id = 10, name = "Wake Early (06:00 AM)", habitType = HabitType.SLEEP, weight = 15)
        )
        val noPornHabitId = db.habitDao().insertHabit(
            HabitEntity(id = 11, name = "No Porn (Discipline)", habitType = HabitType.NO_PORN, weight = 15)
        )
        val readingHabitId = db.habitDao().insertHabit(
            HabitEntity(id = 12, name = "Book Reading", habitType = HabitType.READING, weight = 10)
        )

        db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = sleepHabitId, date = testDate, isCompleted = true))
        db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = noPornHabitId, date = testDate, isCompleted = true))
        db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = readingHabitId, date = testDate, isCompleted = true))

        // Initial compute: 2/5 study tasks done -> (2/5 * 40) = 16 pts
        // Workout = 20 pts, Sleep = 15 pts, NoPorn = 15 pts, Reading = 10 pts
        // Total expected = 16 + 20 + 15 + 15 + 10 = 76
        repository.recalculateDisciplineScore(testDate)
        val initialDiscipline = db.disciplineDao().getDisciplineForDateDirect(testDate)
        assertNotNull(initialDiscipline)
        val initialScore = initialDiscipline!!.totalScore
        assertEquals(76, initialScore)

        // 3. Toggle exactly ONE study task (Task 3 from incomplete to complete)
        val targetTask = db.dailyPlanDao().getTasksForDateDirect(testDate).first { it.id == 3L }
        repository.toggleTaskCompleted(targetTask)

        // New compute: 3/5 study tasks done -> (3/5 * 40) = 24 pts (+8 pts)
        // Total expected = 24 + 20 + 15 + 15 + 10 = 84
        val afterToggleDiscipline = db.disciplineDao().getDisciplineForDateDirect(testDate)
        assertNotNull(afterToggleDiscipline)
        val newScore = afterToggleDiscipline!!.totalScore
        assertEquals(84, newScore)

        val scoreDelta = abs(newScore - initialScore)
        assertTrue(
            "Score changed by $scoreDelta, which exceeds maximum bound $maxExpectedDelta",
            scoreDelta <= maxExpectedDelta
        )

        // 4. Toggle back from complete to incomplete
        val updatedTask = db.dailyPlanDao().getTasksForDateDirect(testDate).first { it.id == 3L }
        repository.toggleTaskCompleted(updatedTask)

        val revertedDiscipline = db.disciplineDao().getDisciplineForDateDirect(testDate)
        assertNotNull(revertedDiscipline)
        val revertedScore = revertedDiscipline!!.totalScore
        assertEquals(initialScore, revertedScore)
        val revertDelta = abs(revertedScore - newScore)
        assertTrue(
            "Revert delta $revertDelta exceeds maximum bound $maxExpectedDelta",
            revertDelta <= maxExpectedDelta
        )
    }

    /**
     * Bug 2 Fix Verification:
     * When there are no tasks or logs for a date, the score must be 0 and never fabricate
     * arbitrary fallback points (such as 35 for study or 20 for workout).
     */
    @Test
    fun emptyDateYieldsZeroScoreWithoutFabricatedFallbacks() = runBlocking {
        val emptyDate = "2026-10-15"
        repository.recalculateDisciplineScore(emptyDate)

        val discipline = db.disciplineDao().getDisciplineForDateDirect(emptyDate)
        assertNotNull(discipline)
        assertEquals(0, discipline!!.studyScore)
        assertEquals(0, discipline.workoutScore)
        assertEquals(0, discipline.noPornScore)
        assertEquals(0, discipline.sleepScore)
        assertEquals(0, discipline.readingScore)
        assertEquals(0, discipline.totalScore)
    }

    /**
     * Bug 1 Fix Verification:
     * Habits are identified and scored strictly by HabitType enum, not display name substrings.
     */
    @Test
    fun habitScoringUsesStableHabitTypeEnumInsteadOfNameSubstring() = runBlocking {
        val testDate = "2026-09-02"

        // Habit with custom name that does not contain 'wake' or 'sleep'
        val customNamedSleepHabit = HabitEntity(
            id = 20,
            name = "Morning Protocol 05:30",
            habitType = HabitType.SLEEP,
            weight = 15
        )
        val customNamedPornHabit = HabitEntity(
            id = 21,
            name = "Pure Mind Vow",
            habitType = HabitType.NO_PORN,
            weight = 15
        )
        db.habitDao().insertHabit(customNamedSleepHabit)
        db.habitDao().insertHabit(customNamedPornHabit)

        db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = 20, date = testDate, isCompleted = true))
        db.habitDao().insertOrUpdateLog(HabitLogEntity(habitId = 21, date = testDate, isCompleted = true))

        repository.recalculateDisciplineScore(testDate)

        val discipline = db.disciplineDao().getDisciplineForDateDirect(testDate)
        assertNotNull(discipline)
        assertEquals(15, discipline!!.sleepScore)
        assertEquals(15, discipline.noPornScore)
        assertEquals(30, discipline.totalScore)
    }
}
