package com.sistemapersonal.model

enum class EjecucionTab(val id: String, val label: String) {
    HOY("hoy", "Hoy"),
    RUTINAS("rutinas", "Rutinas"),
    NUTRICION("nutricion", "Nutrición"),
    ENTRENAMIENTO("entrenamiento", "Entrenamiento"),
    DESVIACION("desviacion", "Desviación"),
    DESPERTAR("despertar", "Al Despertar"),
    CIERRE("cierre", "Cierre del Día"),
    REVISION("revision", "Revisión Semanal"),
    ESTADISTICAS("estadisticas", "Estadísticas")
}

enum class EvolucionTab(val id: String, val label: String) {
    EVENTOS("eventos", "Eventos"),
    COMPARAR("comparar", "Comparar"),
    PAPELERA("papelera", "Papelera"),
    CARPETAS("carpetas", "Carpetas y etiquetas")
}