package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intention_contracts")
data class IntentionContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contractText: String,
    val createdAt: Long = System.currentTimeMillis(),
    val wasRead: Boolean = false,
    val wasHonored: Boolean? = null,
    val honoredNote: String? = null
)
