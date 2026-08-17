package com.sistemapersonal.network

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.File

class StorageBridge(context: Context, private val familyId: String) {
    private val storage get() = if (FirebaseConfig.estaConfigurado) Firebase.storage else null

    suspend fun subirScreenshot(file: File, timestamp: Long): String? {
        if (FirebaseIdentity.ensureAnonymousSignIn().isFailure) return null
        val path = "familias/$familyId/screenshots/$timestamp.jpg"
        val ref = storage?.reference?.child(path) ?: return null
        return try {
            ref.putFile(android.net.Uri.fromFile(file)).await()
            path
        } catch (e: Exception) {
            Log.w("StorageBridge", "No se pudo subir captura", e)
            null
        }
    }
}
