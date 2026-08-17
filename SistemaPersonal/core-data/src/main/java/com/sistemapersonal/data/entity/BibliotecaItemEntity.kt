package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biblioteca_item")
data class BibliotecaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val categoria: String,
    val resumen: String,
    val enlace: String,
    val creadoEn: Long,
    val actualizadoEn: Long
)
