package com.sistemapersonal.data.files

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

class ScreenshotFileStore(private val context: Context) {

    private val dir: File by lazy {
        File(context.filesDir, "screenshots").apply { mkdirs() }
    }

    fun guardar(bitmap: Bitmap, timestamp: Long): File {
        val file = File(dir, "shot_$timestamp.jpg")
        try {
            FileOutputStream(file).use { out ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)) {
                    "No se pudo comprimir la captura en JPEG"
                }
            }
            return file
        } catch (e: Exception) {
            runCatching { file.delete() }
            throw e
        }
    }

    fun eliminar(path: String): Boolean = File(path).let { it.exists() && it.delete() }

    fun eliminarArchivosAnterioresA(antesDe: Long): Int {
        return dir.listFiles()?.count { f ->
            f.lastModified() < antesDe && f.delete()
        } ?: 0
    }

    fun eliminarExceso(maxBytes: Long): List<String> {
        val archivos = dir.listFiles()?.filter { it.isFile }?.sortedBy { it.lastModified() }.orEmpty()
        var total = archivos.sumOf { it.length() }
        val eliminados = mutableListOf<String>()
        for (file in archivos) {
            if (total <= maxBytes) break
            val size = file.length()
            if (file.delete()) {
                total -= size
                eliminados += file.absolutePath
            }
        }
        return eliminados
    }

    fun espacioUsadoBytes(): Long = dir.listFiles()?.sumOf { it.length() } ?: 0L

    fun copiarImagenExterna(context: Context, uri: android.net.Uri, prefijo: String): File? = try {
        val destino = File(File(context.filesDir, prefijo).apply { mkdirs() }, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            java.io.FileOutputStream(destino).use { output -> input.copyTo(output) }
        }
        destino
    } catch (e: Exception) {
        null
    }
}
