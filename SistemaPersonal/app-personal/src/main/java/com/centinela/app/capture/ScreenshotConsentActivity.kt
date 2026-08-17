package com.centinela.app.capture

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScreenshotConsentActivity : ComponentActivity() {

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            lifecycleScope.launch {
                val repo = SistemaPersonalRepository.get(applicationContext)
                val intervalo = repo.ajustes.intervaloScreenshotsSeg.first()
                repo.ajustes.setScreenshotsHabilitado(true)
                val serviceIntent = ScreenshotCaptureService.crearIntent(
                    applicationContext, result.resultCode, result.data!!, intervalo
                )
                androidx.core.content.ContextCompat.startForegroundService(applicationContext, serviceIntent)
                finish()
            }
        } else {
            lifecycleScope.launch {
                val context = applicationContext
                SistemaPersonalRepository.get(context).ajustes.setScreenshotsHabilitado(false)
                context.stopService(ScreenshotCaptureService.crearIntentDetener(context))
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(MediaProjectionManager::class.java)
        launcher.launch(manager.createScreenCaptureIntent())
    }
}
