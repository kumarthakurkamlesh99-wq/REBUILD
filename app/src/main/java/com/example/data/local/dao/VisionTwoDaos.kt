package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DistractionLogEntity
import com.example.data.local.entity.SyllabusSubtopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusSubtopicDao {
    @Query("SELECT * FROM syllabus_subtopics WHERE topicId = :topicId ORDER BY subtopicNumber ASC")
    fun getSubtopicsForTopic(topicId: Long): Flow<List<SyllabusSubtopicEntity>>

    @Query("SELECT * FROM syllabus_subtopics WHERE subjectCode = :subjectCode ORDER BY topicId ASC, subtopicNumber ASC")
    fun getSubtopicsForSubject(subjectCode: String): Flow<List<SyllabusSubtopicEntity>>

    @Query("SELECT COUNT(*) FROM syllabus_subtopics WHERE isCompleted = 1")
    fun getCompletedSubtopicsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM syllabus_subtopics")
    fun getTotalSubtopicsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopic(subtopic: SyllabusSubtopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopics(subtopics: List<SyllabusSubtopicEntity>)

    @Update
    suspend fun updateSubtopic(subtopic: SyllabusSubtopicEntity)

    @Delete
    suspend fun deleteSubtopic(subtopic: SyllabusSubtopicEntity)
}

@Dao
interface DistractionDao {
    @Query("SELECT * FROM distraction_logs ORDER BY timestamp DESC")
    fun getAllDistractions(): Flow<List<DistractionLogEntity>>

    @Query("SELECT * FROM distraction_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getDistractionsForDate(date: String): Flow<List<DistractionLogEntity>>

    @Query("SELECT COUNT(*) FROM distraction_logs WHERE date = :date")
    fun getDistractionCountForDate(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistraction(log: DistractionLogEntity): Long

    @Query("DELETE FROM distraction_logs")
    suspend fun clearAll()
}
