package com.centinela.app.contract

import com.sistemapersonal.data.dao.IntentionContractDao
import com.sistemapersonal.data.entity.IntentionContractEntity
import kotlinx.coroutines.flow.Flow

class IntentionContractRepository(private val dao: IntentionContractDao) {
    suspend fun saveContract(text: String): Long {
        val contract = IntentionContractEntity(contractText = text)
        return dao.insert(contract)
    }

    suspend fun getLatest(): IntentionContractEntity? = dao.getLatest()

    fun getLatestFlow(): Flow<IntentionContractEntity?> = dao.getLatestFlow()

    suspend fun markAsRead(id: Long) = dao.markAsRead(id)
}
