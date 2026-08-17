package com.sistemapersonal.network

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

class FirebaseBridge(context: Context, private val familyId: String) {
    private val tag = "FirebaseBridge"
    private val db: FirebaseDatabase? by lazy {
        if (!FirebaseConfig.estaConfigurado) null else Firebase.database
    }

    private suspend fun authenticatedDatabase(): FirebaseDatabase? {
        if (!FirebaseConfig.estaConfigurado) return null
        val auth = FirebaseIdentity.ensureAnonymousSignIn()
        if (auth.isFailure) return null
        return db
    }

    suspend fun publicarEvento(evento: EventoRemoto): Boolean {
        val database = authenticatedDatabase() ?: return false
        return runCatching {
            database.getReference("familias/$familyId/eventos").push().setValue(
                mapOf(
                    "tipo" to evento.tipo,
                    "timestamp" to evento.timestamp,
                    "resumen" to evento.resumen,
                    "extra" to evento.extra
                )
            ).await()
            true
        }.getOrElse {
            Log.w(tag, "No se pudo publicar evento tipo=${evento.tipo}", it)
            false
        }
    }

    suspend fun actualizarEstadoActual(resumen: Map<String, Any?>): Boolean {
        val database = authenticatedDatabase() ?: return false
        return runCatching {
            database.getReference("familias/$familyId/estado_actual").setValue(resumen).await()
            true
        }.getOrElse {
            Log.w(tag, "No se pudo actualizar estado_actual", it)
            false
        }
    }

    fun referenciaEventos() = db?.getReference("familias/$familyId/eventos")
    fun referenciaEstadoActual() = db?.getReference("familias/$familyId/estado_actual")
}
