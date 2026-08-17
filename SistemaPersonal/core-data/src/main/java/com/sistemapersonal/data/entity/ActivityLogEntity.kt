package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_log")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appPackage: String,
    val appName: String,
    val timestampStart: Long,
    val timestampEnd: Long,
    val durationMs: Long,
    val fecha: String,
    val esAppProhibida: Boolean,
    val sincronizado: Boolean = false
)
