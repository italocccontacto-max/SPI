package com.centinela.app.ui.content

data class PasoPud(val clave: String, val etiqueta: String, val titulo: String, val pregunta: String)

val PASOS_PUD = listOf(
    PasoPud("importa", "Importa", "¿Importa?", "¿Vale la pena gastar tiempo pensando esto?"),
    PasoPud("entiende", "Entiende", "Entiende.", "¿Cuál es el problema? ¿Qué sé y qué estoy suponiendo?"),
    PasoPud("explora", "Explora", "Explora.", "¿Qué opciones tengo? ¿Hay una tercera opción?"),
    PasoPud("filtra", "Filtra", "Filtra.", "¿Qué opciones respetan mi Constitución y mi Dirección?"),
    PasoPud("evalua", "Evalúa", "Evalúa.", "¿Qué gano, qué pierdo y qué tan reversible es?"),
    PasoPud("actua", "Actúa", "Actúa.", "Elige. Ejecuta. Aprende. Ajusta.")
)

const val PUD_PRINCIPIO_PROPORCIONALIDAD = "Principio de proporcionalidad: no todas las decisiones merecen el mismo " +
    "nivel de análisis. Cuanto mayor sea el impacto, la incertidumbre o la irreversibilidad de una decisión, mayor " +
    "deberá ser el rigor del proceso."

const val PUD_CITA = "Una buena decisión no garantiza un buen resultado; garantiza haber actuado de forma razonable " +
    "con la información disponible y conservar la capacidad de corregir el rumbo cuando la realidad lo exija."
