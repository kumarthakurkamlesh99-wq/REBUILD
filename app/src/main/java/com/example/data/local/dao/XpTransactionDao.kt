package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.XpTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface XpTransactionDao {
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<XpTransactionEntity>>

    @Query("SELECT * FROM xp_transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<XpTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: XpTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<XpTransactionEntity>)

    @Query("SELECT SUM(xp) FROM xp_transactions")
    fun getTotalXpFlow(): Flow<Int?>

    @Query("SELECT SUM(xp) FROM xp_transactions")
    suspend fun getTotalXpDirect(): Int?

    @Query("SELECT SUM(xp) FROM xp_transactions WHERE timestamp >= :sinceTimestamp")
    fun getXpSinceFlow(sinceTimestamp: Long): Flow<Int?>

    @Query("SELECT SUM(xp) FROM xp_transactions WHERE timestamp >= :sinceTimestamp")
    suspend fun getXpSinceDirect(sinceTimestamp: Long): Int?

    @Query("SELECT COUNT(*) FROM xp_transactions")
    suspend fun getTransactionCount(): Int

    @Query("DELETE FROM xp_transactions")
    suspend fun clearAll()
}
