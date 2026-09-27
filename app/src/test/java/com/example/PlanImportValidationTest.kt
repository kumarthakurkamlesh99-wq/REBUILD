package com.example

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.util.PlanImportManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlanImportValidationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var planImportManager: PlanImportManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        planImportManager = PlanImportManager(context, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTempJsonUri(json: String): Uri {
        val file = File(context.cacheDir, "test_plan_${System.currentTimeMillis()}.json")
        file.writeText(json)
        return Uri.fromFile(file)
    }

    @Test
    fun testHumanReadableErrorForMissingHabitName() = runBlocking {
        // Missing name at habit #2
        val json = """
        {
            "plan_name": "Test Plan",
            "habits": [
                { "name": "Morning Cardio" },
                { "is_negative": false, "target_numeric": 5 }
            ]
        }
        """.trimIndent()

        val uri = createTempJsonUri(json)
        val result = planImportManager.validateAndParsePlan(uri)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        val error = result.errors[0]
        assertEquals("Habit #2 is missing Name", error.summaryBullet)
        assertTrue(error.friendlyMessage.contains("Habit #2 is missing a Name"))
        assertTrue(error.technicalDetails.contains("$.habits[1]"))
    }

    @Test
    fun testValidateEntireFileBeforeImport_CollectsAllIssues() = runBlocking {
        // Goal #1 missing Title, Task #2 missing Title, Habit #1 missing Name, Alarm #1 invalid time format
        val json = """
        {
            "plan_name": "Broken Plan",
            "goals": [
                { "description": "No title goal" }
            ],
            "tasks": [
                { "title": "Valid Task" },
                { "subject": "Math", "target_minutes": 60 }
            ],
            "habits": [
                { "is_negative": true }
            ],
            "alarms": [
                { "title": "Wake Up", "time": "25:99" }
            ]
        }
        """.trimIndent()

        val uri = createTempJsonUri(json)
        val result = planImportManager.validateAndParsePlan(uri)

        assertFalse(result.isValid)
        assertEquals(4, result.errors.size)

        val bullets = result.errors.map { it.summaryBullet }
        assertTrue(bullets.any { it.contains("Goal #1 is missing Title") })
        assertTrue(bullets.any { it.contains("Task #2 is missing Title") })
        assertTrue(bullets.any { it.contains("Habit #1 is missing Name") })
        assertTrue(bullets.any { it.contains("Alarm #1 has invalid time format") })
    }

    @Test
    fun testAutoCompatibilityMode_SupportsAliases() = runBlocking {
        // Using aliases:
        // plan_title -> plan_name
        // habit with "title" -> name
        // task with "task_name" -> title
        // goal with "goal_name" -> title
        // alarm with "alarm_time" -> time
        val json = """
        {
            "plan_title": "Legacy Formatted Plan",
            "goals": [
                { "goal_name": "Crack IIT JEE", "category": "ACADEMIC" }
            ],
            "tasks": [
                { "task_name": "Organic Chemistry Revision", "duration": 90 }
            ],
            "habits": [
                { "title": "Chemistry Study", "target": 2 }
            ],
            "alarms": [
                { "alarm_title": "Morning Routine", "alarm_time": "06:30" }
            ]
        }
        """.trimIndent()

        val uri = createTempJsonUri(json)
        val result = planImportManager.validateAndParsePlan(uri)

        assertTrue("Expected valid plan with auto-compatibility aliases, but errors: ${result.errors}", result.isValid)
        assertNotNull(result.plan)
        val plan = result.plan!!

        assertEquals("Legacy Formatted Plan", plan.planName)
        assertEquals("Crack IIT JEE", plan.goals?.get(0)?.title)
        assertEquals("Organic Chemistry Revision", plan.tasks?.get(0)?.title)
        assertEquals(90, plan.tasks?.get(0)?.targetMinutes)
        assertEquals("Chemistry Study", plan.habits?.get(0)?.name)
        assertEquals(2, plan.habits?.get(0)?.targetNumeric)
        assertEquals("06:30", plan.alarms?.get(0)?.time)
    }

    @Test
    fun testGracefulDefaultsForOptionalFields() = runBlocking {
        // Minimal objects with only strictly required fields
        val json = """
        {
            "goals": [
                { "title": "Read 20 Books" }
            ],
            "tasks": [
                { "title": "Deep Work Block" }
            ],
            "habits": [
                { "name": "Cold Shower" }
            ],
            "alarms": [
                { "time": "07:15" }
            ]
        }
        """.trimIndent()

        val uri = createTempJsonUri(json)
        val result = planImportManager.validateAndParsePlan(uri)

        assertTrue(result.isValid)
        val plan = result.plan!!

        // Default plan name
        assertEquals("Imported Plan", plan.planName)

        // Goal defaults
        assertEquals("Read 20 Books", plan.goals?.get(0)?.title)
        assertEquals("", plan.goals?.get(0)?.description)
        assertEquals("ACADEMIC", plan.goals?.get(0)?.category)

        // Task defaults
        assertEquals("Deep Work Block", plan.tasks?.get(0)?.title)
        assertEquals(45, plan.tasks?.get(0)?.targetMinutes)
        assertEquals("General", plan.tasks?.get(0)?.subject)
        assertEquals(0, plan.tasks?.get(0)?.xp)

        // Habit defaults
        assertEquals("Cold Shower", plan.habits?.get(0)?.name)
        assertEquals("CUSTOM", plan.habits?.get(0)?.habitType)
        assertEquals(false, plan.habits?.get(0)?.isNegative)
        assertEquals("Completed", plan.habits?.get(0)?.targetUnit)
        assertEquals(1, plan.habits?.get(0)?.targetNumeric)

        // Alarm defaults
        assertEquals("07:15", plan.alarms?.get(0)?.time)
        assertEquals("Alarm #1", plan.alarms?.get(0)?.title)
        assertEquals("MATH", plan.alarms?.get(0)?.challengeType)
        assertEquals("MEDIUM", plan.alarms?.get(0)?.difficulty)
    }

    @Test
    fun testImportReportReturnsCorrectCounts() = runBlocking {
        val json = """
        {
            "plan_name": "Championship Plan",
            "goals": [
                { "title": "Goal 1" },
                { "title": "Goal 2" }
            ],
            "tasks": [
                { "title": "Task 1" },
                { "title": "Task 2" },
                { "title": "Task 3" }
            ],
            "habits": [
                { "name": "Habit 1" }
            ],
            "alarms": [
                { "time": "06:00" },
                { "time": "22:00" }
            ]
        }
        """.trimIndent()

        val uri = createTempJsonUri(json)
        val validation = planImportManager.validateAndParsePlan(uri)
        assertTrue(validation.isValid)

        val report = planImportManager.importPlan(validation.plan!!, replaceMode = true)
        assertEquals("Championship Plan", report.planName)
        assertEquals(2, report.goalsCount)
        assertEquals(3, report.tasksCount)
        assertEquals(1, report.habitsCount)
        assertEquals(2, report.alarmsCount)
    }
}
