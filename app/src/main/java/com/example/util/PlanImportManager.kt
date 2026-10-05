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

        val trimmed = jsonString.trim()
        val root: JSONObject
        if (trimmed.startsWith("[")) {
            // Root is an array of tasks or schedule items
            try {
                val array = JSONArray(trimmed)
                root = JSONObject().apply {
                    put("plan_name", "Imported Schedule")
                    put("tasks", array)
                }
            } catch (e: Exception) {
                return@withContext PlanValidationResult(
                    isValid = false,
                    plan = null,
                    errors = listOf(
                        PlanValidationError(
                            itemType = "JSON Syntax",
                            index = 0,
                            field = "Syntax",
                            summaryBullet = "Invalid JSON array format",
                            friendlyMessage = "The file starts with an array but could not be parsed: ${e.message}",
                            technicalDetails = e.stackTraceToString()
                        )
                    ),
                    technicalSummary = e.stackTraceToString()
                )
            }
        } else {
            try {
                var parsedRoot = JSONObject(trimmed)
                val envelopeKeys = listOf("plan", "daily_plan", "rebuild_plan", "study_plan", "data", "payload", "content", "response", "result")
                for (k in envelopeKeys) {
                    if (parsedRoot.has(k) && parsedRoot.optJSONObject(k) != null) {
                        parsedRoot = parsedRoot.getJSONObject(k)
                    }
                }
                root = parsedRoot
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
        }

        // Collect all errors across the entire document
        val errors = mutableListOf<PlanValidationError>()

        // 1. Plan Name & Versioning (Support v1, v2, v3 with auto-detection)
        val planName = root.findString("plan_name", "plan_title", "title", "name") ?: "Imported Plan"
        val explicitVersion = root.findInt("version", default = 0)
        val hasMultiDay = root.has("days") || root.has("weeks") || root.has("daily_plans") || root.has("roadmap") || root.has("phases") || root.has("stages") || root.has("timeline")
        val hasAdvanced = root.has("milestones") || root.has("xp_rules") || root.has("alarms") || root.has("exam_date")
        val version = if (explicitVersion > 0) explicitVersion else if (hasAdvanced) 3 else if (hasMultiDay) 2 else 1
        val examDate = root.findString("exam_date", "board_exam_date", "examDate", "target_date")

        // 2. Goals Validation & Extraction (Strict required: Goal.title)
        val goals = mutableListOf<PlanGoal>()
        val goalsArray = root.findArray("goals", "goal_list", "objectives", "targets")
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

                // Auto-compatibility: title, goal_name, name, goal_title, objective
                val title = gObj.findString("title", "goal_name", "name", "goal_title", "objective")
                if (title.isNullOrBlank()) {
                    val keysFound = gObj.keys().asSequence().toList().joinToString()
                    errors.add(
                        PlanValidationError(
                            itemType = "Goal",
                            index = itemNumber,
                            field = "Title",
                            summaryBullet = "Goal #$itemNumber is missing Title",
                            friendlyMessage = "Goal #$itemNumber is missing a title. Found fields: [$keysFound]. Supported keys: 'title', 'name', 'goal_name'.",
                            technicalDetails = "Required value 'title' missing at $.goals[$i]. Found keys: [$keysFound]"
                        )
                    )
                } else {
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

        // 3. Tasks Validation & Extraction (Single-day + Multi-day 90-day support + Duration aliases)
        val tasks = mutableListOf<PlanTask>()
        val directTasksArray = root.findArray("tasks", "task_list", "daily_tasks", "items", "todo", "plan_tasks", "activities", "sessions", "actions")

        fun parseTaskObject(tObj: JSONObject?, itemNumber: Int, defaultDate: String? = null) {
            if (tObj == null) {
                errors.add(
                    PlanValidationError(
                        itemType = "Task",
                        index = itemNumber,
                        field = "Object",
                        summaryBullet = "Task #$itemNumber is not a valid object",
                        friendlyMessage = "Task #$itemNumber is corrupted or not a valid JSON object.",
                        technicalDetails = "Element is not a JSONObject"
                    )
                )
                return
            }

            var title = tObj.findString("title", "task_name", "taskName", "name", "task_title", "task", "activity")
            if (title.isNullOrBlank()) {
                val topicFallback = tObj.findString("topic", "chapter", "lecture", "unit", "subject_topic")
                if (topicFallback != null) {
                    title = topicFallback
                } else {
                    val keysFound = tObj.keys().asSequence().toList().joinToString()
                    errors.add(
                        PlanValidationError(
                            itemType = "Task",
                            index = itemNumber,
                            field = "Title",
                            summaryBullet = "Task #$itemNumber is missing Title",
                            friendlyMessage = "Task #$itemNumber is missing a title. Expected 'title' or 'task_name'. Found fields: [$keysFound]. Supported keys: 'title', 'task_name', 'name'.",
                            technicalDetails = "Required value 'title' missing at task #$itemNumber. Found keys: [$keysFound]"
                        )
                    )
                    return
                }
            }

            val subject = tObj.findString("subject", "category", "topic", "tag") ?: "General"
            val type = tObj.findString("type", "task_type") ?: "LECTURE"
            val details = tObj.findString("details", "description", "desc", "notes") ?: ""
            // Robust duration parsing supporting "duration_minutes", "60 mins", "1.5 hours", etc.
            val targetMinutes = tObj.findDurationMinutes(
                "target_minutes", "targetMinutes", "duration_minutes", "durationMinutes",
                "duration", "minutes", "time_minutes", default = 45
            )
            val date = tObj.findString("date", "target_date") ?: defaultDate
            val xp = tObj.findInt("xp", "task_xp", default = 0)
            val rawTime = tObj.findString("time", "start_time", "startTime", "schedule_time", "slot_time", "timing", "from", "start")
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

        // Direct tasks array at root
        if (directTasksArray != null) {
            for (i in 0 until directTasksArray.length()) {
                parseTaskObject(directTasksArray.optJSONObject(i), i + 1)
            }
        } else {
            // Direct tasks object at root (e.g. { "tasks": { "0": {...}, "1": {...} } })
            val directTasksObj = root.optJSONObject("tasks") ?: root.optJSONObject("todo") ?: root.optJSONObject("items")
            if (directTasksObj != null) {
                val keys = directTasksObj.keys().asSequence().toList()
                for (k in keys) {
                    val v = directTasksObj.opt(k)
                    if (v is JSONObject) {
                        parseTaskObject(v, tasks.size + 1)
                    } else if (v is JSONArray) {
                        for (j in 0 until v.length()) {
                            parseTaskObject(v.optJSONObject(j), tasks.size + 1)
                        }
                    }
                }
            }
        }

        // Multi-day plans as Array (days: [ { day: 1, date: "...", tasks: [...] } ])
        val daysArray = root.findArray("days", "weeks", "daily_plans", "day_list", "roadmap_days", "roadmap", "phases", "stages", "timeline", "curriculum")
        if (daysArray != null) {
            for (d in 0 until daysArray.length()) {
                val dayObj = daysArray.optJSONObject(d) ?: continue
                val dayNumber = dayObj.findInt("day", "day_number", default = d + 1)
                val explicitDate = dayObj.findString("date", "target_date", "day_date")
                val dayDate = if (!explicitDate.isNullOrBlank() && explicitDate.contains("-")) {
                    explicitDate
                } else {
                    calculateDateForDayOffset(dayNumber - 1)
                }
                val dayTasks = dayObj.findArray("tasks", "task_list", "daily_tasks", "items", "schedule", "activities", "sessions", "blocks")
                if (dayTasks != null) {
                    for (t in 0 until dayTasks.length()) {
                        parseTaskObject(dayTasks.optJSONObject(t), tasks.size + 1, defaultDate = dayDate)
                    }
                } else if (dayObj.has("title") || dayObj.has("task") || dayObj.has("task_name") || dayObj.has("name") || dayObj.has("activity")) {
                    parseTaskObject(dayObj, tasks.size + 1, defaultDate = dayDate)
                }
            }
        }

        // Multi-day plans as Object (e.g. "days": { "1": [...], "2": [...] } or { "day_1": { "tasks": [...] } })
        val daysObj = root.optJSONObject("days") ?: root.optJSONObject("daily_plans") ?: root.optJSONObject("roadmap") ?: root.optJSONObject("phases")
        if (daysObj != null) {
            val sortedKeys = daysObj.keys().asSequence().toList().sortedWith(Comparator { a, b ->
                val numA = Regex("""\d+""").find(a)?.value?.toIntOrNull() ?: 0
                val numB = Regex("""\d+""").find(b)?.value?.toIntOrNull() ?: 0
                if (numA != numB) numA.compareTo(numB) else a.compareTo(b)
            })
            for ((dIdx, key) in sortedKeys.withIndex()) {
                val numFromKey = Regex("""\d+""").find(key)?.value?.toIntOrNull() ?: (dIdx + 1)
                val dayDate = calculateDateForDayOffset(numFromKey - 1)
                val dayVal = daysObj.opt(key)
                if (dayVal is JSONArray) {
                    for (t in 0 until dayVal.length()) {
                        parseTaskObject(dayVal.optJSONObject(t), tasks.size + 1, defaultDate = dayDate)
                    }
                } else if (dayVal is JSONObject) {
                    val subTasks = dayVal.findArray("tasks", "task_list", "items", "schedule", "activities")
                    if (subTasks != null) {
                        for (t in 0 until subTasks.length()) {
                            parseTaskObject(subTasks.optJSONObject(t), tasks.size + 1, defaultDate = dayDate)
                        }
                    } else {
                        parseTaskObject(dayVal, tasks.size + 1, defaultDate = dayDate)
                    }
                }
            }
        }

        // Root keys matching "Day 1", "Day 2", etc.
        val dayKeys = root.keys().asSequence().filter { it.matches(Regex("""(?i)^day[\s_-]?\d+$""")) }.toList().sortedWith(Comparator { a, b ->
            val numA = Regex("""\d+""").find(a)?.value?.toIntOrNull() ?: 0
            val numB = Regex("""\d+""").find(b)?.value?.toIntOrNull() ?: 0
            numA.compareTo(numB)
        })
        if (dayKeys.isNotEmpty() && tasks.isEmpty()) {
            for ((dIdx, key) in dayKeys.withIndex()) {
                val dayNum = Regex("""\d+""").find(key)?.value?.toIntOrNull() ?: (dIdx + 1)
                val dayDate = calculateDateForDayOffset(dayNum - 1)
                val dayVal = root.opt(key)
                if (dayVal is JSONArray) {
                    for (t in 0 until dayVal.length()) {
                        parseTaskObject(dayVal.optJSONObject(t), tasks.size + 1, defaultDate = dayDate)
                    }
                } else if (dayVal is JSONObject) {
                    val subTasks = dayVal.findArray("tasks", "task_list", "items", "schedule")
                    if (subTasks != null) {
                        for (t in 0 until subTasks.length()) {
                            parseTaskObject(subTasks.optJSONObject(t), tasks.size + 1, defaultDate = dayDate)
                        }
                    } else {
                        parseTaskObject(dayVal, tasks.size + 1, defaultDate = dayDate)
                    }
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
                val targetMinutes = sObj.findDurationMinutes(
                    "target_minutes", "targetMinutes", "duration_minutes", "durationMinutes",
                    "duration", "minutes", default = 45
                )
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
        val habitsArray = root.findArray("habits", "habit_list", "routines", "habit")
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

                val name = hObj.findString("name", "title", "habit_title", "habit_name", "habit")
                if (name.isNullOrBlank()) {
                    val keysFound = hObj.keys().asSequence().toList().joinToString()
                    errors.add(
                        PlanValidationError(
                            itemType = "Habit",
                            index = itemNumber,
                            field = "Name",
                            summaryBullet = "Habit #$itemNumber is missing Name",
                            friendlyMessage = "Habit #$itemNumber is missing a Name. Found fields: [$keysFound]. Supported keys: 'name', 'title'.",
                            technicalDetails = "Required value 'name' missing at $.habits[$i]. Found keys: [$keysFound]"
                        )
                    )
                } else {
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

        // 5. Alarms Validation & Extraction (Robust time parsing for 12h & 24h)
        val alarms = mutableListOf<PlanAlarm>()
        val alarmsArray = root.findArray("alarms", "alarm_list", "reminders", "alarm")
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

                val rawTime = aObj.findString("time", "alarm_time", "schedule_time", "time_str", "timing")
                if (rawTime.isNullOrBlank()) {
                    errors.add(
                        PlanValidationError(
                            itemType = "Alarm",
                            index = itemNumber,
                            field = "Time",
                            summaryBullet = "Alarm #$itemNumber is missing Time",
                            friendlyMessage = "Alarm #$itemNumber is missing a Time. Please provide time like '06:00 AM' or '06:00'.",
                            technicalDetails = "Required value 'time' missing at $.alarms[$i]"
                        )
                    )
                } else {
                    val parsedTime = parseTimeHourMinute(rawTime)
                    if (parsedTime == null) {
                        errors.add(
                            PlanValidationError(
                                itemType = "Alarm",
                                index = itemNumber,
                                field = "Time",
                                summaryBullet = "Alarm #$itemNumber has invalid time format",
                                friendlyMessage = "Alarm #$itemNumber has invalid time format '$rawTime'. Please use 12h format like '06:00 AM' or 24h format like '06:00'.",
                                technicalDetails = "Failed to parse time '$rawTime' at $.alarms[$i].time"
                            )
                        )
                    } else {
                        val formattedTime = String.format(Locale.US, "%02d:%02d", parsedTime.first, parsedTime.second)
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

                // Task is stored in Room DB. Dynamic rolling window scheduler will handle alarms safely.
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

        // Arm dynamic rolling window for upcoming task alarms (OS safe)
        if (tasksImported > 0 || scheduleImported > 0) {
            AlarmScheduler.rescheduleUpcomingTaskAlarms(context)
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
            tasksCount = tasksImported + scheduleImported,
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

    private fun JSONObject.findDurationMinutes(vararg keys: String, default: Int = 45): Int {
        for (k in keys) {
            if (has(k) && !isNull(k)) {
                val direct = optInt(k, Int.MIN_VALUE)
                if (direct != Int.MIN_VALUE && direct > 0) return direct
                val str = optString(k, "").trim().lowercase(Locale.US)
                if (str.isNotEmpty()) {
                    val directNum = str.toIntOrNull()
                    if (directNum != null && directNum > 0) return directNum

                    if (str.contains("hour") || str.contains("hr")) {
                        val floatMatch = Regex("""(\d+(\.\d+)?)""").find(str)?.value?.toFloatOrNull()
                        if (floatMatch != null && floatMatch > 0) {
                            return (floatMatch * 60).toInt()
                        }
                    }

                    val digitMatch = Regex("""\d+""").find(str)?.value?.toIntOrNull()
                    if (digitMatch != null && digitMatch > 0) return digitMatch
                }
            }
        }
        return default
    }

    private fun calculateDateForDayOffset(dayOffset: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, dayOffset.coerceAtLeast(0))
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
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
