package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LevelPurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelPurchaseDao {

    @Query("SELECT * FROM level_purchases ORDER BY level ASC")
    fun getAllPurchases(): Flow<List<LevelPurchaseEntity>>

    @Query("SELECT * FROM level_purchases ORDER BY level ASC")
    suspend fun getAllPurchasesDirect(): List<LevelPurchaseEntity>

    @Query("SELECT * FROM level_purchases WHERE level = :level LIMIT 1")
    fun getPurchaseForLevel(level: Int): Flow<LevelPurchaseEntity?>

    @Query("SELECT * FROM level_purchases WHERE level = :level LIMIT 1")
    suspend fun getPurchaseForLevelDirect(level: Int): LevelPurchaseEntity?

    @Query("SELECT MAX(level) FROM level_purchases")
    fun getMaxUnlockedLevelFlow(): Flow<Int?>

    @Query("SELECT MAX(level) FROM level_purchases")
    suspend fun getMaxUnlockedLevelDirect(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: LevelPurchaseEntity)

    @Update
    suspend fun update(purchase: LevelPurchaseEntity)

    @Query("UPDATE level_purchases SET isCertificateMinted = 1, certificateMintedAt = :timestamp, certificateTxnId = :txnId WHERE level = :level")
    suspend fun markCertificateMinted(level: Int, timestamp: Long, txnId: String)
}
