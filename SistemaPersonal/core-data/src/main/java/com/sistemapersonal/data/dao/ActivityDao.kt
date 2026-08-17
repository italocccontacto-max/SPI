package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertar(log: ActivityLogEntity): Long

    @Query("SELECT * FROM activity_log WHERE fecha = :fecha ORDER BY timestampStart DESC")
    fun porDia(fecha: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_log WHERE fecha BETWEEN :desde AND :hasta ORDER BY timestampStart DESC")
    fun enRango(desde: String, hasta: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT appPackage, SUM(durationMs) as total FROM activity_log WHERE fecha = :fecha GROUP BY appPackage ORDER BY total DESC")
    suspend fun resumenPorApp(fecha: String): List<ResumenApp>

    @Query("SELECT * FROM activity_log WHERE sincronizado = 0 LIMIT :limite")
    suspend fun pendientesDeSync(limite: Int = 200): List<ActivityLogEntity>

    @Query("UPDATE activity_log SET sincronizado = 1 WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<Long>)
}

data class ResumenApp(val appPackage: String, val total: Long)
