package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyPlanTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plan_tasks WHERE date = :date ORDER BY orderIndex ASC, id ASC")
    fun getTasksForDate(date: String): Flow<List<DailyPlanTaskEntity>>

    @Query("SELECT * FROM daily_plan_tasks WHERE date = :date ORDER BY orderIndex ASC, id ASC")
    suspend fun getTasksForDateDirect(date: String): List<DailyPlanTaskEntity>

    @Query("SELECT * FROM daily_plan_tasks WHERE date < :todayDate AND isCompleted = 0")
    suspend fun getIncompleteTasksBefore(todayDate: String): List<DailyPlanTaskEntity>

    @Query("SELECT COUNT(*) FROM daily_plan_tasks WHERE date = :date")
    fun getTaskCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_plan_tasks WHERE date = :date AND isCompleted = 1")
    fun getCompletedTaskCountForDate(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyPlanTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyPlanTaskEntity>)

    @Update
    suspend fun updateTask(task: DailyPlanTaskEntity)

    @Delete
    suspend fun deleteTask(task: DailyPlanTaskEntity)

    @Query("DELETE FROM daily_plan_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM daily_plan_tasks WHERE date = :date")
    suspend fun clearTasksForDate(date: String)
}
