package com.sistemapersonal.domain

import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.network.EventoRemoto
import com.sistemapersonal.network.FirebaseBridge
import com.sistemapersonal.network.StorageBridge
import kotlinx.coroutines.flow.firstOrNull

class SyncManager(
    private val repo: SistemaPersonalRepository,
    private val bridge: FirebaseBridge?,
    private val storage: StorageBridge? = null
) {
    suspend fun sincronizarPendientes() {
        val puente = bridge ?: return
        if (repo.ajustes.syncFamiliaHabilitado.firstOrNull() != true) return

        val pendientesActividad = repo.activityDao().pendientesDeSync()
        val idsOk = mutableListOf<Long>()
        var ultimaAppSincronizada: String? = null
        for (log in pendientesActividad) {
            val ok = puente.publicarEvento(
                EventoRemoto(
                    tipo = "app_abierta",
                    timestamp = log.timestampStart,
                    resumen = "${log.appName}: ${log.durationMs / 60000} min",
                    extra = mapOf("paquete" to log.appPackage, "prohibida" to log.esAppProhibida)
                )
            )
            if (ok) {
                idsOk += log.id
                ultimaAppSincronizada = log.appName
            }
        }
        if (idsOk.isNotEmpty()) repo.activityDao().marcarSincronizados(idsOk)

        val screenshotsPendientes = repo.screenshotDao().pendientesRelevantes()
        for (s in screenshotsPendientes) {
            val remotePath = storage?.subirScreenshot(java.io.File(s.filePath), s.timestamp) ?: continue
            val ok = puente.publicarEvento(
                EventoRemoto(
                    tipo = "screenshot",
                    timestamp = s.timestamp,
                    resumen = "Captura relevante",
                    extra = mapOf("width" to s.width, "height" to s.height, "storagePath" to remotePath)
                )
            )
            if (ok) repo.screenshotDao().marcarSubida(s.id, remotePath)
        }

        publicarEstadoActual(puente, ultimaAppSincronizada)
    }

    private suspend fun publicarEstadoActual(puente: FirebaseBridge, ultimaApp: String?) {
        val streak = repo.streakDao().obtener()
        puente.actualizarEstadoActual(
            mapOf(
                "racha_dias" to (streak?.diasConsecutivos ?: 0),
                "ultima_app" to (ultimaApp ?: ""),
                "ultimo_sync" to System.currentTimeMillis()
            )
        )
    }
}
