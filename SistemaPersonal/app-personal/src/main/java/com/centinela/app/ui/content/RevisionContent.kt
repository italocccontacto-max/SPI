package com.centinela.app.ui.content

data class PreguntaRevision(val id: String, val texto: String)
data class SeccionRevision(val numero: String, val titulo: String, val preguntas: List<PreguntaRevision>)

val REVISION_SECCIONES = listOf(
    SeccionRevision("I", "CONFRONTACIÓN Y DECISIONES", listOf(
        PreguntaRevision("q1", "¿En qué área de mi vida estoy evitando una conversación difícil o una decisión inevitable?"),
        PreguntaRevision("q2", "¿Cuál es el problema real que estoy evitando resolver?"),
        PreguntaRevision("q3", "¿Qué verdad me niego a aceptar porque implicaría cambios incómodos?")
    )),
    SeccionRevision("II", "EJECUCIÓN Y DISCIPLINA", listOf(
        PreguntaRevision("q4", "¿Hay algo que hice o dejé esta semana que hoy me genera fricción interna? ¿Qué haré para corregirlo?"),
        PreguntaRevision("q5", "¿Qué excusa repetí esta semana?"),
        PreguntaRevision("q6", "¿Cuál es el cuello de botella más importante?"),
        PreguntaRevision("q7", "¿Qué activo o habilidad estoy desperdiciando?")
    )),
    SeccionRevision("III", "CONDUCTA Y PATRONES", listOf(
        PreguntaRevision("q8", "¿Qué patrón negativo repetí otra vez?"),
        PreguntaRevision("q9", "¿Qué comportamiento toleré que contradice mis estándares?"),
        PreguntaRevision("q10", "¿Dónde falló mi inteligencia emocional?"),
        PreguntaRevision("q11", "¿Qué energía estoy desperdiciando intentando controlar algo que no depende de mí?"),
        PreguntaRevision("q12", "¿Qué hice realmente bien esta semana?")
    ))
)

val REVISION_SINTESIS = listOf(
    PreguntaRevision("q13", "¿Qué funcionó?"),
    PreguntaRevision("q14", "¿Qué falló repetidamente?"),
    PreguntaRevision("q15", "¿Qué obstáculo apareció con más frecuencia?")
)

const val REVISION_Q16 = "¿El problema fue mi conducta o el diseño del sistema?"
val REVISION_Q16_OPCIONES = listOf("conducta" to "MI CONDUCTA", "sistema" to "EL DISEÑO DEL SISTEMA", "ambos" to "AMBOS")

val REVISION_SINTESIS2 = listOf(
    PreguntaRevision("q17", "¿Qué cambio concreto hará la próxima semana?"),
    PreguntaRevision("q18", "¿Qué no voy a cambiar todavía?")
)
