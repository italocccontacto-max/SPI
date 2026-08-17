package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.IntentionContractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentionContractDao {
    @Insert
    suspend fun insert(contract: IntentionContractEntity): Long

    @Query("SELECT * FROM intention_contracts ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): IntentionContractEntity?

    @Query("SELECT * FROM intention_contracts ORDER BY createdAt DESC LIMIT 1")
    fun getLatestFlow(): Flow<IntentionContractEntity?>

    @Query("UPDATE intention_contracts SET wasRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}
