package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evolucion_evento")
data class EvolucionEventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val carpetaId: Long? = null,
    val etiquetas: String = "",
    val imagenPath: String? = null,
    val enPapelera: Boolean = false,
    val eliminadoEn: Long? = null,
    val creadoEn: Long
)

@Entity(tableName = "evolucion_carpeta")
data class CarpetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

@Entity(tableName = "evolucion_etiqueta")
data class EtiquetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)
