package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_purchases")
data class LevelPurchaseEntity(
    @PrimaryKey
    val level: Int, // 1..25
    val rankTitle: String,
    val xpCost: Int,
    val transactionId: String, // e.g. "TXN-RB-2026-000124"
    val unlockedAt: Long = System.currentTimeMillis(),
    val isCertificateMinted: Boolean = false,
    val certificateMintedAt: Long? = null,
    val certificateTxnId: String? = null
)
