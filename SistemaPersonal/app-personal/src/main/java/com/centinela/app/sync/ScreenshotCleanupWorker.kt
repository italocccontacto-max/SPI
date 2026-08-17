package com.centinela.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sistemapersonal.data.repo.SistemaPersonalRepository

class ScreenshotCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        SistemaPersonalRepository.get(applicationContext).limpiarCapturasAntiguas()
        Result.success()
    }.getOrElse { Result.retry() }
}
