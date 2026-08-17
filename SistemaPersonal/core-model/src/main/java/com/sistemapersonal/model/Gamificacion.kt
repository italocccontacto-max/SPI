package com.sistemapersonal.model

data class RachaEstado(
    val diasConsecutivos: Int,
    val fechaInicio: String,
    val rota: Boolean,
    val mejorRacha: Int
)

data class Logro(
    val clave: String,
    val titulo: String,
    val descripcion: String,
    val desbloqueadoEn: Long?
)

object CatalogoLogros {
    val BASE = listOf(
        Logro("racha_7", "PRIMERA SEMANA", "7 días consecutivos cumpliendo tus límites.", null),
        Logro("racha_30", "30 DÍAS DE RACHA", "Un mes entero de constancia.", null),
        Logro("estudio_1h", "1 HORA DE ESTUDIO CONSECUTIVO", "Un bloque de estudio ininterrumpido.", null),
        Logro("lectura_5d", "LECTURA MATUTINA x5", "5 días seguidos de lectura al despertar.", null),
        Logro("primer_dia_sin", "PRIMER DÍA LIMPIO", "Un día completo dentro de tus límites de uso.", null)
    )
}
