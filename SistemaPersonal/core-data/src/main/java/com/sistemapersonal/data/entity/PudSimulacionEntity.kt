package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pud_simulacion")
data class PudSimulacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val decision: String,
    val respuestaImporta: String,
    val respuestaEntiende: String,
    val respuestaExplora: String,
    val respuestaFiltra: String,
    val respuestaEvalua: String,
    val respuestaActua: String,
    val timestamp: Long
)
