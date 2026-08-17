package com.sistemapersonal.model

enum class Modulo(val id: String, val label: String, val theme: AccentTheme, val roman: String?) {
    INICIO("inicio", "Inicio", AccentTheme.AMBER, null),
    CONSTITUCION("constitucion", "Constitución", AccentTheme.AMBER, "I"),
    IDENTIDAD("identidad", "Identidad", AccentTheme.BLUE, "II"),
    ANTIIDENTIDAD("antiidentidad", "Antiidentidad", AccentTheme.RED, "III"),
    DIRECCION("direccion", "Dirección", AccentTheme.MINT, "IV"),
    OBJETIVOS("objetivos", "Objetivos", AccentTheme.TEAL, "V"),
    PUD("pud", "PUD", AccentTheme.PURPLE, "VI"),
    PROTOCOLOS("protocolos", "Protocolos", AccentTheme.AMBER, "VII"),
    EJECUCION("ejecucion", "Ejecución", AccentTheme.AMBER, "VIII"),
    EVOLUCION("evolucion", "Evolución", AccentTheme.NEON, "IX"),
    BIBLIOTECA("biblioteca", "Biblioteca", AccentTheme.AMBER, "X"),
    GUARDIAN("guardian", "Guardián", AccentTheme.CYAN, null);
}

enum class AccentTheme { AMBER, CYAN, BLUE, RED, MINT, TEAL, PURPLE, NEON }
