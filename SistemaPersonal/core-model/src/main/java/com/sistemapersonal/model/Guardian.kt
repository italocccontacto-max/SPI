package com.sistemapersonal.model

enum class NivelBloqueo {

    SUAVE,

    TOTAL
}

data class EstadoRoot(val disponible: Boolean, val verificadoEn: Long)

sealed interface AccionGuardian {
    data object Ninguna : AccionGuardian
    data class Interrumpir(val paquete: String, val minutosSesion: Long) : AccionGuardian
    data class EjercicioForzado(val paquete: String, val segundos: Int, val instruccion: String) : AccionGuardian
    data class BloqueoTotal(val paquete: String, val hastaMillis: Long) : AccionGuardian
    data class Deuda(val minutosPendientes: Long) : AccionGuardian
}
