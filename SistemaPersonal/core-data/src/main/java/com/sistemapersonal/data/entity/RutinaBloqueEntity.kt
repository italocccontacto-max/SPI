package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutina_bloque")
data class RutinaBloqueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipoSemana: String,
    val inicio: String,
    val fin: String,
    val etiqueta: String,
    val orden: Int
)

@Entity(tableName = "rutina_bloque_log")
data class RutinaBloqueLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bloqueId: Long,
    val fecha: String,
    val completadoEn: Long
)
