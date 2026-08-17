package com.centinela.app

import android.app.Application
import androidx.work.*
import com.centinela.app.sync.SyncWorker
import com.centinela.app.sync.ScreenshotCleanupWorker
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.network.FirebaseConfig
import com.sistemapersonal.network.FirebaseIdentity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SistemaPersonalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseConfig.inicializar(this)
        CoroutineScope(Dispatchers.IO).launch {
            SistemaPersonalRepository.get(this@SistemaPersonalApp).ajustes.migrarLegacySiCorresponde()
            if (FirebaseConfig.estaConfigurado) FirebaseIdentity.ensureAnonymousSignIn()
        }
        programarSyncPeriodico()
        programarLimpiezaScreenshots()
    }

    fun programarSyncAhora() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "sync_familia_ahora",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun programarSyncPeriodico() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_familia_periodico",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun programarLimpiezaScreenshots() {
        val request = PeriodicWorkRequestBuilder<ScreenshotCleanupWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "limpieza_screenshots",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
