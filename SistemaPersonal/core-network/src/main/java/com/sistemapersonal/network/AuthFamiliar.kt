package com.sistemapersonal.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthFamiliar {
    private val auth get() = FirebaseAuth.getInstance()

    suspend fun iniciarSesion(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun refrescarToken(): Result<Unit> = try {
        auth.currentUser?.getIdToken(true)?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun cerrarSesion() = auth.signOut()
    fun usuarioActual() = auth.currentUser
}
