package com.sistemapersonal.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object FirebaseIdentity {
    private const val TAG = "FirebaseIdentity"

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    suspend fun ensureAnonymousSignIn(): Result<String> = try {
        auth.currentUser?.let { user ->
            if (user.isAnonymous) return Result.success(user.uid)
            auth.signOut()
        }
        val result = auth.signInAnonymously().await()
        val user = result.user ?: error("Firebase Auth no devolvió usuario")
        Result.success(user.uid)
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo autenticar anónimamente", e)
        Result.failure(e)
    }

    suspend fun refreshClaims(): Result<Unit> = try {
        auth.currentUser?.getIdToken(true)?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo refrescar el token de Firebase", e)
        Result.failure(e)
    }

    fun currentUid(): String? = auth.currentUser?.uid
}
