package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val clave: String,
    val titulo: String,
    val descripcion: String,
    val desbloqueadoEn: Long?
)
