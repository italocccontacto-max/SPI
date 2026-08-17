package com.centinela.app.ui.content

data class EjeDireccion(val numero: String, val titulo: String, val cuerpo: String, val rumbo: String, val porcentaje: Int)

val DIRECCION_EJES = listOf(
    EjeDireccion("01", "SALUD",
        "Mantener un cuerpo capaz de sostener la vida que quiero construir.",
        "La salud no es un premio. Es infraestructura.", 86),
    EjeDireccion("02", "CARÁCTER",
        "Desarrollar una personalidad capaz de actuar conforme a principios incluso cuando resulte incómodo.",
        "El carácter reduce la distancia entre intención y acción.", 78),
    EjeDireccion("03", "CONOCIMIENTO",
        "Aprender continuamente para comprender mejor la realidad.",
        "No acumular información. Reducir errores de interpretación.", 74),
    EjeDireccion("04", "CAPACIDAD",
        "Convertir conocimiento en habilidades útiles. Incrementar la autonomía mediante competencia.",
        "Cada habilidad adquirida amplía las posibilidades futuras.", 70),
    EjeDireccion("05", "PROPÓSITO",
        "Construir deliberadamente una vida propia en lugar de aceptar pasivamente una vida por defecto.",
        "Cada decisión debe acercarme a la vida que elegí construir.", 72),
    EjeDireccion("06", "VÍNCULOS",
        "Construir relaciones compatibles con la vida que deseo.",
        "Alejarme progresivamente de vínculos que destruyan mi dirección.", 68),
    EjeDireccion("07", "BIENESTAR MENTAL",
        "Aprender a regular pensamientos, emociones e impulsos sin depender constantemente de la evasión.",
        "La estabilidad interior permite sostener el rumbo cuando las circunstancias cambian.", 64)
)

val DIRECCION_PRINCIPIOS = listOf(
    "No tomo decisiones únicamente porque sean posibles, cómodas o atractivas.",
    "Las tomo considerando si fortalecen o debilitan la vida que he decidido construir.",
    "Mi dirección no depende de emociones momentáneas.",
    "Es el criterio que mantiene alineados mis principios, mi identidad y mis acciones a lo largo del tiempo."
)

val DIRECCION_EXITO = listOf("Lo que considero correcto.", "Lo que decido.", "Lo que hago.", "La persona en la que me convierto.")

val DIRECCION_VISION = listOf(
    "mi cuerpo me permita actuar;", "mi carácter inspire confianza;", "mis conocimientos aumenten mi libertad;",
    "mis capacidades produzcan valor;", "mis relaciones sean compatibles con mis principios;",
    "y mi estabilidad interior no dependa constantemente de las circunstancias."
)

val DIRECCION_DECLARACION_FINAL = listOf(
    "No controlo el futuro, ni las circunstancias, ni los resultados.",
    "Pero sí puedo controlar el rumbo desde el que tomo mis decisiones.",
    "Mientras conserve ese rumbo, incluso los errores forman parte del avance."
)
