package com.centinela.app.guardian

import android.content.Context
import android.os.Build
import java.io.File

object RootChecker {

    private val RUTAS_SU = listOf(
        "/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/sbin/su",
        "/system/bin/.ext/.su", "/system/usr/we-need-root/su-backup",
        "/system/xbin/mu", "/data/local/xbin/su", "/data/local/bin/su",
        "/data/local/su", "/su/bin/su"
    )

    private val PAQUETES_GESTION_ROOT = listOf(
        "com.topjohnwu.magisk", "eu.chainfire.supersu", "com.noshufou.android.su",
        "com.noshufou.android.su.elite", "com.koushikdutta.superuser",
        "com.thirdparty.superuser", "com.yellowes.su", "com.kingroot.kinguser",
        "com.kingo.root", "com.smedialink.oneclickroot", "com.zhiqupk.root.global",
        "com.alephzain.framaroot"
    )

    data class ResultadoRoot(
        val detectado: Boolean,
        val señales: List<String>,
        val verificadoEn: Long
    )

    fun verificar(context: Context): ResultadoRoot {
        val señales = mutableListOf<String>()

        if (tieneTestKeys()) señales += "build tags contienen test-keys"
        rutaSuEncontrada()?.let { señales += "binario su presente en $it" }
        if (comandoSuEjecutable()) señales += "el comando 'su' respondió correctamente"
        paqueteGestionRootInstalado(context)?.let { señales += "app de gestión de root instalada: $it" }
        if (directorioSistemaEscribible()) señales += "/system aparenta ser escribible"

        return ResultadoRoot(
            detectado = señales.isNotEmpty(),
            señales = señales,
            verificadoEn = System.currentTimeMillis()
        )
    }

    private fun tieneTestKeys(): Boolean =
        Build.TAGS?.contains("test-keys") == true

    private fun rutaSuEncontrada(): String? =
        RUTAS_SU.firstOrNull { File(it).exists() }

    private fun comandoSuEjecutable(): Boolean = try {
        val proceso = ProcessBuilder("su", "-c", "id").redirectErrorStream(true).start()
        val salida = proceso.inputStream.bufferedReader().readText()
        val terminoOk = proceso.waitFor(1500, java.util.concurrent.TimeUnit.MILLISECONDS)
        terminoOk && salida.contains("uid=0")
    } catch (e: Exception) {
        false
    }

    private fun paqueteGestionRootInstalado(context: Context): String? {
        val pm = context.packageManager
        return PAQUETES_GESTION_ROOT.firstOrNull { pkg ->
            try { pm.getPackageInfo(pkg, 0); true } catch (e: Exception) { false }
        }
    }

    private fun directorioSistemaEscribible(): Boolean = try {
        val testFile = File("/system/._sp_root_test")
        val creado = testFile.createNewFile()
        if (creado) testFile.delete()
        creado
    } catch (e: Exception) {
        false
    }
}
