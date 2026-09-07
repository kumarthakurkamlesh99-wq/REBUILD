package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MistakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistake_entries ORDER BY timestamp DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_entries WHERE subjectCode = :subjectCode ORDER BY timestamp DESC")
    fun getMistakesBySubject(subjectCode: String): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_entries WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getUnresolvedMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT COUNT(*) FROM mistake_entries WHERE isResolved = 0")
    fun getUnresolvedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mistake_entries WHERE isResolved = 1")
    fun getResolvedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistakes(mistakes: List<MistakeEntity>)

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)

    @Query("DELETE FROM mistake_entries WHERE id = :id")
    suspend fun deleteMistakeById(id: Long)
}
