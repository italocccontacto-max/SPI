package com.sistemapersonal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutricion_metas")
data class NutricionMetasEntity(
    @PrimaryKey val id: Int = 1,
    val kcal: Int = 2000,
    val proteinas: Int = 120,
    val carbohidratos: Int = 240,
    val grasas: Int = 60,
    val hidratacionMl: Int = 2100
)

@Entity(tableName = "agua_dia")
data class AguaDiaEntity(
    @PrimaryKey val fecha: String,
    val ml: Int = 0
)

@Entity(tableName = "nutricion_notas")
data class NutricionNotasEntity(
    @PrimaryKey val fecha: String,
    val notas: String = ""
)
