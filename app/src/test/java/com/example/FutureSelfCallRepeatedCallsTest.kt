package com.example

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.FutureSelfCallState
import com.example.data.local.entity.TaskType
import com.example.data.repository.RebuildRepository
import com.example.notification.AlarmScheduler
import com.example.notification.BootReceiver
import com.example.notification.FutureSelfCallEngine
import com.example.notification.FutureSelfCallReceiver
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FutureSelfCallRepeatedCallsTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: RebuildRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(
            context,
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        )
        repository = RebuildRepository(database, context)
        runBlocking {
            database.dailyPlanDao().clearAll()
            database.futureSelfCallDao().clearAll()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            database.dailyPlanDao().clearAll()
            database.futureSelfCallDao().clearAll()
        }
    }

    /**
     * TEST 1: Answer a call while its task is pending -> NO repeated call.
     * Task completion remains separate from call state.
     */
    @Test
    fun testAnswerCallWhileTaskPending_noRepeatedCall() = runBlocking {
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomorrowCal.time)

        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = tomorrowStr,
                subject = "Physics",
                title = "Electrostatics Deep Work",
                type = TaskType.LECTURE,
                targetMinutes = 90,
                isCompleted = false,
                reminderHour = 6,
                reminderMinute = 0,
                startTime = "06:00 AM"
            )
        )

        val occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, tomorrowStr, 6, 0)

        // 1. Initial call arrives: can initiate call
        val canProceed1 = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "06:00 AM"
        )
        assertTrue("First call attempt must proceed", canProceed1)

        val ringingRecord = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertNotNull(ringingRecord)
        assertEquals(FutureSelfCallState.RINGING, ringingRecord?.state)

        // 2. User answers the call
        FutureSelfCallEngine.markAnswered(context, occurrenceId, taskId)

        // Verify call state is ANSWERED, but task is still pending (NOT completed)
        val answeredRecord = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertNotNull(answeredRecord)
        assertEquals(FutureSelfCallState.ANSWERED, answeredRecord?.state)

        val taskInDb = database.dailyPlanDao().getTaskById(taskId)
        assertNotNull(taskInDb)
        assertFalse("Answering call must NOT mark task as completed", taskInDb!!.isCompleted)

        // 3. Reschedule upcoming task alarms (e.g. via worker or update)
        AlarmScheduler.rescheduleUpcomingTaskAlarms(context)

        // 4. Any subsequent attempt to initiate call for this same occurrence must be rejected
        val canProceedAgain = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "06:00 AM"
        )
        assertFalse("Answered call must NEVER be re-initiated", canProceedAgain)
    }

    /**
     * TEST 2: Dismiss a call without completing the task -> NO repeated call.
     */
    @Test
    fun testDismissCallWithoutCompletingTask_noRepeatedCall() = runBlocking {
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomorrowCal.time)

        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = tomorrowStr,
                subject = "Chemistry",
                title = "Thermodynamics",
                type = TaskType.LECTURE,
                targetMinutes = 60,
                isCompleted = false,
                reminderHour = 7,
                reminderMinute = 0,
                startTime = "07:00 AM"
            )
        )

        val occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, tomorrowStr, 7, 0)

        // Call rings
        val canProceed = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "07:00 AM"
        )
        assertTrue(canProceed)

        // User dismisses the call
        FutureSelfCallEngine.markDismissed(context, occurrenceId, taskId, "USER_DECLINED")

        val record = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertNotNull(record)
        assertEquals(FutureSelfCallState.DISMISSED, record?.state)

        val taskInDb = database.dailyPlanDao().getTaskById(taskId)
        assertFalse("Dismissing call must NOT complete the task", taskInDb!!.isCompleted)

        // Verify it cannot be triggered again
        val canProceedAgain = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "07:00 AM"
        )
        assertFalse("Dismissed call must NOT be triggered again", canProceedAgain)
    }

    /**
     * TEST 3: Complete the task -> all pending call triggers cancelled.
     */
    @Test
    fun testCompleteTask_cancelsAllPendingCalls() = runBlocking {
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomorrowCal.time)

        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = tomorrowStr,
                subject = "Maths",
                title = "Calculus Integration",
                type = TaskType.PYQ,
                targetMinutes = 90,
                isCompleted = false,
                reminderHour = 8,
                reminderMinute = 0,
                startTime = "08:00 AM"
            )
        )

        val occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, tomorrowStr, 8, 0)

        // Register scheduled call record
        database.futureSelfCallDao().insertOrUpdate(
            com.example.data.local.entity.FutureSelfCallRecordEntity(
                callOccurrenceId = occurrenceId,
                taskId = taskId,
                scheduledDate = tomorrowStr,
                scheduledTime = "08:00 AM",
                scheduledTimestamp = System.currentTimeMillis() + 86400000L,
                state = FutureSelfCallState.SCHEDULED
            )
        )

        // User marks task completed
        val task = database.dailyPlanDao().getTaskById(taskId)!!
        repository.toggleTaskCompleted(task)

        // Verify task is completed
        val updatedTask = database.dailyPlanDao().getTaskById(taskId)
        assertTrue(updatedTask!!.isCompleted)

        // Verify call record is CANCELLED
        val callRecord = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertEquals(FutureSelfCallState.CANCELLED, callRecord?.state)

        // Attempting to initiate call on completed task must be blocked
        val canProceed = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "08:00 AM"
        )
        assertFalse("Completed task call must be cancelled and blocked", canProceed)
    }

    /**
     * TEST 4: Snooze: exactly one call occurs after the chosen delay.
     */
    @Test
    fun testSnooze_exactlyOneCallOccursAfterDelay() = runBlocking {
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomorrowCal.time)

        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = tomorrowStr,
                subject = "Biology",
                title = "Cell Division",
                type = TaskType.LECTURE,
                targetMinutes = 45,
                isCompleted = false,
                reminderHour = 9,
                reminderMinute = 0,
                startTime = "09:00 AM"
            )
        )

        val occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, tomorrowStr, 9, 0)

        // Call rings
        FutureSelfCallEngine.canInitiateCall(context, occurrenceId, taskId)

        // User snoozes for 15 minutes
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        val initialAlarmCount = shadowAlarmManager.scheduledAlarms.size

        FutureSelfCallEngine.snoozeCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            snoozeMinutes = 15,
            taskTitle = "Cell Division",
            taskSubject = "Biology"
        )

        // 1. Original occurrence is SNOOZED
        val origRecord = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertEquals(FutureSelfCallState.SNOOZED, origRecord?.state)

        // 2. Exactly ONE new scheduled occurrence created for the task
        val allRecords = database.futureSelfCallDao().getRecordsForTask(taskId)
        assertEquals("Total call records must be 2 (1 SNOOZED + 1 SCHEDULED)", 2, allRecords.size)

        val scheduledRecords = allRecords.filter { it.state == FutureSelfCallState.SCHEDULED }
        assertEquals("Exactly ONE call record must be in SCHEDULED state", 1, scheduledRecords.size)

        // 3. Exactly ONE new alarm added into AlarmManager
        val newAlarmCount = shadowAlarmManager.scheduledAlarms.size
        assertEquals("Exactly ONE new alarm must be registered in AlarmManager", initialAlarmCount + 1, newAlarmCount)

        // 4. Old occurrence cannot ring again
        val canOldRing = FutureSelfCallEngine.canInitiateCall(context, occurrenceId, taskId)
        assertFalse("Old snoozed occurrence cannot ring again", canOldRing)
    }

    /**
     * TEST 5: Force-stop and reopen the app: answered calls do not ring again.
     */
    @Test
    fun testForceStopAndReopenApp_answeredCallsDoNotRingAgain() = runBlocking {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = todayStr,
                subject = "English",
                title = "Grammar & Reading",
                type = TaskType.REVISION,
                targetMinutes = 45,
                isCompleted = false,
                reminderHour = 10,
                reminderMinute = 0,
                startTime = "10:00 AM"
            )
        )

        val occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, todayStr, 10, 0)

        // Call rang and was answered
        FutureSelfCallEngine.canInitiateCall(context, occurrenceId, taskId)
        FutureSelfCallEngine.markAnswered(context, occurrenceId, taskId)

        val recordBeforeRestart = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertEquals(FutureSelfCallState.ANSWERED, recordBeforeRestart?.state)

        // Simulate app restart / device reboot: BootReceiver / Application.onCreate runs reschedule
        val bootReceiver = BootReceiver()
        bootReceiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
        AlarmScheduler.rescheduleUpcomingTaskAlarms(context)

        // The answered occurrence must remain ANSWERED and never be re-scheduled
        val recordAfterRestart = database.futureSelfCallDao().getRecordByOccurrenceId(occurrenceId)
        assertEquals(FutureSelfCallState.ANSWERED, recordAfterRestart?.state)

        val canProceedAfterRestart = FutureSelfCallEngine.canInitiateCall(context, occurrenceId, taskId)
        assertFalse("Answered call must not ring after force-stop and app restart", canProceedAfterRestart)
    }

    /**
     * TEST 6: Trigger duplicate receiver events: only ONE call instance is created.
     */
    @Test
    fun testTriggerDuplicateReceiverEvents_onlyOneCallInstanceCreated() = runBlocking {
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomorrowCal.time)

        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = tomorrowStr,
                subject = "Physics",
                title = "Wave Optics",
                type = TaskType.LECTURE,
                targetMinutes = 60,
                isCompleted = false,
                reminderHour = 11,
                reminderMinute = 0,
                startTime = "11:00 AM"
            )
        )

        val occurrenceId = FutureSelfCallEngine.generateOccurrenceId(taskId, tomorrowStr, 11, 0)

        // Duplicate receiver event 1
        val event1Result = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "11:00 AM"
        )

        // Duplicate receiver event 2 (arrives immediately with same occurrenceId)
        val event2Result = FutureSelfCallEngine.canInitiateCall(
            context = context,
            occurrenceId = occurrenceId,
            taskId = taskId,
            scheduledDate = tomorrowStr,
            scheduledTime = "11:00 AM"
        )

        assertTrue("First event must succeed", event1Result)
        assertFalse("Duplicate second event must be rejected", event2Result)

        val records = database.futureSelfCallDao().getRecordsForTask(taskId)
        assertEquals("Only exactly ONE call record instance must exist", 1, records.size)
        assertEquals(FutureSelfCallState.RINGING, records[0].state)
    }

    /**
     * TEST 7: Overdue tasks do not trigger repeated alarms or alarm loops.
     */
    @Test
    fun testOverdueTasksDoNotTriggerRepeatedAlarms() = runBlocking {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

        // Insert a task scheduled for 01:00 AM (in the past today)
        val taskId = database.dailyPlanDao().insertTask(
            DailyPlanTaskEntity(
                date = todayStr,
                subject = "History",
                title = "Ancient Civilizations",
                type = TaskType.LECTURE,
                targetMinutes = 60,
                isCompleted = false,
                reminderHour = 1,
                reminderMinute = 0,
                startTime = "01:00 AM"
            )
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        shadowAlarmManager.scheduledAlarms.clear()

        // Run scheduler
        AlarmScheduler.rescheduleUpcomingTaskAlarms(context)

        // Overdue task from 01:00 AM must NOT be scheduled in AlarmManager
        assertEquals("Past overdue tasks must NOT be scheduled in AlarmManager", 0, shadowAlarmManager.scheduledAlarms.size)
    }
}
