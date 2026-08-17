package com.sistemapersonal.network

data class EventoRemoto(
    val tipo: String,
    val timestamp: Long,
    val resumen: String,
    val extra: Map<String, Any?> = emptyMap()
)
