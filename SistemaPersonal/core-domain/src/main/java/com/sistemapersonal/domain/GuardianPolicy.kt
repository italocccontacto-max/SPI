package com.sistemapersonal.domain

import com.sistemapersonal.model.AccionGuardian

object GuardianPolicy {

    fun decidir(
        paquete: String,
        minutosSesionActual: Long,
        umbralMinutos: Int,
        bloqueoTotalHabilitado: Boolean,
        yaBloqueadoHasta: Long,
        ahora: Long,
        duracionBloqueoMs: Long,
        deudaPendiente: Boolean
    ): AccionGuardian {

        if (ahora < yaBloqueadoHasta) {
            return when {
                bloqueoTotalHabilitado -> AccionGuardian.BloqueoTotal(paquete, yaBloqueadoHasta)
                deudaPendiente -> AccionGuardian.Deuda((yaBloqueadoHasta - ahora) / 60_000L)
                else -> AccionGuardian.Interrumpir(paquete, minutosSesionActual)
            }
        }

        if (minutosSesionActual >= umbralMinutos) {
            return AccionGuardian.EjercicioForzado(
                paquete = paquete,
                segundos = 20,
                instruccion = "Haz 10 flexiones"
            )
        }

        return AccionGuardian.Ninguna
    }
}
