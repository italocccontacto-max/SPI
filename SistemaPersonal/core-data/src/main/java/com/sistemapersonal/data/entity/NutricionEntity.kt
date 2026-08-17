package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutricion_log")
data class NutricionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val nombreAlimento: String,
    val gramos: Float,
    val kcal: Float,
    val proteinas: Float,
    val carbohidratos: Float,
    val grasas: Float,
    val momento: String,
    val timestamp: Long
)
