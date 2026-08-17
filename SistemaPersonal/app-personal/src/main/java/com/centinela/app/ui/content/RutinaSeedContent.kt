package com.centinela.app.ui.content

data class BloqueSeed(val inicio: String, val fin: String, val etiqueta: String)

val SEED_ENTRE_SEMANA = listOf(
    BloqueSeed("06:20", "06:50", "Levantarse → barra, dientes, cara, agua"),
    BloqueSeed("06:50", "07:30", "Prepararme"),
    BloqueSeed("07:30", "14:00", "Escuela"),
    BloqueSeed("10:45", "11:15", "Desayuno"),
    BloqueSeed("14:00", "14:30", "Parque (imaginar, reflexionar, snack)"),
    BloqueSeed("14:30", "14:50", "Sesión de movilidad y flexibilidad"),
    BloqueSeed("14:50", "15:10", "Baño"),
    BloqueSeed("15:10", "15:50", "Almuerzo e higiene facial y dental"),
    BloqueSeed("15:50", "16:20", "Caminata"),
    BloqueSeed("16:20", "17:20", "Calistenia"),
    BloqueSeed("17:20", "17:40", "Baño post entrenamiento"),
    BloqueSeed("17:40", "18:40", "Estudio / limpieza"),
    BloqueSeed("18:40", "18:50", "Oscuras (música)"),
    BloqueSeed("18:50", "19:50", "Cenar (19:00)"),
    BloqueSeed("19:50", "20:10", "Higiene facial y dental"),
    BloqueSeed("20:10", "20:40", "Caminata"),
    BloqueSeed("20:40", "21:00", "Preparar mi día siguiente"),
    BloqueSeed("21:00", "21:10", "Ensayo neuromuscular"),
    BloqueSeed("21:10", "21:30", "Cama, sin teléfono"),
    BloqueSeed("21:30", "23:59", "Dormir")
)

val SEED_FIN_SEMANA = listOf(
    BloqueSeed("07:30", "08:00", "Levantarse → barra, dientes, cara, agua"),
    BloqueSeed("08:00", "08:40", "Desayuno"),
    BloqueSeed("09:00", "10:30", "Calistenia"),
    BloqueSeed("10:30", "11:00", "Baño post entrenamiento"),
    BloqueSeed("11:00", "13:00", "Proyectos personales"),
    BloqueSeed("13:30", "14:15", "Almuerzo"),
    BloqueSeed("15:00", "17:00", "Tareas / Limpieza / Proyectos"),
    BloqueSeed("17:50", "18:40", "Tareas / Limpieza / Proyectos"),
    BloqueSeed("18:40", "18:50", "A oscuras"),
    BloqueSeed("18:50", "19:50", "Cenar"),
    BloqueSeed("19:50", "20:10", "Higiene facial y dental"),
    BloqueSeed("20:40", "21:00", "Preparar mi día siguiente"),
    BloqueSeed("21:10", "21:30", "Cama, sin teléfono"),
    BloqueSeed("21:30", "23:59", "Dormir")
)

const val TIPO_ENTRE_SEMANA = "entre_semana"
const val TIPO_FIN_SEMANA = "fin_semana"
