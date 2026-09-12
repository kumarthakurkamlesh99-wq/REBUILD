package com.example.util

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.notification.AlarmScheduler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PlanImportManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun parsePlan(uri: Uri): Result<PlanImport> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            } ?: throw Exception("Could not read file")

            val adapter = moshi.adapter(PlanImport::class.java)
            val plan = adapter.fromJson(jsonString) ?: throw Exception("Invalid JSON format")
            
            // Validate required logic
            validatePlan(plan)
            
            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validatePlan(plan: PlanImport) {
        if (plan.planName.isNullOrBlank()) {
            throw Exception("Plan name is missing")
        }
        
        plan.alarms?.forEach { alarm ->
            val parts = alarm.time.split(":")
            if (parts.size != 2) throw Exception("Invalid alarm time format: ${alarm.time}")
            val h = parts[0].toIntOrNull() ?: throw Exception("Invalid hour in alarm time: ${alarm.time}")
            val m = parts[1].toIntOrNull() ?: throw Exception("Invalid minute in alarm time: ${alarm.time}")
            if (h !in 0..23 || m !in 0..59) {
                throw Exception("Invalid Schedule Time: ${alarm.time}")
            }
        }
    }

    suspend fun importPlan(plan: PlanImport, replaceMode: Boolean) = withContext(Dispatchers.IO) {
        // Create Backup Before Import
        createBackup()

        database.withTransaction {
            if (replaceMode) {
                database.goalDao().clearAll()
                database.dailyPlanDao().clearAll()
                database.habitDao().clearAll()
                database.alarmDao().clearAllAlarms()
                database.winterArcObjectivesDao().clearAllArcGoals()
                // other tables if needed
            }

            // Import Goals
            plan.goals?.forEach { g ->
                val category = runCatching { GoalCategory.valueOf(g.category ?: "ACADEMIC") }
                    .getOrDefault(GoalCategory.ACADEMIC)
                database.goalDao().insertGoal(
                    GoalEntity(
                        title = g.title,
                        description = g.description ?: "",
                        category = category,
                        targetDate = g.targetDate
                    )
                )
            }

            // Import Tasks (assuming DailyPlanTaskEntity)
            plan.tasks?.forEach { t ->
                val type = runCatching { TaskType.valueOf(t.type ?: "LECTURE") }
                    .getOrDefault(TaskType.LECTURE)
                database.dailyPlanDao().insertTask(
                    DailyPlanTaskEntity(
                        date = t.date ?: "2026-09-11", // fallback to current date or something
                        subject = t.subject ?: "General",
                        title = t.title,
                        type = type,
                        details = t.details ?: "",
                        targetMinutes = t.targetMinutes ?: 45
                    )
                )
            }
            
            // Import Focus Sessions (Schedules)
            plan.focusSessions?.forEach { f ->
                database.dailyPlanDao().insertTask(
                    DailyPlanTaskEntity(
                        date = "2026-09-11", // default date for schedule
                        subject = "Focus Session",
                        title = f.title,
                        type = TaskType.REVISION,
                        details = "Duration: ${f.duration ?: 25} minutes",
                        targetMinutes = f.duration ?: 25
                    )
                )
            }

            // Import Habits
            plan.habits?.forEach { h ->
                val habitType = runCatching { HabitType.valueOf(h.habitType ?: "CUSTOM") }
                    .getOrDefault(HabitType.CUSTOM)
                database.habitDao().insertHabit(
                    HabitEntity(
                        name = h.name,
                        habitType = habitType,
                        isNegativeHabit = h.isNegative ?: false,
                        targetUnit = h.targetUnit ?: "Completed",
                        targetNumeric = h.targetNumeric ?: 1,
                        isDefault = false
                    )
                )
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
            plan.alarms?.forEach { a ->
                val parts = a.time.split(":")
                val h = parts[0].toInt()
                val m = parts[1].toInt()
                
                val challengeType = runCatching { AlarmChallengeType.valueOf(a.challengeType ?: "MATH") }
                    .getOrDefault(AlarmChallengeType.MATH)
                
                val difficulty = runCatching { AlarmDifficulty.valueOf(a.difficulty ?: "MEDIUM") }
                    .getOrDefault(AlarmDifficulty.MEDIUM)

                database.alarmDao().insertAlarm(
                    AlarmEntity(
                        title = a.title,
                        hour = h,
                        minute = m,
                        challengeType = challengeType,
                        challengeDifficulty = difficulty
                    )
                )
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
        
        // Ensure Alarms are scheduled via AlarmScheduler
        AlarmScheduler.scheduleAllDefaultAlarms(context)
    }

    private suspend fun createBackup() = withContext(Dispatchers.IO) {
        try {
            val goals = database.goalDao().getAllGoals().first()
            val alarms = database.alarmDao().getAllAlarmsDirect()
            // To simplify, just saving a marker or partial backup.
            // In full implementation, serialize everything.
            val backupPlan = PlanImport(
                planName = "Auto-Backup",
                version = 1,
                goals = goals.map { PlanGoal(it.title, it.description, it.category.name, it.targetDate) },
                alarms = alarms.map { PlanAlarm(it.title, String.format("%02d:%02d", it.hour, it.minute), it.challengeType.name, it.challengeDifficulty.name) }
            )
            val json = moshi.adapter(PlanImport::class.java).toJson(backupPlan)
            val file = java.io.File(context.filesDir, "plan_backup.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun restoreBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(context.filesDir, "plan_backup.json")
            if (!file.exists()) throw Exception("No backup found")
            val json = file.readText()
            val plan = moshi.adapter(PlanImport::class.java).fromJson(json) ?: throw Exception("Invalid backup data")
            
            // Re-import the backup
            database.withTransaction {
                database.goalDao().clearAll()
                database.dailyPlanDao().clearAll()
                database.habitDao().clearAll()
                database.alarmDao().clearAllAlarms()
                database.winterArcObjectivesDao().clearAllArcGoals()
                
                plan.goals?.forEach { g ->
                    val category = runCatching { GoalCategory.valueOf(g.category ?: "ACADEMIC") }.getOrDefault(GoalCategory.ACADEMIC)
                    database.goalDao().insertGoal(GoalEntity(title = g.title, description = g.description ?: "", category = category, targetDate = g.targetDate))
                }
                plan.alarms?.forEach { a ->
                    val parts = a.time.split(":")
                    val h = parts[0].toInt()
                    val m = parts[1].toInt()
                    val c = runCatching { AlarmChallengeType.valueOf(a.challengeType ?: "MATH") }.getOrDefault(AlarmChallengeType.MATH)
                    val d = runCatching { AlarmDifficulty.valueOf(a.difficulty ?: "MEDIUM") }.getOrDefault(AlarmDifficulty.MEDIUM)
                    database.alarmDao().insertAlarm(AlarmEntity(title = a.title, hour = h, minute = m, challengeType = c, challengeDifficulty = d))
                }
            }
            AlarmScheduler.scheduleAllDefaultAlarms(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
