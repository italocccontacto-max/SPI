package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "obj_area")
data class AreaObjetivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val orden: Int
)

@Entity(tableName = "obj_punto")
data class PuntoObjetivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val areaId: Long,
    val titulo: String,
    val porQue: String,
    val conducta: String,
    val principio: String,
    val pregunta: String,

    val metricaResultado: Int = 0,

    val metricaEjecucionSemana: String = "0000000",
    val orden: Int
)

@Entity(tableName = "obj_micro")
data class MicroObjetivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val puntoId: Long,
    val texto: String,
    val hecho: Boolean = false,
    val orden: Int
)

@Entity(tableName = "obj_indicador")
data class IndicadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val nombre: String,
    val base: String = "",
    val meta: String = "",
    val actual: String = "",
    val valorFinal: String = "",
    val cumplido: Boolean = false
)
