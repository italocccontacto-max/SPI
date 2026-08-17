package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshot")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val filePath: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val uploaded: Boolean = false,
    val remotePath: String? = null,
    val relevante: Boolean = false
)
