package com.centinela.app.guardian

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.centinela.app.DebtActivity
import com.centinela.app.InterruptActivity
import com.centinela.app.LockActivity
import com.centinela.app.WeeklyMirrorActivity
import com.centinela.app.admin.DeviceOwnerManager
import com.sistemapersonal.data.entity.ActivityLogEntity
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.domain.GuardianPolicy
import com.sistemapersonal.domain.RachaManager
import com.sistemapersonal.model.AccionGuardian
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GuardianService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var checkJob: Job? = null
    private val prefs by lazy { getSharedPreferences("centinela", Context.MODE_PRIVATE) }
    private val repo by lazy { SistemaPersonalRepository.get(applicationContext) }
    private val rachaManager by lazy { RachaManager(repo) }
    private val gamificacionEngine by lazy { com.sistemapersonal.domain.GamificacionEngine.get(repo) }
    private val diaFmt = SimpleDateFormat("yyyyMMdd", Locale.US)

    private var lastInterruptedApp: String?
        get() = prefs.getString("last_interrupted_app", null)
        set(v) = prefs.edit().putString("last_interrupted_app", v).apply()

    private var lastInterruptTime: Long
        get() = prefs.getLong("last_interrupt_time", 0L)
        set(v) = prefs.edit().putLong("last_interrupt_time", v).apply()

    private var blockedUntil: Long
        get() = prefs.getLong("blocked_until", 0L)
        set(v) = prefs.edit().putLong("blocked_until", v).apply()

    private var weeklyMirrorLaunchedThisSession = false

    companion object {
        const val CHANNEL_ID = "centinela_guardian"
        const val NOTIFICATION_ID = 1
        const val CHECK_INTERVAL_MS = 5_000L
        const val COOLDOWN_MS = 10 * 60 * 1000L
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch { verificarRootSiCorresponde() }
        startWatching()
    }

    private suspend fun verificarRootSiCorresponde() {
        val ultimaVerificacion = repo.ajustes.rootUltimaVerificacion.first()
        val seisHorasMs = 6 * 60 * 60 * 1000L
        if (System.currentTimeMillis() - ultimaVerificacion < seisHorasMs) return
        val resultado = RootChecker.verificar(applicationContext)
        repo.ajustes.registrarResultadoRoot(resultado.detectado, resultado.señales)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun startWatching() {
        checkJob = scope.launch {
            while (isActive) {
                checkWeeklyMirror()
                verificarRachaDiaria()
                checkUsage()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private fun marcarFalloHoy() {
        prefs.edit().putBoolean("fallo_${diaFmt.format(Date())}", true).apply()
    }

    private suspend fun verificarRachaDiaria() {
        val hoy = diaFmt.format(Date())
        val ultimoDiaEvaluado = prefs.getString("ultimo_dia_evaluado_racha", null)

        if (ultimoDiaEvaluado == null) {

            prefs.edit().putString("ultimo_dia_evaluado_racha", hoy).apply()
            return
        }
        if (ultimoDiaEvaluado == hoy) return

        val huboFallo = prefs.getBoolean("fallo_$ultimoDiaEvaluado", false)
        if (!huboFallo) {
            rachaManager.registrarDiaLimpio(ultimoDiaEvaluado)

            gamificacionEngine.otorgarPuntos(delta = 10, razon = "Día limpio ($ultimoDiaEvaluado)")
            val streakActual = repo.streakDao().obtener()
            if (streakActual != null) {
                gamificacionEngine.evaluarLogrosPorRacha(streakActual.diasConsecutivos)
            }
        }
        prefs.edit()
            .putString("ultimo_dia_evaluado_racha", hoy)
            .remove("fallo_$ultimoDiaEvaluado")
            .apply()
    }

    private fun checkWeeklyMirror() {
        if (weeklyMirrorLaunchedThisSession) return
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val isSunday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val lastMirror = prefs.getLong("last_weekly_mirror", 0L)
        val oneDayMs = 24 * 60 * 60 * 1000L

        if (isSunday && hour >= 10 && (now - lastMirror) > oneDayMs) {
            weeklyMirrorLaunchedThisSession = true
            prefs.edit().putLong("last_weekly_mirror", now).apply()
            startActivity(Intent(this, WeeklyMirrorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    private data class EstadoUso(val paquete: String, val minutosSesion: Long, val esSesionContinuaReal: Boolean)

    private fun detectarUso(userBlockedApps: Set<String>, usageThresholdMs: Long): EstadoUso? {
        val now = System.currentTimeMillis()

        val fgPaquete = prefs.getString(AccessibilityGuardian.KEY_FG_PACKAGE, null)
        val fgDesde = prefs.getLong(AccessibilityGuardian.KEY_FG_SINCE, 0L)
        val accesibilidadActiva = prefs.getBoolean(AccessibilityGuardian.KEY_ACCESSIBILITY_ACTIVA, false)

        val señalUtilizable = accesibilidadActiva && fgPaquete != null && fgPaquete in userBlockedApps &&
            fgDesde > 0L

        if (señalUtilizable) {
            val minutos = (now - fgDesde) / 60_000L
            return EstadoUso(fgPaquete!!, minutos, esSesionContinuaReal = true)
        }

        val usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val windowStart = now - (60 * 60 * 1000L)
        val stats = usageStats.queryUsageStats(UsageStatsManager.INTERVAL_BEST, windowStart, now)
        val topApp = stats
            ?.filter { it.packageName in userBlockedApps }
            ?.filter { it.lastTimeUsed >= (now - 10_000L) }
            ?.maxByOrNull { it.totalTimeInForeground }
            ?: return null

        val sessionStart = now - usageThresholdMs
        val recentStats = usageStats.queryUsageStats(UsageStatsManager.INTERVAL_BEST, sessionStart, now)
        val sessionTimeMs = recentStats
            ?.filter { it.packageName == topApp.packageName }
            ?.sumOf { it.totalTimeInForeground } ?: 0L

        return EstadoUso(topApp.packageName, sessionTimeMs / 60_000L, esSesionContinuaReal = false)
    }

    private suspend fun checkUsage() {
        val now = System.currentTimeMillis()

        val userBlockedApps = repo.ajustes.appsProhibidas.first()
        val thresholdMinutes = repo.ajustes.umbralMinutos.first()
        val usageThresholdMs = thresholdMinutes * 60 * 1000L
        val blockDurationMinutes = repo.ajustes.duracionBloqueoMinutos.first()
        val blockDurationMs = blockDurationMinutes * 60 * 1000L

        val estado = detectarUso(userBlockedApps, usageThresholdMs) ?: return

        val debtAction = repo.ajustes.debtAction.first()
        val debtPaidAt = prefs.getLong("debt_paid_at", 0L)
        val debtIsPending = debtAction.isNotBlank() && debtPaidAt < blockedUntil

        val bloqueoTotalRealmenteActivo = repo.ajustes.bloqueoTotalHabilitado.first() &&
            DeviceOwnerManager.esDeviceOwner(applicationContext)

        val accion = GuardianPolicy.decidir(
            paquete = estado.paquete,
            minutosSesionActual = estado.minutosSesion,
            umbralMinutos = thresholdMinutes,
            bloqueoTotalHabilitado = bloqueoTotalRealmenteActivo,
            yaBloqueadoHasta = blockedUntil,
            ahora = now,
            duracionBloqueoMs = blockDurationMs,
            deudaPendiente = debtIsPending
        )

        when (accion) {
            is AccionGuardian.Ninguna -> return

            is AccionGuardian.Deuda -> {
                startActivity(Intent(this, DebtActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("distraction_minutes", estado.minutosSesion)
                })
            }

            is AccionGuardian.BloqueoTotal -> {
                registrarActividad(estado.paquete, now - estado.minutosSesion * 60_000L, estado.minutosSesion * 60_000L)
                marcarFalloHoy()
                scope.launch { rachaManager.romperRacha() }

                prefs.edit().putLong("lock_until", blockedUntil).apply()
                startActivity(Intent(this, LockActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
            }

            is AccionGuardian.Interrumpir -> {

                triggerInterrupt(estado.paquete, estado.minutosSesion * 60_000L, estado.minutosSesion * 60_000L)
            }

            is AccionGuardian.EjercicioForzado -> {
                val cooldownExpired = (now - lastInterruptTime) > COOLDOWN_MS
                val isDifferentApp = estado.paquete != lastInterruptedApp
                if (isDifferentApp || cooldownExpired) {
                    blockedUntil = now + blockDurationMs
                    lastInterruptedApp = estado.paquete
                    lastInterruptTime = now
                    registrarActividad(estado.paquete, now - estado.minutosSesion * 60_000L, estado.minutosSesion * 60_000L)
                    startActivity(Intent(this, ExerciseInterruptionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("instruccion", accion.instruccion.uppercase())
                        putExtra("tipo", "flexiones")
                        putExtra("cantidad", 10)
                        putExtra("segundos", accion.segundos)
                    })
                }
            }
        }
    }

    private fun triggerInterrupt(packageName: String, timeMs: Long, sessionMs: Long) {
        val sessionStartMs = System.currentTimeMillis() - sessionMs
        registrarActividad(packageName, sessionStartMs, sessionMs)
        marcarFalloHoy()
        scope.launch { rachaManager.romperRacha() }
        val intent = Intent(this, InterruptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("package_name", packageName)
            putExtra("time_ms", timeMs)
            putExtra("session_start_ms", sessionStartMs)
        }
        startActivity(intent)
    }

    private fun registrarActividad(packageName: String, startMs: Long, durationMs: Long) {
        scope.launch {
            val prohibidas = repo.ajustes.appsProhibidas.first()
            repo.activityDao().insertar(
                ActivityLogEntity(
                    appPackage = packageName,
                    appName = packageName.substringAfterLast('.'),
                    timestampStart = startMs,
                    timestampEnd = startMs + durationMs,
                    durationMs = durationMs,
                    fecha = diaFmt.format(Date(startMs)),
                    esAppProhibida = packageName in prohibidas
                )
            )
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Guardián Activo",
            NotificationManager.IMPORTANCE_MIN).apply {
            description = "CENTINELA vigilando en segundo plano"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("CENTINELA")
        .setContentText("Guardián activo")
        .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setOngoing(true)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        checkJob?.cancel()
        scope.cancel()
    }
}
