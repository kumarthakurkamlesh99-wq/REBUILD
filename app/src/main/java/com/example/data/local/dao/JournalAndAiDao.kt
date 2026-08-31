package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AiPlanCacheEntity
import com.example.data.local.entity.DailyReflectionEntity
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subjectTag = :tag ORDER BY isPinned DESC, timestamp DESC")
    fun getNotesBySubject(tag: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE date = :date LIMIT 1")
    fun getReflectionForDate(date: String): Flow<DailyReflectionEntity?>

    @Query("SELECT * FROM daily_reflections WHERE date = :date LIMIT 1")
    suspend fun getReflectionForDateDirect(date: String): DailyReflectionEntity?

    @Query("SELECT * FROM daily_reflections ORDER BY date DESC LIMIT 30")
    fun getRecentReflections(): Flow<List<DailyReflectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(reflection: DailyReflectionEntity)

    @Query("DELETE FROM daily_reflections")
    suspend fun clearAll()
}

@Dao
interface AiPlanDao {
    @Query("SELECT * FROM ai_plan_cache")
    fun getAllCachedPlans(): Flow<List<AiPlanCacheEntity>>

    @Query("SELECT * FROM ai_plan_cache WHERE planType = :planType LIMIT 1")
    fun getCachedPlan(planType: String): Flow<AiPlanCacheEntity?>

    @Query("SELECT * FROM ai_plan_cache WHERE planType = :planType LIMIT 1")
    suspend fun getCachedPlanDirect(planType: String): AiPlanCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlan(plan: AiPlanCacheEntity)

    @Query("DELETE FROM ai_plan_cache WHERE planType = :planType")
    suspend fun deletePlan(planType: String)

    @Query("DELETE FROM ai_plan_cache")
    suspend fun clearAll()
}
