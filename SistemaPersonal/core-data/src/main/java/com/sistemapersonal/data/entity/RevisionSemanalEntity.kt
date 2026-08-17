package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_semanal")
data class RevisionSemanalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semana: String,
    val respuestasJson: String,
    val timestamp: Long
)
