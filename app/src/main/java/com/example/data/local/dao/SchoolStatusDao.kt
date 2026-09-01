package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SchoolStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolStatusDao {
    @Query("SELECT * FROM school_status_logs WHERE date = :date LIMIT 1")
    fun getStatusForDate(date: String): Flow<SchoolStatusEntity?>

    @Query("SELECT * FROM school_status_logs WHERE date = :date LIMIT 1")
    suspend fun getStatusForDateDirect(date: String): SchoolStatusEntity?

    @Query("SELECT * FROM school_status_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<SchoolStatusEntity>>

    @Query("SELECT * FROM school_status_logs WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getLogsForMonth(monthPrefix: String): Flow<List<SchoolStatusEntity>>

    @Query("SELECT COUNT(*) FROM school_status_logs WHERE isPresent = 1")
    fun getTotalPresentDays(): Flow<Int>

    @Query("SELECT COUNT(*) FROM school_status_logs WHERE isPresent = 0 AND isHoliday = 0")
    fun getTotalAbsentDays(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(status: SchoolStatusEntity)

    @Update
    suspend fun update(status: SchoolStatusEntity)

    @Query("DELETE FROM school_status_logs")
    suspend fun clearAll()
}
