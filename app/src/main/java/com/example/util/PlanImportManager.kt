package com.example.util

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.notification.AlarmScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PlanImportManager(
    private val context: Context,
    private val database: AppDatabase
) {

    /**
     * Parses and validates the entire JSON plan file.
     * Implements Auto-Compatibility Mode, graceful defaults for optional fields,
     * and full-file validation without early termination.
     */
    suspend fun validateAndParsePlan(uri: Uri): PlanValidationResult = withContext(Dispatchers.IO) {
        val jsonString: String
        try {
            jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: return@withContext PlanValidationResult(
                isValid = false,
                plan = null,
                errors = listOf(
                    PlanValidationError(
                        itemType = "File",
                        index = 0,
                        field = "File Stream",
                        summaryBullet = "Could not open selected file",
                        friendlyMessage = "Unable to read the selected file. Please make sure the file exists and is accessible.",
                        technicalDetails = "Context contentResolver.openInputStream returned null for URI: $uri"
                    )
                ),
                technicalSummary = "I/O Error opening URI: $uri"
            )
        } catch (e: Exception) {
            return@withContext PlanValidationResult(
                isValid = false,
                plan = null,
                errors = listOf(
                    PlanValidationError(
                        itemType = "File",
                        index = 0,
                        field = "Read Error",
                        summaryBullet = "Failed to read file",
                        friendlyMessage = "An error occurred while reading the file: ${e.localizedMessage}",
                        technicalDetails = "Exception reading file: ${e.stackTraceToString()}"
                    )
                ),
                technicalSummary = e.stackTraceToString()
            )
        }

        if (jsonString.isBlank()) {
            return@withContext PlanValidationResult(
                isValid = false,
                plan = null,
                errors = listOf(
                    PlanValidationError(
                        itemType = "File",
                        index = 0,
                        field = "Empty File",
                        summaryBullet = "File is empty",
                        friendlyMessage = "The selected file is empty. Please select a valid plan JSON file.",
                        technicalDetails = "Empty string read from $uri"
                    )
                ),
                technicalSummary = "Input file is empty."
            )
        }

        val root: JSONObject
        try {
            root = JSONObject(jsonString)
        } catch (e: JSONException) {
            return@withContext PlanValidationResult(
                isValid = false,
                plan = null,
                errors = listOf(
                    PlanValidationError(
                        itemType = "JSON Syntax",
                        index = 0,
                        field = "Syntax",
                        summaryBullet = "Invalid JSON format",
                        friendlyMessage = "The file is not a valid JSON document.\nPlease check for syntax errors such as missing quotes, commas, or braces.",
                        technicalDetails = "JSONException at parse time: ${e.message}"
                    )
                ),
                technicalSummary = "JSONException: ${e.message}\n${e.stackTraceToString()}"
            )
        }

        // Collect all errors across the entire document
        val errors = mutableListOf<PlanValidationError>()

        // 1. Plan Name (Auto-compatibility: plan_name, plan_title, name, title)
        // Graceful default if missing
        val planName = root.findString("plan_name", "plan_title", "title", "name") ?: "Imported Plan"
        val version = root.findInt("version", default = 1)
        val examDate = root.findString("exam_date", "board_exam_date", "examDate", "target_date")

        // 2. Goals Validation & Extraction (Strict required: Goal.title)
        val goals = mutableListOf<PlanGoal>()
        val goalsArray = root.findArray("goals", "goal_list")
        if (goalsArray != null) {
            for (i in 0 until goalsArray.length()) {
                val gObj = goalsArray.optJSONObject(i)
                val itemNumber = i + 1
                if (gObj == null) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Goal",
                            index = itemNumber,
                            field = "Object",
                            summaryBullet = "Goal #$itemNumber is not a valid object",
                            friendlyMessage = "Goal #$itemNumber is corrupted or not a valid JSON object.",
                            technicalDetails = "Element at $.goals[$i] is not a JSONObject"
                        )
                    )
                    continue
                }

                // Auto-compatibility: title, goal_name, name, goal_title
                val title = gObj.findString("title", "goal_name", "name", "goal_title")
                if (title.isNullOrBlank()) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Goal",
                            index = itemNumber,
                            field = "Title",
                            summaryBullet = "Goal #$itemNumber is missing Title",
                            friendlyMessage = "Goal #$itemNumber is missing a Title.\nPlease provide a title for this goal and try again.",
                            technicalDetails = "Required value 'title' (checked aliases: title, goal_name, name, goal_title) missing or empty at $.goals[$i]"
                        )
                    )
                } else {
                    // Graceful defaults for optional fields
                    val description = gObj.findString("description", "details", "desc") ?: ""
                    val category = gObj.findString("category", "type") ?: "ACADEMIC"
                    val targetDate = gObj.findString("target_date", "targetDate", "due_date", "date")
                    goals.add(
                        PlanGoal(
                            title = title,
                            description = description,
                            category = category,
                            targetDate = targetDate
                        )
                    )
                }
            }
        }

        // 3. Tasks Validation & Extraction (Strict required: Task.title)
        val tasks = mutableListOf<PlanTask>()
        val tasksArray = root.findArray("tasks", "task_list", "daily_tasks")
        if (tasksArray != null) {
            for (i in 0 until tasksArray.length()) {
                val tObj = tasksArray.optJSONObject(i)
                val itemNumber = i + 1
                if (tObj == null) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Task",
                            index = itemNumber,
                            field = "Object",
                            summaryBullet = "Task #$itemNumber is not a valid object",
                            friendlyMessage = "Task #$itemNumber is corrupted or not a valid JSON object.",
                            technicalDetails = "Element at $.tasks[$i] is not a JSONObject"
                        )
                    )
                    continue
                }

                // Auto-compatibility: title, task_name, name, task_title
                val title = tObj.findString("title", "task_name", "name", "task_title")
                if (title.isNullOrBlank()) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Task",
                            index = itemNumber,
                            field = "Title",
                            summaryBullet = "Task #$itemNumber is missing Title",
                            friendlyMessage = "Task #$itemNumber is missing a Title.\nPlease provide a title for this task and try again.",
                            technicalDetails = "Required value 'title' (checked aliases: title, task_name, name, task_title) missing or empty at $.tasks[$i]"
                        )
                    )
                } else {
                    // Graceful defaults for optional fields
                    val subject = tObj.findString("subject", "category") ?: "General"
                    val type = tObj.findString("type", "task_type") ?: "LECTURE"
                    val details = tObj.findString("details", "description", "desc", "notes") ?: ""
                    val targetMinutes = tObj.findInt("target_minutes", "targetMinutes", "duration", "minutes", default = 45)
                    val date = tObj.findString("date", "target_date")
                    val xp = tObj.findInt("xp", "task_xp", default = 0)
                    val rawTime = tObj.findString("time", "start_time", "schedule_time", "slot_time", "timing")
                    var startTime = tObj.findString("start_time", "startTime", "from", "start") ?: rawTime
                    var endTime = tObj.findString("end_time", "endTime", "to", "end")
                    if (!rawTime.isNullOrBlank() && (rawTime.contains(" - ") || rawTime.contains(" – "))) {
                        val delim = if (rawTime.contains(" – ")) " – " else " - "
                        val parts = rawTime.split(delim)
                        if (parts.size >= 2) {
                            startTime = parts[0].trim()
                            if (endTime.isNullOrBlank()) {
                                endTime = parts[1].trim()
                            }
                        }
                    }

                    tasks.add(
                        PlanTask(
                            title = title,
                            subject = subject,
                            type = type,
                            details = details,
                            targetMinutes = targetMinutes,
                            date = date,
                            xp = xp,
                            time = rawTime,
                            startTime = startTime,
                            endTime = endTime
                        )
                    )
                }
            }
        }

        // 3.5 Schedule / Timetable Validation & Extraction
        val schedule = mutableListOf<PlanScheduleItem>()
        val scheduleArray = root.findArray("schedule", "timetable", "time_table", "daily_schedule", "routine", "slots")
        if (scheduleArray != null) {
            for (i in 0 until scheduleArray.length()) {
                val sObj = scheduleArray.optJSONObject(i) ?: continue
                val rawTime = sObj.findString("time", "slot_time", "start_time", "timing") ?: "09:00"
                var startTime = sObj.findString("start_time", "startTime", "from", "start") ?: rawTime
                var endTime = sObj.findString("end_time", "endTime", "to", "end")
                if (rawTime.contains(" - ") || rawTime.contains(" – ")) {
                    val delim = if (rawTime.contains(" – ")) " – " else " - "
                    val parts = rawTime.split(delim)
                    if (parts.size >= 2) {
                        startTime = parts[0].trim()
                        if (endTime.isNullOrBlank()) {
                            endTime = parts[1].trim()
                        }
                    }
                }
                val title = sObj.findString("title", "name", "activity", "task") ?: "Study Block #${i + 1}"
                val category = sObj.findString("category", "subject", "type") ?: "Study"
                val targetMinutes = sObj.findInt("target_minutes", "duration", "minutes", default = 45)
                val type = sObj.findString("type", "task_type") ?: "LECTURE"
                val details = sObj.findString("details", "description", "desc", "notes") ?: ""
                schedule.add(
                    PlanScheduleItem(
                        time = rawTime,
                        startTime = startTime,
                        endTime = endTime,
                        title = title,
                        category = category,
                        targetMinutes = targetMinutes,
                        type = type,
                        details = details
                    )
                )
            }
        }

        // 4. Habits Validation & Extraction (Strict required: Habit.name)
        val habits = mutableListOf<PlanHabit>()
        val habitsArray = root.findArray("habits", "habit_list")
        if (habitsArray != null) {
            for (i in 0 until habitsArray.length()) {
                val hObj = habitsArray.optJSONObject(i)
                val itemNumber = i + 1
                if (hObj == null) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Habit",
                            index = itemNumber,
                            field = "Object",
                            summaryBullet = "Habit #$itemNumber is not a valid object",
                            friendlyMessage = "Habit #$itemNumber is corrupted or not a valid JSON object.",
                            technicalDetails = "Element at $.habits[$i] is not a JSONObject"
                        )
                    )
                    continue
                }

                // Auto-compatibility: name, title, habit_title, habit_name
                val name = hObj.findString("name", "title", "habit_title", "habit_name")
                if (name.isNullOrBlank()) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Habit",
                            index = itemNumber,
                            field = "Name",
                            summaryBullet = "Habit #$itemNumber is missing Name",
                            friendlyMessage = "Habit #$itemNumber is missing a Name.\nPlease provide a name for this habit and try again.",
                            technicalDetails = "Required value 'name' (checked aliases: name, title, habit_title, habit_name) missing or empty at $.habits[$i]"
                        )
                    )
                } else {
                    // Graceful defaults for optional fields
                    val habitType = hObj.findString("habit_type", "habitType", "type") ?: "CUSTOM"
                    val isNegative = hObj.findBoolean("is_negative", "isNegative", "negative", default = false)
                    val targetUnit = hObj.findString("target_unit", "targetUnit", "unit") ?: "Completed"
                    val targetNumeric = hObj.findInt("target_numeric", "targetNumeric", "target_count", "target", default = 1)
                    habits.add(
                        PlanHabit(
                            name = name,
                            habitType = habitType,
                            isNegative = isNegative,
                            targetUnit = targetUnit,
                            targetNumeric = targetNumeric
                        )
                    )
                }
            }
        }

        // 5. Alarms Validation & Extraction (Strict required: Alarm.time)
        val alarms = mutableListOf<PlanAlarm>()
        val alarmsArray = root.findArray("alarms", "alarm_list", "reminders")
        if (alarmsArray != null) {
            for (i in 0 until alarmsArray.length()) {
                val aObj = alarmsArray.optJSONObject(i)
                val itemNumber = i + 1
                if (aObj == null) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Alarm",
                            index = itemNumber,
                            field = "Object",
                            summaryBullet = "Alarm #$itemNumber is not a valid object",
                            friendlyMessage = "Alarm #$itemNumber is corrupted or not a valid JSON object.",
                            technicalDetails = "Element at $.alarms[$i] is not a JSONObject"
                        )
                    )
                    continue
                }

                // Auto-compatibility: time, alarm_time, schedule_time, time_str
                val rawTime = aObj.findString("time", "alarm_time", "schedule_time", "time_str")
                if (rawTime.isNullOrBlank()) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Alarm",
                            index = itemNumber,
                            field = "Time",
                            summaryBullet = "Alarm #$itemNumber is missing Time",
                            friendlyMessage = "Alarm #$itemNumber is missing a Time.\nPlease provide a valid 24-hour time (HH:mm) for this alarm.",
                            technicalDetails = "Required value 'time' (checked aliases: time, alarm_time, schedule_time, time_str) missing or empty at $.alarms[$i]"
                        )
                    )
                } else {
                    // Validate time format
                    val parts = rawTime.trim().split(":")
                    val hour = if (parts.size == 2) parts[0].trim().toIntOrNull() else null
                    val minute = if (parts.size == 2) parts[1].trim().toIntOrNull() else null

                    if (parts.size != 2 || hour == null || minute == null) {
                        errors.add(
                            PlanValidationError(
                                itemType = "Alarm",
                                index = itemNumber,
                                field = "Time",
                                summaryBullet = "Alarm #$itemNumber has invalid time format",
                                friendlyMessage = "Alarm #$itemNumber has invalid time format '$rawTime'.\nPlease use 24-hour format such as '07:30' or '22:00'.",
                                technicalDetails = "Invalid time format '$rawTime' at $.alarms[$i].time (expected HH:mm format)"
                            )
                        )
                    } else if (hour !in 0..23 || minute !in 0..59) {
                        errors.add(
                            PlanValidationError(
                                itemType = "Alarm",
                                index = itemNumber,
                                field = "Time",
                                summaryBullet = "Alarm #$itemNumber has invalid time format",
                                friendlyMessage = "Alarm #$itemNumber has invalid time '$rawTime'. Hours must be 0–23 and minutes 0–59.",
                                technicalDetails = "Out of range time '$rawTime' (hour=$hour, minute=$minute) at $.alarms[$i].time"
                            )
                        )
                    } else {
                        val formattedTime = String.format("%02d:%02d", hour, minute)
                        val title = aObj.findString("title", "name", "alarm_title", "label") ?: "Alarm #$itemNumber"
                        val challengeType = aObj.findString("challenge_type", "challengeType", "challenge") ?: "MATH"
                        val difficulty = aObj.findString("difficulty", "challenge_difficulty") ?: "MEDIUM"
                        alarms.add(
                            PlanAlarm(
                                title = title,
                                time = formattedTime,
                                challengeType = challengeType,
                                difficulty = difficulty
                            )
                        )
                    }
                }
            }
        }

        // 6. Optional Focus Sessions
        val focusSessions = mutableListOf<PlanFocusSession>()
        val focusArray = root.findArray("focus_sessions", "focusSessions")
        if (focusArray != null) {
            for (i in 0 until focusArray.length()) {
                val fObj = focusArray.optJSONObject(i) ?: continue
                val title = fObj.findString("title", "name") ?: "Focus Session #${i + 1}"
                val duration = fObj.findInt("duration", "minutes", default = 25)
                focusSessions.add(PlanFocusSession(title = title, duration = duration))
            }
        }

        // 7. Optional Milestones
        val milestones = mutableListOf<PlanMilestone>()
        val milestonesArray = root.findArray("milestones", "milestone_list")
        if (milestonesArray != null) {
            for (i in 0 until milestonesArray.length()) {
                val mObj = milestonesArray.optJSONObject(i) ?: continue
                val title = mObj.findString("title", "name") ?: "Milestone #${i + 1}"
                val date = mObj.findString("date", "target_date")
                milestones.add(PlanMilestone(title = title, date = date))
            }
        }

        // 8. Optional XP Rules
        val xpRulesObj = root.optJSONObject("xp_rules") ?: root.optJSONObject("xpRules")
        val xpRules = if (xpRulesObj != null) {
            PlanXpRules(
                taskXp = xpRulesObj.findInt("task_xp", "taskXp", default = 50),
                habitXp = xpRulesObj.findInt("habit_xp", "habitXp", default = 10),
                focusXp = xpRulesObj.findInt("focus_xp", "focusXp", default = 20)
            )
        } else null

        if (errors.isNotEmpty()) {
            val techSummary = buildTechnicalSummary(errors)
            PlanValidationResult(
                isValid = false,
                plan = null,
                errors = errors,
                technicalSummary = techSummary
            )
        } else {
            val validatedPlan = PlanImport(
                planName = planName,
                version = version,
                examDate = examDate,
                schedule = schedule,
                goals = goals,
                tasks = tasks,
                habits = habits,
                alarms = alarms,
                focusSessions = focusSessions,
                milestones = milestones,
                xpRules = xpRules
            )
            PlanValidationResult(
                isValid = true,
                plan = validatedPlan,
                errors = emptyList(),
                technicalSummary = "Validation passed successfully."
            )
        }
    }

    /**
     * Backward-compatible parsePlan helper.
     */
    suspend fun parsePlan(uri: Uri): Result<PlanImport> = withContext(Dispatchers.IO) {
        val validation = validateAndParsePlan(uri)
        if (validation.isValid && validation.plan != null) {
            Result.success(validation.plan)
        } else {
            val firstErr = validation.errors.firstOrNull()?.friendlyMessage ?: "Failed to validate plan"
            Result.failure(Exception(firstErr))
        }
    }

    /**
     * Commits the validated plan into the database.
     * Returns an ImportReport with the exact counts of items applied.
     */
    suspend fun importPlan(plan: PlanImport, replaceMode: Boolean): ImportReport = withContext(Dispatchers.IO) {
        createBackup()

        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var goalsImported = 0
        var tasksImported = 0
        var scheduleImported = 0
        var habitsImported = 0
        var alarmsImported = 0

        database.withTransaction {
            if (replaceMode) {
                database.goalDao().clearAll()
                database.dailyPlanDao().clearAll()
                database.habitDao().clearAll()
                database.alarmDao().clearAllAlarms()
                database.winterArcObjectivesDao().clearAllArcGoals()
            }

            // Update Board Exam date if present
            plan.examDate?.let { eDate ->
                if (eDate.isNotBlank()) {
                    val current = database.boardExamDao().getBoardExamConfigDirect()
                    if (current != null) {
                        database.boardExamDao().insertOrUpdate(current.copy(examDate = eDate))
                    } else {
                        database.boardExamDao().insertOrUpdate(BoardExamConfigEntity(examDate = eDate))
                    }
                }
            }

            // Import Goals
            plan.goals?.forEach { g ->
                val category = runCatching { GoalCategory.valueOf(g.category?.uppercase() ?: "ACADEMIC") }
                    .getOrDefault(GoalCategory.ACADEMIC)
                database.goalDao().insertGoal(
                    GoalEntity(
                        title = g.title,
                        description = g.description ?: "",
                        category = category,
                        targetDate = g.targetDate
                    )
                )
                goalsImported++
            }

            // Import Tasks
            plan.tasks?.forEachIndexed { index, t ->
                val type = when (t.type?.uppercase()) {
                    "LECTURE" -> TaskType.LECTURE
                    "NOTES" -> TaskType.NOTES
                    "REVISION" -> TaskType.REVISION
                    "PYQ" -> TaskType.PYQ
                    "WORKOUT" -> TaskType.WORKOUT
                    else -> TaskType.LECTURE
                }

                val taskDate = if (t.date.isNullOrBlank() || 
                    t.date.equals("today", ignoreCase = true) || 
                    t.date.equals("current", ignoreCase = true) || 
                    t.date < todayDateStr
                ) {
                    todayDateStr
                } else {
                    t.date
                }

                val rawTime = t.startTime ?: t.time
                var finalStartTime = rawTime
                var finalEndTime = t.endTime

                if (!rawTime.isNullOrBlank() && (rawTime.contains(" - ") || rawTime.contains(" – "))) {
                    val delim = if (rawTime.contains(" – ")) " – " else " - "
                    val parts = rawTime.split(delim)
                    if (parts.size >= 2) {
                        finalStartTime = parts[0].trim()
                        if (finalEndTime.isNullOrBlank()) {
                            finalEndTime = parts[1].trim()
                        }
                    }
                }

                val s12 = if (!finalStartTime.isNullOrBlank()) DateTimeUtils.formatTo12Hour(finalStartTime) else null
                val e12 = if (!finalEndTime.isNullOrBlank()) {
                    DateTimeUtils.formatTo12Hour(finalEndTime)
                } else if (!finalStartTime.isNullOrBlank()) {
                    DateTimeUtils.calculateEndTime(finalStartTime, t.targetMinutes ?: 45)
                } else null

                val parsedTime = if (!finalStartTime.isNullOrBlank()) DateTimeUtils.parseHourMinute(finalStartTime) else null

                val insertedTaskId = database.dailyPlanDao().insertTask(
                    DailyPlanTaskEntity(
                        date = taskDate,
                        subject = t.subject ?: "General",
                        title = t.title,
                        type = type,
                        details = t.details ?: "",
                        targetMinutes = t.targetMinutes ?: 45,
                        isCompleted = false,
                        orderIndex = tasksImported,
                        xpReward = if (t.xp != null && t.xp > 0) t.xp else 50,
                        reminderHour = parsedTime?.first,
                        reminderMinute = parsedTime?.second,
                        startTime = s12,
                        endTime = e12
                    )
                )

                if (parsedTime != null) {
                    AlarmScheduler.scheduleTaskAlarm(
                        context = context,
                        taskId = insertedTaskId,
                        hour = parsedTime.first,
                        minute = parsedTime.second,
                        title = t.title,
                        subject = t.subject ?: "General"
                    )
                }

                tasksImported++
            }

            // Import Schedule Timeline Items as today's tasks
            plan.schedule?.forEachIndexed { index, s ->
                val type = when (s.type?.uppercase()) {
                    "WORKOUT" -> TaskType.WORKOUT
                    "REVISION" -> TaskType.REVISION
                    "PYQ" -> TaskType.PYQ
                    "NOTES" -> TaskType.NOTES
                    "LECTURE" -> TaskType.LECTURE
                    else -> {
                        if (s.category?.contains("workout", ignoreCase = true) == true || s.category?.contains("run", ignoreCase = true) == true) TaskType.WORKOUT
                        else if (s.category?.contains("revision", ignoreCase = true) == true) TaskType.REVISION
                        else TaskType.LECTURE
                    }
                }

                val rawTime = s.startTime ?: s.time
                var finalStartTime = rawTime
                var finalEndTime = s.endTime

                if (!rawTime.isNullOrBlank() && (rawTime.contains(" - ") || rawTime.contains(" – "))) {
                    val delim = if (rawTime.contains(" – ")) " – " else " - "
                    val parts = rawTime.split(delim)
                    if (parts.size >= 2) {
                        finalStartTime = parts[0].trim()
                        if (finalEndTime.isNullOrBlank()) {
                            finalEndTime = parts[1].trim()
                        }
                    }
                }

                val s12 = if (!finalStartTime.isNullOrBlank()) DateTimeUtils.formatTo12Hour(finalStartTime) else null
                val e12 = if (!finalEndTime.isNullOrBlank()) {
                    DateTimeUtils.formatTo12Hour(finalEndTime)
                } else if (!finalStartTime.isNullOrBlank()) {
                    DateTimeUtils.calculateEndTime(finalStartTime, s.targetMinutes ?: 45)
                } else null

                val parsedTime = if (!finalStartTime.isNullOrBlank()) DateTimeUtils.parseHourMinute(finalStartTime) else null

                val insertedScheduleId = database.dailyPlanDao().insertTask(
                    DailyPlanTaskEntity(
                        date = todayDateStr,
                        subject = s.category ?: "Schedule",
                        title = s.title,
                        type = type,
                        details = s.details ?: (if (s12 != null && e12 != null) "Scheduled $s12 – $e12" else ""),
                        targetMinutes = s.targetMinutes ?: 45,
                        isCompleted = false,
                        orderIndex = tasksImported + scheduleImported,
                        xpReward = 50,
                        reminderHour = parsedTime?.first,
                        reminderMinute = parsedTime?.second,
                        startTime = s12,
                        endTime = e12
                    )
                )

                if (parsedTime != null) {
                    AlarmScheduler.scheduleTaskAlarm(
                        context = context,
                        taskId = insertedScheduleId,
                        hour = parsedTime.first,
                        minute = parsedTime.second,
                        title = s.title,
                        subject = s.category ?: "Schedule"
                    )
                }

                scheduleImported++
            }

            // Import Focus Sessions
            plan.focusSessions?.forEachIndexed { index, f ->
                database.dailyPlanDao().insertTask(
                    DailyPlanTaskEntity(
                        date = todayDateStr,
                        subject = "Focus Session",
                        title = f.title,
                        type = TaskType.REVISION,
                        details = "Duration: ${f.duration ?: 25} minutes",
                        targetMinutes = f.duration ?: 25,
                        orderIndex = tasksImported + scheduleImported + index,
                        xpReward = 40
                    )
                )
                tasksImported++
            }

            // Import Habits
            plan.habits?.forEach { h ->
                val habitType = when (h.habitType?.uppercase()?.replace(" ", "_")?.replace("-", "_")) {
                    "SLEEP", "WAKE", "WAKE_UP", "EARLY_WAKE" -> HabitType.SLEEP
                    "WORKOUT", "FITNESS", "EXERCISE", "RUNNING", "GYM" -> HabitType.WORKOUT
                    "DEEP_STUDY", "STUDY", "DEEPWORK", "8H_STUDY" -> HabitType.DEEP_STUDY
                    "READING", "BOOK" -> HabitType.READING
                    "NO_PORN", "NOFAP" -> HabitType.NO_PORN
                    "NO_REELS", "NO_DOOMSCROLL", "SCREEN_TIME" -> HabitType.NO_REELS
                    "MEDITATION", "MINDFULNESS" -> HabitType.MEDITATION
                    "HYDRATION", "WATER" -> HabitType.HYDRATION
                    else -> runCatching { HabitType.valueOf(h.habitType ?: "CUSTOM") }.getOrDefault(HabitType.CUSTOM)
                }
                database.habitDao().insertHabit(
                    HabitEntity(
                        name = h.name,
                        habitType = habitType,
                        isNegativeHabit = h.isNegative ?: false,
                        targetUnit = h.targetUnit ?: "Completed",
                        targetNumeric = h.targetNumeric ?: 1,
                        isDefault = false,
                        isArchived = false
                    )
                )
                habitsImported++
            }

            // Import Milestones
            plan.milestones?.forEach { m ->
                database.winterArcObjectivesDao().insertArcGoal(
                    ArcGoalPlanItemEntity(
                        timeHorizon = "MONTHLY",
                        title = m.title,
                        description = "Imported Milestone",
                        targetDateOrPeriod = m.date ?: "",
                        xpReward = 100,
                        priority = "HIGH"
                    )
                )
            }

            // Import Alarms
            val importedAlarms = plan.alarms ?: emptyList()
            if (importedAlarms.isNotEmpty()) {
                importedAlarms.forEach { a ->
                    val parsedTime = parseTimeHourMinute(a.time) ?: Pair(6, 0)
                    val challengeType = when (a.challengeType?.uppercase()) {
                        "MATH" -> AlarmChallengeType.MATH
                        "SHAKE", "PHYSICAL_SHAKE" -> AlarmChallengeType.PHYSICAL_SHAKE
                        "CAPTCHA", "TYPING" -> AlarmChallengeType.CAPTCHA
                        "WALK", "STEPS", "PHYSICAL_STEPS" -> AlarmChallengeType.PHYSICAL_STEPS
                        else -> AlarmChallengeType.MATH
                    }

                    val difficulty = when (a.difficulty?.uppercase()) {
                        "EASY" -> AlarmDifficulty.EASY
                        "MEDIUM" -> AlarmDifficulty.MEDIUM
                        "HARD", "EXTREME" -> AlarmDifficulty.HARD
                        else -> AlarmDifficulty.MEDIUM
                    }

                    val id = database.alarmDao().insertAlarm(
                        AlarmEntity(
                            title = a.title,
                            hour = parsedTime.first,
                            minute = parsedTime.second,
                            isEnabled = true,
                            challengeType = challengeType,
                            challengeDifficulty = difficulty
                        )
                    )
                    val entity = database.alarmDao().getAlarmById(id)
                    if (entity != null) {
                        AlarmScheduler.scheduleCustomAlarm(context, entity)
                    }
                    alarmsImported++
                }
            }
        }

        // Save XP Rules if present
        plan.xpRules?.let { rules ->
            val prefs = context.getSharedPreferences("xp_rules_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("task_xp", rules.taskXp ?: 50)
                .putInt("habit_xp", rules.habitXp ?: 10)
                .putInt("focus_xp", rules.focusXp ?: 20)
                .apply()
        }

        ImportReport(
            planName = plan.planName ?: "Imported Plan",
            goalsCount = goalsImported,
            tasksCount = tasksImported,
            scheduleCount = scheduleImported,
            habitsCount = habitsImported,
            alarmsCount = alarmsImported
        )
    }

    private suspend fun createBackup() = withContext(Dispatchers.IO) {
        try {
            val goals = database.goalDao().getAllGoals().first()
            val alarms = database.alarmDao().getAllAlarmsDirect()
            val backupPlan = JSONObject().apply {
                put("plan_name", "Auto-Backup")
                put("version", 1)
                val gArr = JSONArray()
                goals.forEach { g ->
                    gArr.put(JSONObject().apply {
                        put("title", g.title)
                        put("description", g.description)
                        put("category", g.category.name)
                        put("target_date", g.targetDate)
                    })
                }
                put("goals", gArr)
                val aArr = JSONArray()
                alarms.forEach { a ->
                    aArr.put(JSONObject().apply {
                        put("title", a.title)
                        put("time", String.format("%02d:%02d", a.hour, a.minute))
                        put("challenge_type", a.challengeType.name)
                        put("difficulty", a.challengeDifficulty.name)
                    })
                }
                put("alarms", aArr)
            }
            val file = java.io.File(context.filesDir, "plan_backup.json")
            file.writeText(backupPlan.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun restoreBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(context.filesDir, "plan_backup.json")
            if (!file.exists()) throw Exception("No backup found")
            val json = file.readText()
            val root = JSONObject(json)
            val goalsArray = root.optJSONArray("goals")
            val alarmsArray = root.optJSONArray("alarms")

            database.withTransaction {
                database.goalDao().clearAll()
                database.dailyPlanDao().clearAll()
                database.habitDao().clearAll()
                database.alarmDao().clearAllAlarms()
                database.winterArcObjectivesDao().clearAllArcGoals()

                if (goalsArray != null) {
                    for (i in 0 until goalsArray.length()) {
                        val g = goalsArray.optJSONObject(i) ?: continue
                        val title = g.optString("title", "")
                        val categoryStr = g.optString("category", "ACADEMIC")
                        val cat = runCatching { GoalCategory.valueOf(categoryStr) }.getOrDefault(GoalCategory.ACADEMIC)
                        database.goalDao().insertGoal(
                            GoalEntity(
                                title = title,
                                description = g.optString("description", ""),
                                category = cat,
                                targetDate = if (g.has("target_date") && !g.isNull("target_date")) g.optString("target_date") else null
                            )
                        )
                    }
                }

                if (alarmsArray != null) {
                    for (i in 0 until alarmsArray.length()) {
                        val a = alarmsArray.optJSONObject(i) ?: continue
                        val title = a.optString("title", "Alarm #${i + 1}")
                        val timeStr = a.optString("time", "07:00")
                        val parts = timeStr.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val c = runCatching { AlarmChallengeType.valueOf(a.optString("challenge_type", "MATH")) }.getOrDefault(AlarmChallengeType.MATH)
                        val d = runCatching { AlarmDifficulty.valueOf(a.optString("difficulty", "MEDIUM")) }.getOrDefault(AlarmDifficulty.MEDIUM)
                        val id = database.alarmDao().insertAlarm(
                            AlarmEntity(title = title, hour = h, minute = m, challengeType = c, challengeDifficulty = d)
                        )
                        val entity = database.alarmDao().getAlarmById(id)
                        if (entity != null) {
                            AlarmScheduler.scheduleCustomAlarm(context, entity)
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildTechnicalSummary(errors: List<PlanValidationError>): String {
        val sb = StringBuilder()
        sb.append("JSON Plan Validation: ${errors.size} error(s) encountered:\n")
        errors.forEachIndexed { idx, err ->
            sb.append("[${idx + 1}] ${err.technicalDetails}\n")
        }
        return sb.toString().trim()
    }

    // Helper extensions for JSON parsing & alias matching
    private fun JSONObject.findString(vararg keys: String): String? {
        for (k in keys) {
            if (has(k) && !isNull(k)) {
                val value = optString(k, "").trim()
                if (value.isNotEmpty()) return value
            }
        }
        return null
    }

    private fun JSONObject.findInt(vararg keys: String, default: Int): Int {
        for (k in keys) {
            if (has(k) && !isNull(k)) {
                val direct = optInt(k, Int.MIN_VALUE)
                if (direct != Int.MIN_VALUE) return direct
                val asStr = optString(k, "").toIntOrNull()
                if (asStr != null) return asStr
            }
        }
        return default
    }

    private fun JSONObject.findBoolean(vararg keys: String, default: Boolean): Boolean {
        for (k in keys) {
            if (has(k) && !isNull(k)) {
                return optBoolean(k, default)
            }
        }
        return default
    }

    private fun JSONObject.findArray(vararg keys: String): JSONArray? {
        for (k in keys) {
            if (has(k) && !isNull(k)) {
                val arr = optJSONArray(k)
                if (arr != null) return arr
            }
        }
        return null
    }

    private fun parseTimeHourMinute(timeStr: String?): Pair<Int, Int>? {
        if (timeStr.isNullOrBlank()) return null
        return DateTimeUtils.parseHourMinute(timeStr)
    }
}
