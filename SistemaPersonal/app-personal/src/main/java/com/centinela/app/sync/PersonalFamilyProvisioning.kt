package com.centinela.app.sync

import android.content.Context
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.network.FirebaseConfig
import com.sistemapersonal.network.FirebaseFamilyApi
import com.sistemapersonal.network.FirebaseIdentity
import com.sistemapersonal.network.FamilyProvisioning
import com.sistemapersonal.network.PairingCode
import kotlinx.coroutines.flow.first

object PersonalFamilyProvisioning {
    suspend fun ensure(context: Context): Result<FamilyProvisioning> {
        if (!FirebaseConfig.estaConfigurado) {
            return Result.failure(IllegalStateException("Firebase no está configurado"))
        }
        FirebaseIdentity.ensureAnonymousSignIn().getOrElse { return Result.failure(it) }
        val repo = SistemaPersonalRepository.get(context)
        val familyId = repo.ajustes.familyId.first()
        val pairingCode = repo.ajustes.pairingCode.first()
        val expiresAt = repo.ajustes.pairingCodeExpiresAt.first()

        if (!familyId.isNullOrBlank()) {
            return Result.success(FamilyProvisioning(familyId, pairingCode.orEmpty(), expiresAt))
        }

        return FirebaseFamilyApi().crearFamilia().onSuccess { provision ->
            repo.ajustes.guardarProvisionFamilia(provision.familyId, provision.pairingCode, provision.expiresAt)
        }
    }

    suspend fun renovarCodigo(context: Context): Result<PairingCode> {
        FirebaseIdentity.ensureAnonymousSignIn().getOrElse { return Result.failure(it) }
        val repo = SistemaPersonalRepository.get(context)
        repo.ajustes.familyId.first().takeUnless { it.isNullOrBlank() }
            ?: return Result.failure(IllegalStateException("La familia todavía no está creada"))
        return FirebaseFamilyApi().generarCodigoVinculacion().onSuccess { code ->
            repo.ajustes.actualizarCodigoVinculacion(code.pairingCode, code.expiresAt)
        }
    }
}
