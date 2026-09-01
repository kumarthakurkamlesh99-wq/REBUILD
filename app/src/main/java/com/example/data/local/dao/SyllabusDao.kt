package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SyllabusChapterEntity
import com.example.data.local.entity.SyllabusTopicEntity
import com.example.data.local.entity.SyllabusUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    // Units
    @Query("SELECT * FROM syllabus_units ORDER BY orderIndex ASC")
    fun getAllUnits(): Flow<List<SyllabusUnitEntity>>

    @Query("SELECT * FROM syllabus_units WHERE subjectCode = :subjectCode ORDER BY unitNumber ASC")
    fun getUnitsForSubject(subjectCode: String): Flow<List<SyllabusUnitEntity>>

    @Query("SELECT * FROM syllabus_units WHERE subjectCode = :subjectCode ORDER BY unitNumber ASC")
    suspend fun getUnitsForSubjectDirect(subjectCode: String): List<SyllabusUnitEntity>

    @Query("SELECT * FROM syllabus_units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: Long): SyllabusUnitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<SyllabusUnitEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: SyllabusUnitEntity): Long

    @Update
    suspend fun updateUnit(unit: SyllabusUnitEntity)

    @Query("DELETE FROM syllabus_units")
    suspend fun clearAllUnits()

    // Chapters
    @Query("SELECT * FROM syllabus_chapters WHERE unitId = :unitId ORDER BY chapterNumber ASC")
    fun getChaptersForUnit(unitId: Long): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE unitId = :unitId ORDER BY chapterNumber ASC")
    suspend fun getChaptersForUnitDirect(unitId: Long): List<SyllabusChapterEntity>

    @Query("SELECT * FROM syllabus_chapters WHERE subjectCode = :subjectCode ORDER BY chapterNumber ASC")
    fun getChaptersForSubject(subjectCode: String): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE subjectCode = :subjectCode ORDER BY chapterNumber ASC")
    suspend fun getChaptersForSubjectDirect(subjectCode: String): List<SyllabusChapterEntity>

    @Query("SELECT * FROM syllabus_chapters ORDER BY id ASC")
    fun getAllChapters(): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters ORDER BY id ASC")
    suspend fun getAllChaptersDirect(): List<SyllabusChapterEntity>

    @Query("SELECT * FROM syllabus_chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Long): SyllabusChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<SyllabusChapterEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: SyllabusChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: SyllabusChapterEntity)

    @Query("DELETE FROM syllabus_chapters")
    suspend fun clearAllChapters()

    // Topics
    @Query("SELECT * FROM syllabus_topics WHERE chapterId = :chapterId ORDER BY topicNumber ASC")
    fun getTopicsForChapter(chapterId: Long): Flow<List<SyllabusTopicEntity>>

    @Query("SELECT * FROM syllabus_topics WHERE chapterId = :chapterId ORDER BY topicNumber ASC")
    suspend fun getTopicsForChapterDirect(chapterId: Long): List<SyllabusTopicEntity>

    @Query("SELECT * FROM syllabus_topics WHERE unitId = :unitId ORDER BY topicNumber ASC")
    fun getTopicsForUnit(unitId: Long): Flow<List<SyllabusTopicEntity>>

    @Query("SELECT * FROM syllabus_topics WHERE unitId = :unitId ORDER BY topicNumber ASC")
    suspend fun getTopicsForUnitDirect(unitId: Long): List<SyllabusTopicEntity>

    @Query("SELECT * FROM syllabus_topics WHERE subjectCode = :subjectCode ORDER BY id ASC")
    fun getTopicsForSubject(subjectCode: String): Flow<List<SyllabusTopicEntity>>

    @Query("SELECT * FROM syllabus_topics WHERE subjectCode = :subjectCode ORDER BY id ASC")
    suspend fun getTopicsForSubjectDirect(subjectCode: String): List<SyllabusTopicEntity>

    @Query("SELECT * FROM syllabus_topics ORDER BY id ASC")
    fun getAllTopics(): Flow<List<SyllabusTopicEntity>>

    @Query("SELECT * FROM syllabus_topics ORDER BY id ASC")
    suspend fun getAllTopicsDirect(): List<SyllabusTopicEntity>

    @Query("SELECT * FROM syllabus_topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): SyllabusTopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<SyllabusTopicEntity>)

    @Update
    suspend fun updateTopic(topic: SyllabusTopicEntity)

    @Query("DELETE FROM syllabus_topics")
    suspend fun clearAllTopics()

    @Query("SELECT COUNT(*) FROM syllabus_topics WHERE subjectCode = :subjectCode")
    fun getTotalTopicsCountForSubject(subjectCode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM syllabus_topics WHERE subjectCode = :subjectCode AND status IN ('COMPLETED', 'REVISED_ONCE', 'REVISED_TWICE', 'MASTERED')")
    fun getCompletedTopicsCountForSubject(subjectCode: String): Flow<Int>
}
