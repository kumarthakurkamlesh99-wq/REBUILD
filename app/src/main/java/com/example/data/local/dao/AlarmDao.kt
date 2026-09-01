package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.AlarmLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    suspend fun getAllAlarmsDirect(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getEnabledAlarmsDirect(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    fun getAlarmByIdFlow(id: Long): Flow<AlarmEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<AlarmEntity>)

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("DELETE FROM alarms")
    suspend fun clearAllAlarms()

    // Alarm Logs
    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAlarmLogs(): Flow<List<AlarmLogEntity>>

    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentAlarmLogsDirect(): List<AlarmLogEntity>

    @Query("SELECT * FROM alarm_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<AlarmLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmLog(log: AlarmLogEntity): Long

    @Query("DELETE FROM alarm_logs")
    suspend fun clearAllLogs()
}
