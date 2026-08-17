package com.sistemapersonal.domain

import com.sistemapersonal.data.entity.StreakEntity
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import java.text.SimpleDateFormat
import java.util.*

private val FORMATO_DIA = SimpleDateFormat("yyyyMMdd", Locale.US)

class RachaManager(private val repo: SistemaPersonalRepository) {

    suspend fun registrarDiaLimpio(fecha: String = hoy()) {
        val actual = repo.streakDao().obtener() ?: StreakEntity(fechaInicio = fecha)
        if (actual.fechaUltimoExito == fecha) return
        val nuevosDias = if (actual.rota || actual.fechaInicio.isBlank()) 1 else actual.diasConsecutivos + 1
        val mejor = maxOf(actual.mejorRacha, nuevosDias)
        repo.streakDao().guardar(
            actual.copy(
                diasConsecutivos = nuevosDias,
                mejorRacha = mejor,
                fechaInicio = if (actual.rota || actual.fechaInicio.isBlank()) fecha else actual.fechaInicio,
                fechaUltimoExito = fecha,
                rota = false
            )
        )
    }

    suspend fun romperRacha() {
        val actual = repo.streakDao().obtener() ?: StreakEntity()
        repo.streakDao().guardar(actual.copy(diasConsecutivos = 0, rota = true))
    }

    fun hoy(): String = FORMATO_DIA.format(Date())
}
