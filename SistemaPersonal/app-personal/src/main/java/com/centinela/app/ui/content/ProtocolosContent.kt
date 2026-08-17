package com.centinela.app.ui.content

data class Protocolo(
    val numero: String,
    val titulo: String,
    val pasos: List<String>,
    val señalSalida: String,
    val avisoProfesional: Boolean = false
)

val PROTOCOLOS = listOf(
    Protocolo("01", "PROTOCOLO ANTIPROCRASTINACIÓN", listOf(
        "Detener inercia: nombrar la tarea con precisión.",
        "Identificar causa raíz (fatiga, miedo, aburrimiento, incertidumbre).",
        "Evaluar estado del operador.",
        "Reducir fricción: ejecutar la acción mínima viable de inicio."
    ), "Tarea iniciada y atención focalizada. Si reaparece la desviación, reiniciar ciclo."),

    Protocolo("02", "PROTOCOLO FALLO", listOf(
        "Reconocer desviación objetivamente, sin sobredimensionar.",
        "Analizar causa empírica.",
        "Ejecutar reparación si el entorno lo permite.",
        "Extraer aprendizaje sistemático.",
        "Reanudar flujo inmediatamente."
    ), "Fallo documentado, corrección ejecutada. Retorno a operaciones sin penalización mental."),

    Protocolo("03", "PROTOCOLO BLOQUEO", listOf(
        "Definir la decisión subyacente real.",
        "Aplicar matriz PUD.",
        "Discernir déficit de información vs. resistencia cognitiva.",
        "Seleccionar la alternativa más viable.",
        "Ejecutar la primera acción vinculada."
    ), "Decisión consolidada. Ejecución en marcha sin reevaluación hasta obtener nuevos datos."),

    Protocolo("04", "PROTOCOLO CRISIS", listOf(
        "Suspender decisiones críticas temporalmente.",
        "Disminuir activación (distanciamiento, respiración).",
        "Aplicar anclaje: separar hechos de interpretaciones.",
        "Garantizar seguridad (escalar a asistencia profesional si hay riesgo).",
        "Aguardar claridad cognitiva antes de operar."
    ), "Intensidad reducida, capacidad analítica restaurada.", avisoProfesional = true),

    Protocolo("05", "PROTOCOLO DESCANSO", listOf(
        "Monitorear indicadores de fatiga (sueño, energía, irritabilidad).",
        "Ante alerta, autorizar pausa sin condición de \"mérito\".",
        "Documentar 1-2 variables exitosas del ciclo semanal.",
        "Registrar aciertos como evidencia para la autoconfianza."
    ), "Recuperación ejecutada sin sesgo de culpa. Reanudación progresiva de protocolos estándar.")
)
