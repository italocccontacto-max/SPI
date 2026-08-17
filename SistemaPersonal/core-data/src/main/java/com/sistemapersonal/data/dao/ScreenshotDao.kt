package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.ScreenshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Insert
    suspend fun insertar(s: ScreenshotEntity): Long

    @Query("SELECT * FROM screenshot ORDER BY timestamp DESC LIMIT :limite")
    fun recientes(limite: Int = 100): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshot WHERE relevante = 1 AND uploaded = 0 LIMIT :limite")
    suspend fun pendientesRelevantes(limite: Int = 20): List<ScreenshotEntity>

    @Query("UPDATE screenshot SET uploaded = 1, remotePath = :path WHERE id = :id")
    suspend fun marcarSubida(id: Long, path: String?)

    @Query("SELECT * FROM screenshot WHERE timestamp < :antesDe")
    suspend fun anterioresA(antesDe: Long): List<ScreenshotEntity>

    @Delete
    suspend fun eliminar(s: ScreenshotEntity)

    @Query("DELETE FROM screenshot WHERE timestamp < :antesDe")
    suspend fun eliminarAnterioresA(antesDe: Long): Int

    @Query("DELETE FROM screenshot WHERE filePath IN (:paths)")
    suspend fun eliminarPorPaths(paths: List<String>): Int
}
