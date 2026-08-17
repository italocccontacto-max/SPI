package com.centinela.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.domain.SyncManager
import com.sistemapersonal.network.FirebaseBridge
import com.sistemapersonal.network.FirebaseConfig
import com.sistemapersonal.network.StorageBridge
import kotlinx.coroutines.flow.first

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!FirebaseConfig.estaConfigurado) return Result.success()
        val repo = SistemaPersonalRepository.get(applicationContext)
        if (!repo.ajustes.syncFamiliaHabilitado.first()) return Result.success()

        return runCatching {
            PersonalFamilyProvisioning.ensure(applicationContext).getOrThrow()
            val familyId = repo.ajustes.familyId.first() ?: error("Familia no provisionada")
            val bridge = FirebaseBridge(applicationContext, familyId)
            val storage = StorageBridge(applicationContext, familyId)
            SyncManager(repo, bridge, storage).sincronizarPendientes()
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
