package com.centinela.app.ui.content

data class AreaAntiidentidad(val numero: String, val titulo: String, val items: List<String>)

val ANTIIDENTIDAD_AREAS = listOf(
    AreaAntiidentidad("01", "CUERPO Y ENERGÍA", listOf(
        "Sedentarismo prolongado.", "Descuidar la actividad física.", "Alimentarme por comodidad.",
        "Sacrificar el sueño por distracciones.", "Vivir agotado sin investigar causas.",
        "Excederme con azúcar o estímulos.", "Usar pantallas cerca de dormir."
    )),
    AreaAntiidentidad("02", "MENTE, ATENCIÓN E INFORMACIÓN", listOf(
        "Contenido corto compulsivo.", "Estímulos fáciles para evitar emociones.", "Entretenimiento que desplaza lo importante.",
        "Dejar que el algoritmo decida.", "Adoptar ideas sin cuestionar.", "Repetir opiniones ajenas.",
        "Defender creencias por identidad.", "Confundir información con comprensión.", "Usar plataformas sin límite consciente."
    )),
    AreaAntiidentidad("03", "ACTITUD ANTE LA VIDA", listOf(
        "Victimizarme y culpar factores externos.", "Buscar excusas en lugar de soluciones.", "Compararme para sentirme superior.",
        "Ignorar lo que tengo por lo que falta.", "Usar la gratitud como sustituto de la mejora."
    )),
    AreaAntiidentidad("04", "RECURSOS", listOf(
        "Consumir recursos sin considerar el futuro.", "Gastar impulsivamente.", "Dejar de desarrollar capacidades útiles.",
        "Usar recursos sin dirección clara."
    )),
    AreaAntiidentidad("05", "RELACIONES Y ENTORNO", listOf(
        "Rodearme de estancamiento.", "Permitir que otros definan mi tiempo o dirección.", "Aislarme de relaciones saludables.",
        "Mantener relaciones perjudiciales por miedo o comodidad."
    )),
    AreaAntiidentidad("06", "ACCIÓN Y DISCIPLINA", listOf(
        "Posponer el inicio constantemente.", "Actuar solo con motivación.", "Abandonar ante la primera dificultad.",
        "Sustituir acción por sobreanálisis.", "Usar perfeccionismo para evitar el error."
    )),
    AreaAntiidentidad("07", "MIEDO Y EGO", listOf(
        "Evitar situaciones incómodas por miedo al juicio.", "No intentar nada para evitar fracasar.",
        "Defender el orgullo antes que mejorar.", "Confundir evitar el fracaso con éxito."
    )),
    AreaAntiidentidad("08", "VIDA INTERIOR", listOf(
        "Vivir en automático.", "No revisar si mis acciones tienen sentido.", "Aferrarme al pasado.",
        "Usar consumo o distracción para evitar emociones.", "Juzgarme con dureza extrema en lugar de observar patrones."
    ))
)

val ANTIIDENTIDAD_CONVIERTE = listOf(
    "La procrastinación en \"espera razonable\"", "El miedo en \"prudencia\"", "El ego en \"convicción\"",
    "La distracción en \"descanso\"", "La costumbre en \"identidad\"", "La intención sustituto de la acción"
)

val ANTIIDENTIDAD_SEÑALES = listOf(
    "Justificar lo que antes corregía.", "Defender errores evidentes.", "Actuar por impulso.",
    "Abandonar compromisos.", "Posponer decisiones incómodas.", "Evitar la evidencia.",
    "Creer que mañana será distinto sin cambiar hoy."
)

val ANTIIDENTIDAD_HERRAMIENTAS = listOf(
    "Racionalización", "Autoengaño", "Victimismo", "Perfeccionismo", "Orgullo",
    "Impulsividad", "Comodidad", "Distracción", "Gratificación inmediata", "Comparación constante"
)

val ANTIIDENTIDAD_RECUPERAR_CONTROL = listOf(
    "La nombro.", "La acepto sin justificarla.", "Identifico el principio vulnerado.", "Corrijo la acción.", "Continúo."
)

val ANTIIDENTIDAD_DECLARACION_FINAL = listOf(
    "La antiidentidad siempre existirá como posibilidad.",
    "No intento eliminarla. Intento impedir que gobierne mis decisiones.",
    "Cada vez que elijo un principio por encima del impulso, debilito la antiidentidad y fortalezco mi carácter."
)
