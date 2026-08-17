package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entrenamiento_log")
data class EntrenamientoLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val tipo: String,
    val cantidadOSegundos: Int,
    val origen: String,
    val timestamp: Long
)
