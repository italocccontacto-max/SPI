package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val id: Int = 1,
    val diasConsecutivos: Int = 0,
    val mejorRacha: Int = 0,
    val fechaInicio: String = "",
    val fechaUltimoExito: String = "",
    val rota: Boolean = false
)
