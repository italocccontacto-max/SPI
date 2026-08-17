package com.sistemapersonal.data.repo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.sistemapersonal.data.db.SistemaPersonalDatabase
import com.sistemapersonal.data.dao.*
import com.sistemapersonal.data.files.ScreenshotFileStore
import com.sistemapersonal.data.prefs.AjustesDataStore

class SistemaPersonalRepository private constructor(context: Context) {
    private val db: SistemaPersonalDatabase = SistemaPersonalDatabase.get(context)
    val ajustes: AjustesDataStore = AjustesDataStore(context)
    val archivos: ScreenshotFileStore = ScreenshotFileStore(context)

    fun activityDao(): ActivityDao = db.activityDao()
    fun streakDao(): StreakDao = db.streakDao()
    fun screenshotDao(): ScreenshotDao = db.screenshotDao()
    fun pointsDao(): PointsDao = db.pointsDao()
    fun achievementDao(): AchievementDao = db.achievementDao()
    fun areaObjetivoDao(): AreaObjetivoDao = db.areaObjetivoDao()
    fun puntoObjetivoDao(): PuntoObjetivoDao = db.puntoObjetivoDao()
    fun microObjetivoDao(): MicroObjetivoDao = db.microObjetivoDao()
    fun indicadorDao(): IndicadorDao = db.indicadorDao()
    fun rutinaBloqueDao(): RutinaBloqueDao = db.rutinaBloqueDao()
    fun nutricionDao(): NutricionDao = db.nutricionDao()
    fun entrenamientoDao(): EntrenamientoDao = db.entrenamientoDao()
    fun despertarDao(): DespertarDao = db.despertarDao()
    fun cierreDiaDao(): CierreDiaDao = db.cierreDiaDao()
    fun revisionDao(): RevisionDao = db.revisionDao()
    fun bibliotecaDao(): BibliotecaDao = db.bibliotecaDao()
    fun evolucionDao(): EvolucionDao = db.evolucionDao()
    fun carpetaDao(): CarpetaDao = db.carpetaDao()
    fun etiquetaDao(): EtiquetaDao = db.etiquetaDao()
    fun intentionContractDao(): IntentionContractDao = db.intentionContractDao()
    fun pudDao(): PudDao = db.pudDao()

    suspend fun limpiarCapturasAntiguas(
        diasRetencion: Int = 14,
        maxBytes: Long = 250L * 1024L * 1024L
    ) = withContext(Dispatchers.IO) {
        val antesDe = System.currentTimeMillis() - diasRetencion.coerceAtLeast(1) * 24L * 60L * 60L * 1000L
        val dao = screenshotDao()
        val antiguas = dao.anterioresA(antesDe)
        antiguas.forEach { captura ->

            val existe = java.io.File(captura.filePath).exists()
            val borrado = if (existe) archivos.eliminar(captura.filePath) else true
            if (borrado) dao.eliminar(captura)
        }
        val exceso = archivos.eliminarExceso(maxBytes)
        if (exceso.isNotEmpty()) dao.eliminarPorPaths(exceso)
    }

    companion object {
        @Volatile private var INSTANCE: SistemaPersonalRepository? = null
        fun get(context: Context): SistemaPersonalRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SistemaPersonalRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
