package com.sistemapersonal.network

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await

class FirebaseFamilyApi {
    private val functions get() = Firebase.functions

    private suspend fun refreshClaimsOrThrow() {
        FirebaseIdentity.refreshClaims().getOrThrow()
    }

    suspend fun crearFamilia(): Result<FamilyProvisioning> = try {
        FirebaseIdentity.ensureAnonymousSignIn().getOrThrow()
        val result = functions.getHttpsCallable("createFamily").call().await()
        val data = result.data as? Map<*, *> ?: error("Respuesta inválida de createFamily")
        val familyId = data["familyId"] as? String ?: error("Falta familyId")
        val pairingCode = data["pairingCode"] as? String ?: error("Falta pairingCode")
        val expiresAt = (data["expiresAt"] as? Number)?.toLong()
        refreshClaimsOrThrow()
        Result.success(FamilyProvisioning(familyId, pairingCode, expiresAt))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun generarCodigoVinculacion(): Result<PairingCode> = try {
        FirebaseIdentity.ensureAnonymousSignIn().getOrThrow()
        refreshClaimsOrThrow()
        val result = functions.getHttpsCallable("createPairingCode").call().await()
        val data = result.data as? Map<*, *> ?: error("Respuesta inválida de createPairingCode")
        val pairingCode = data["pairingCode"] as? String ?: error("Falta pairingCode")
        val expiresAt = (data["expiresAt"] as? Number)?.toLong() ?: error("Falta expiresAt")
        Result.success(PairingCode(pairingCode, expiresAt))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun vincularPorCodigo(codigo: String): Result<String> = try {
        require(codigo.trim().length == 12) { "El código debe tener 12 caracteres" }
        val clean = codigo.trim().uppercase()
        val result = functions.getHttpsCallable("redeemPairingCode")
            .call(mapOf("pairingCode" to clean))
            .await()
        val data = result.data as? Map<*, *> ?: error("Respuesta inválida de redeemPairingCode")
        val familyId = data["familyId"] as? String ?: error("Falta familyId")
        refreshClaimsOrThrow()
        Result.success(familyId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun desvincular(): Result<Unit> = try {
        functions.getHttpsCallable("leaveFamily").call().await()
        refreshClaimsOrThrow()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

data class FamilyProvisioning(
    val familyId: String,
    val pairingCode: String,
    val expiresAt: Long? = null
)

data class PairingCode(
    val pairingCode: String,
    val expiresAt: Long
)
