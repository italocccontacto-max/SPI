package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despertar_log")
data class DespertarEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val descargaMental: String,
    val directivaPrincipal: String,
    val timestamp: Long
)

@Entity(tableName = "cierre_dia_log")
data class CierreDiaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val anclas: String,
    val evidencia: String,
    val desviacion: String,
    val porque: String,
    val modificacion: String,
    val timestamp: Long
)

@Entity(tableName = "antes_dormir_log")
data class AntesDormirEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val pendiente: String,
    val victorias: String,
    val timestamp: Long
)
