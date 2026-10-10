package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FutureSelfCallRecordEntity
import com.example.data.local.entity.FutureSelfCallState

@Dao
interface FutureSelfCallDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: FutureSelfCallRecordEntity): Long

    @Update
    suspend fun update(record: FutureSelfCallRecordEntity)

    @Query("SELECT * FROM future_self_call_records WHERE callOccurrenceId = :occurrenceId LIMIT 1")
    suspend fun getRecordByOccurrenceId(occurrenceId: String): FutureSelfCallRecordEntity?

    @Query("SELECT * FROM future_self_call_records WHERE taskId = :taskId ORDER BY scheduledTimestamp DESC LIMIT 1")
    suspend fun getLatestRecordForTask(taskId: Long): FutureSelfCallRecordEntity?

    @Query("SELECT * FROM future_self_call_records WHERE taskId = :taskId ORDER BY scheduledTimestamp ASC")
    suspend fun getRecordsForTask(taskId: Long): List<FutureSelfCallRecordEntity>

    @Query("SELECT * FROM future_self_call_records WHERE taskId = :taskId AND state IN ('SCHEDULED', 'RINGING') ORDER BY scheduledTimestamp ASC LIMIT 1")
    suspend fun getPendingOrRingingRecordForTask(taskId: Long): FutureSelfCallRecordEntity?

    @Query("UPDATE future_self_call_records SET state = :newState, handledAt = :handledAt, lastAction = :action WHERE callOccurrenceId = :occurrenceId")
    suspend fun updateCallState(occurrenceId: String, newState: FutureSelfCallState, handledAt: Long?, action: String)

    @Query("UPDATE future_self_call_records SET state = 'CANCELLED', handledAt = :timestamp, lastAction = 'TASK_COMPLETED' WHERE taskId = :taskId AND state IN ('SCHEDULED', 'RINGING')")
    suspend fun cancelPendingCallsForTask(taskId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE future_self_call_records SET state = 'MISSED', handledAt = :timestamp, lastAction = 'OVERDUE_MISSED' WHERE taskId = :taskId AND state = 'SCHEDULED' AND scheduledTimestamp < :timestamp")
    suspend fun markOverdueCallsAsMissed(taskId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM future_self_call_records")
    suspend fun clearAll()
}
