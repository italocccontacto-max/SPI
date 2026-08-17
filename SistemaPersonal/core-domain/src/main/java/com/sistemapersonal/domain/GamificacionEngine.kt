package com.sistemapersonal.domain

import com.sistemapersonal.data.entity.AchievementEntity
import com.sistemapersonal.data.entity.PointsLedgerEntity
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.model.CatalogoLogros
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class EventoCelebracion(val titulo: String, val subtitulo: String?)

class GamificacionEngine private constructor(private val repo: SistemaPersonalRepository) {

    private val _celebraciones = MutableSharedFlow<EventoCelebracion>(extraBufferCapacity = 4)
    val celebraciones: SharedFlow<EventoCelebracion> = _celebraciones.asSharedFlow()

    suspend fun otorgarPuntos(delta: Int, razon: String) {
        repo.pointsDao().agregar(
            PointsLedgerEntity(delta = delta, razon = razon, timestamp = System.currentTimeMillis())
        )
    }

    suspend fun intentarDesbloquear(clave: String): Boolean {
        val existente = repo.achievementDao().obtener(clave)
        if (existente != null) return false
        val base = CatalogoLogros.BASE.find { it.clave == clave } ?: return false
        repo.achievementDao().guardar(
            AchievementEntity(
                clave = base.clave,
                titulo = base.titulo,
                descripcion = base.descripcion,
                desbloqueadoEn = System.currentTimeMillis()
            )
        )
        _celebraciones.emit(EventoCelebracion(titulo = base.titulo, subtitulo = base.descripcion))
        return true
    }

    suspend fun evaluarLogrosPorRacha(diasConsecutivos: Int) {
        if (diasConsecutivos >= 1) intentarDesbloquear("primer_dia_sin")
        if (diasConsecutivos >= 7) intentarDesbloquear("racha_7")
        if (diasConsecutivos >= 30) intentarDesbloquear("racha_30")
    }

    companion object {
        @Volatile private var INSTANCE: GamificacionEngine? = null
        fun get(repo: SistemaPersonalRepository): GamificacionEngine =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: GamificacionEngine(repo).also { INSTANCE = it }
            }
    }
}
