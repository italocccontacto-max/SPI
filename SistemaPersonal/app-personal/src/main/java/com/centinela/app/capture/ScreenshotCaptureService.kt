package com.centinela.app.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sistemapersonal.data.entity.ScreenshotEntity
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ScreenshotCaptureService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureJob: Job? = null

    companion object {
        const val CHANNEL_ID = "sistema_personal_capture"
        const val NOTIFICATION_ID = 2
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA_INTENT = "data_intent"
        const val EXTRA_INTERVAL_SEG = "intervalo_seg"
        const val ACTION_STOP = "com.centinela.app.capture.STOP"

        fun crearIntentDetener(context: Context): Intent =
            Intent(context, ScreenshotCaptureService::class.java).apply { action = ACTION_STOP }

        fun crearIntent(context: Context, resultCode: Int, data: Intent, intervaloSeg: Int): Intent =
            Intent(context, ScreenshotCaptureService::class.java).apply {
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_DATA_INTENT, data)
                putExtra(EXTRA_INTERVAL_SEG, intervaloSeg)
            }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent == null) { stopSelf(); return START_NOT_STICKY }

        startForeground(NOTIFICATION_ID, buildNotification(), notificationType())

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val data = intent.getParcelableExtraCompat(EXTRA_DATA_INTENT)
        val intervaloSeg = intent.getIntExtra(EXTRA_INTERVAL_SEG, 60)

        if (data == null) { stopSelf(); return START_NOT_STICKY }

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = projectionManager.getMediaProjection(resultCode, data)
        if (projection == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        detenerCaptura()
        mediaProjection = projection

        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {

                detenerCaptura()
                scope.launch {
                    runCatching { SistemaPersonalRepository.get(applicationContext).ajustes.setScreenshotsHabilitado(false) }
                }
                stopSelf()
            }
        }, null)

        iniciarVirtualDisplay(projection, intervaloSeg.coerceAtLeast(5))
        return START_STICKY
    }

    private fun iniciarVirtualDisplay(projection: MediaProjection, intervaloSeg: Int) {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val density = resources.displayMetrics.densityDpi

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        virtualDisplay = projection.createVirtualDisplay(
            "SistemaPersonalCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface, null, null
        )

        captureJob = scope.launch {
            val repo = SistemaPersonalRepository.get(applicationContext)
            while (isActive) {
                if (!repo.ajustes.screenshotsHabilitado.first()) {
                    stopSelf()
                    break
                }
                delay(intervaloSeg * 1000L)
                if (!repo.ajustes.screenshotsHabilitado.first()) {
                    stopSelf()
                    break
                }
                try {
                    capturarFrame(reader, width, height, repo)
                } catch (e: Exception) {
                    Log.e("ScreenshotCapture", "Error capturando frame", e)
                }
            }
        }
    }

    private suspend fun capturarFrame(reader: ImageReader, width: Int, height: Int, repo: SistemaPersonalRepository) {
        val image = reader.acquireLatestImage() ?: return
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width

            var bitmap: Bitmap? = null
            var recortado: Bitmap? = null
            var file: java.io.File? = null
            try {
                bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                recortado = Bitmap.createBitmap(bitmap, 0, 0, width, height)

                val timestamp = System.currentTimeMillis()
                file = repo.archivos.guardar(recortado, timestamp)
                val esRelevante = appEnPrimerPlanoProhibida(repo)

                repo.screenshotDao().insertar(
                    ScreenshotEntity(
                        timestamp = timestamp,
                        filePath = file.absolutePath,
                        width = width,
                        height = height,
                        sizeBytes = file.length(),
                        uploaded = false,
                        relevante = esRelevante
                    )
                )
            } catch (e: Exception) {
                Log.e("ScreenshotCapture", "Error guardando screenshot; se elimina el archivo recién creado", e)
                file?.let { runCatching { it.delete() } }
                throw e
            } finally {
                bitmap?.recycle()
                recortado?.recycle()
            }
        } finally {
            image.close()
        }
    }

    private suspend fun appEnPrimerPlanoProhibida(repo: SistemaPersonalRepository): Boolean {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val ahora = System.currentTimeMillis()
            val eventos = usm.queryEvents(ahora - 10_000, ahora)
            var ultimoPaquete: String? = null
            val evento = android.app.usage.UsageEvents.Event()
            while (eventos.hasNextEvent()) {
                eventos.getNextEvent(evento)
                if (evento.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    ultimoPaquete = evento.packageName
                }
            }
            val prohibidas = repo.ajustes.appsProhibidas.first()
            ultimoPaquete != null && ultimoPaquete in prohibidas
        } catch (e: Exception) {
            Log.w("ScreenshotCapture", "No se pudo determinar la app en primer plano", e)
            false
        }
    }

    private fun detenerCaptura() {
        captureJob?.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerCaptura()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Captura de pantalla",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Sesión de captura periódica activa" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SISTEMA PERSONAL")
            .setContentText("Captura de pantalla activa")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

    private fun notificationType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        else 0
}

private fun Intent.getParcelableExtraCompat(key: String): Intent? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(key, Intent::class.java)
    else
        @Suppress("DEPRECATION") getParcelableExtra(key)
