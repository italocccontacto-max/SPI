package com.centinela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.content.*
import com.centinela.app.ui.hoyYyyyMMdd
import com.centinela.app.ui.rememberRepo
import com.centinela.app.ui.semanaIso
import com.sistemapersonal.data.entity.*
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.BarChartSP
import com.sistemapersonal.ui.components.DonutChart
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MOMENTOS = listOf("desayuno" to "DESAYUNO", "almuerzo" to "ALMUERZO", "snack" to "SNACK", "cena" to "CENA")

@Composable
fun NutricionTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val alimentosRepo = remember { com.centinela.app.nutricion.AlimentosRepository(context) }

    var offsetDias by remember { mutableStateOf(0) }
    val fecha = remember(offsetDias) { fechaConOffset(offsetDias) }
    val esHoy = offsetDias == 0

    val registros by repo.nutricionDao().delDia(fecha).collectAsState(initial = emptyList())
    val metas by repo.nutricionDao().observarMetas().collectAsState(initial = null)
    val metasReales = metas ?: NutricionMetasEntity()
    val agua by repo.nutricionDao().aguaDelDia(fecha).collectAsState(initial = null)
    val mlAgua = agua?.ml ?: 0
    val notasEntity by repo.nutricionDao().notasDelDia(fecha).collectAsState(initial = null)

    LaunchedEffect(Unit) {
        if (repo.nutricionDao().observarMetas().first() == null) {
            repo.nutricionDao().guardarMetas(NutricionMetasEntity())
        }
    }

    val totalKcal = registros.sumOf { it.kcal.toDouble() }.toFloat()
    val totalProteinas = registros.sumOf { it.proteinas.toDouble() }.toFloat()
    val totalCarbos = registros.sumOf { it.carbohidratos.toDouble() }.toFloat()
    val totalGrasas = registros.sumOf { it.grasas.toDouble() }.toFloat()
    val restantesKcal = (metasReales.kcal - totalKcal).coerceAtLeast(0f).toInt()

    var mostrarMetas by remember { mutableStateOf(false) }
    var mostrandoFormularioComida by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹ día anterior", color = Mint, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { offsetDias-- })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (esHoy) "HOY" else fecha, color = Ink0, style = MaterialTheme.typography.titleMedium)
                Text(fecha, color = Ink3, style = MaterialTheme.typography.labelSmall)
            }
            Text("día siguiente ›", color = if (esHoy) Ink3 else Mint, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable(enabled = !esHoy) { offsetDias++ })
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AngularPanel(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DonutChart(value = (totalKcal / metasReales.kcal).coerceIn(0f, 1f), color = Mint, label = "$restantesKcal")
                    Spacer(Modifier.height(6.dp))
                    Text("kcal restantes", color = Ink2, style = MaterialTheme.typography.labelSmall)
                    Text("Consumido ${totalKcal.toInt()} · Meta ${metasReales.kcal}", color = Ink3, style = MaterialTheme.typography.labelSmall)
                }
            }
            AngularPanel(modifier = Modifier.weight(1f)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilaMacro("Proteína", totalProteinas, metasReales.proteinas.toFloat(), "g", Mint)
                    FilaMacro("Carbohidratos", totalCarbos, metasReales.carbohidratos.toFloat(), "g", Mint)
                    FilaMacro("Grasas", totalGrasas, metasReales.grasas.toFloat(), "g", Mint)
                    FilaMacro("Hidratación", mlAgua.toFloat(), metasReales.hidratacionMl.toFloat(), "ml", Cyan)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(250, 500, 750).forEach { cantidad ->
                GlowButton("+${cantidad}ml", accent = Cyan, glow = CyanGlow, onClick = {
                    scope.launch { repo.nutricionDao().guardarAgua(AguaDiaEntity(fecha = fecha, ml = mlAgua + cantidad)) }
                })
            }
            Text("editar metas", color = Ink3, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterVertically).clickable { mostrarMetas = !mostrarMetas })
        }

        if (mostrarMetas) {
            Spacer(Modifier.height(10.dp))
            PanelMetas(metasReales) { nuevasMetas ->
                scope.launch { repo.nutricionDao().guardarMetas(nuevasMetas) }
            }
        }

        Spacer(Modifier.height(16.dp))

        MOMENTOS.forEach { (clave, etiqueta) ->
            val entradasMomento = registros.filter { it.momento == clave }
            AngularPanel(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            mostrandoFormularioComida = if (mostrandoFormularioComida == clave) null else clave
                        },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(etiqueta, color = Mint, style = MaterialTheme.typography.titleSmall)
                        Text("${entradasMomento.sumOf { it.kcal.toDouble() }.toInt()} kcal", color = Ink2, style = MaterialTheme.typography.labelSmall)
                    }
                    entradasMomento.forEach { r ->
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${r.nombreAlimento} (${r.gramos.toInt()}g)", color = Ink1, style = MaterialTheme.typography.bodySmall)
                            Text("${r.kcal.toInt()} kcal", color = Ink2, style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.clickable { scope.launch { repo.nutricionDao().eliminar(r) } })
                        }
                    }
                    if (mostrandoFormularioComida == clave) {
                        Spacer(Modifier.height(8.dp))
                        FormularioComida(alimentosRepo, fecha, clave, onGuardado = {
                            scope.launch { repo.nutricionDao().insertar(it) }
                        })
                    }
                }
            }
        }

        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("NOTAS DEL DÍA", color = Mint, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                var notas by remember(fecha, notasEntity) { mutableStateOf(notasEntity?.notas ?: "") }
                BasicTextField(
                    value = notas,
                    onValueChange = {
                        notas = it
                        scope.launch { repo.nutricionDao().guardarNotas(NutricionNotasEntity(fecha = fecha, notas = it)) }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    textStyle = TextStyle(color = Ink1),
                    cursorBrush = SolidColor(Mint)
                )
            }
        }
    }
}

private fun fechaConOffset(offsetDias: Int): String {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.DAY_OF_YEAR, offsetDias)
    return SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
}

@Composable
private fun FilaMacro(nombre: String, actual: Float, meta: Float, unidad: String, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nombre, color = Ink1, style = MaterialTheme.typography.labelSmall)
            Text("${actual.toInt()}/${meta.toInt()}$unidad", color = Ink3, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(2.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().height(4.dp).background(Hairline)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth((actual / meta).coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(color)
            )
        }
    }
}

@Composable
private fun PanelMetas(metas: NutricionMetasEntity, onGuardar: (NutricionMetasEntity) -> Unit) {
    var kcal by remember(metas) { mutableStateOf(metas.kcal.toString()) }
    var proteinas by remember(metas) { mutableStateOf(metas.proteinas.toString()) }
    var carbohidratos by remember(metas) { mutableStateOf(metas.carbohidratos.toString()) }
    var grasas by remember(metas) { mutableStateOf(metas.grasas.toString()) }
    var hidratacion by remember(metas) { mutableStateOf(metas.hidratacionMl.toString()) }

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("METAS DIARIAS", color = Mint, style = MaterialTheme.typography.titleSmall)
            CampoMeta("Kcal", kcal) { kcal = it }
            CampoMeta("Proteína (g)", proteinas) { proteinas = it }
            CampoMeta("Carbohidratos (g)", carbohidratos) { carbohidratos = it }
            CampoMeta("Grasas (g)", grasas) { grasas = it }
            CampoMeta("Hidratación (ml)", hidratacion) { hidratacion = it }
            GlowButton("GUARDAR METAS", accent = Mint, glow = MintGlow, onClick = {
                onGuardar(
                    NutricionMetasEntity(
                        kcal = kcal.toIntOrNull() ?: metas.kcal,
                        proteinas = proteinas.toIntOrNull() ?: metas.proteinas,
                        carbohidratos = carbohidratos.toIntOrNull() ?: metas.carbohidratos,
                        grasas = grasas.toIntOrNull() ?: metas.grasas,
                        hidratacionMl = hidratacion.toIntOrNull() ?: metas.hidratacionMl
                    )
                )
            })
        }
    }
}

@Composable
private fun CampoMeta(etiqueta: String, valor: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, color = Ink2, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(140.dp))
        BasicTextField(
            value = valor, onValueChange = { onChange(it.filter { c -> c.isDigit() }) },
            textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Mint)
        )
    }
}

@Composable
private fun FormularioComida(
    alimentosRepo: com.centinela.app.nutricion.AlimentosRepository,
    fecha: String,
    momento: String,
    onGuardado: (NutricionLogEntity) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var gramos by remember { mutableStateOf("100") }
    var sugerencias by remember { mutableStateOf(listOf<com.centinela.app.nutricion.AlimentoInfo>()) }
    var seleccionado by remember { mutableStateOf<com.centinela.app.nutricion.AlimentoInfo?>(null) }

    LaunchedEffect(nombre) {
        seleccionado = null
        sugerencias = if (nombre.length >= 2) alimentosRepo.buscar(nombre).take(5) else emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = nombre, onValueChange = { nombre = it },
                modifier = Modifier.weight(2f),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Mint),
                decorationBox = { inner -> if (nombre.isEmpty()) Text("Alimento…", color = Ink3); inner() }
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = gramos, onValueChange = { gramos = it.filter { c -> c.isDigit() } },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Mint)
            )
            Text("g", color = Ink2, modifier = Modifier.padding(start = 4.dp))
        }
        if (sugerencias.isNotEmpty() && seleccionado == null) {
            sugerencias.forEach { s ->
                Text(
                    "${s.nombre} · ${s.estado} (${s.kcal.toInt()} kcal/100g)",
                    color = Mint, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable { seleccionado = s; nombre = s.nombre }
                )
            }
        }
        GlowButton("REGISTRAR", accent = Mint, glow = MintGlow, enabled = nombre.isNotBlank(), onClick = {
            val g = gramos.toFloatOrNull() ?: 100f
            val factor = g / 100f
            val base = seleccionado
            onGuardado(
                NutricionLogEntity(
                    fecha = fecha, nombreAlimento = nombre, gramos = g,
                    kcal = (base?.kcal ?: 150.0).toFloat() * factor,
                    proteinas = (base?.proteinas ?: 5.0).toFloat() * factor,
                    carbohidratos = (base?.carbohidratos ?: 15.0).toFloat() * factor,
                    grasas = (base?.grasas ?: 3.0).toFloat() * factor,
                    momento = momento, timestamp = System.currentTimeMillis()
                )
            )
        })
    }
}

@Composable
fun EntrenamientoTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val fecha = hoyYyyyMMdd()
    val registros by repo.entrenamientoDao().delDia(fecha).collectAsState(initial = emptyList())

    Text(
        "Se registran automáticamente los ejercicios completados en las interrupciones del " +
            "Guardián, más los que agregues manualmente aquí.",
        color = Ink2, style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(12.dp))

    val tipos = listOf("flexiones" to 10, "sentadillas" to 20, "plank_seg" to 60, "sprint_seg" to 30)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tipos.forEach { (tipo, cantidad) ->
            GlowButton(tipo.replace("_seg", ""), accent = Red, glow = RedGlow, onClick = {
                scope.launch {
                    repo.entrenamientoDao().insertar(
                        EntrenamientoLogEntity(
                            fecha = fecha, tipo = tipo, cantidadOSegundos = cantidad,
                            origen = "manual", timestamp = System.currentTimeMillis()
                        )
                    )
                }
            })
        }
    }
    Spacer(Modifier.height(16.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(registros, key = { it.id }) { r ->
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${r.tipo} × ${r.cantidadOSegundos}", color = Ink0)
                    Text(if (r.origen == "exercise_interruption") "por interrupción" else "manual", color = Ink2, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun DespertarTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val historial by repo.despertarDao().historial().collectAsState(initial = emptyList())
    var descarga by remember { mutableStateOf("") }
    var directiva by remember { mutableStateOf("") }

    Text("SECUENCIA DE INICIALIZACIÓN » AL DESPERTAR", color = Amber, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("01. DESCARGA MENTAL", color = Amber, style = MaterialTheme.typography.titleSmall)
            Text(
                "Escribe lo primero que se te venga a la cabeza sin filtros: preocupaciones, pendientes, ideas sueltas o frustraciones con las que te despertaste. Vacía el disco duro.",
                color = Ink2, style = MaterialTheme.typography.bodySmall
            )
            BasicTextField(
                value = descarga, onValueChange = { descarga = it },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
                decorationBox = { inner -> if (descarga.isEmpty()) Text("Escribe aquí...", color = Ink3); inner() }
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("02. DIRECTIVA PRINCIPAL", color = Amber, style = MaterialTheme.typography.titleSmall)
            Text(
                "¿Qué acción concreta, incómoda pero necesaria, voy a ejecutar hoy mismo para cerrar la brecha entre lo que pienso y lo que hago?",
                color = Ink2, style = MaterialTheme.typography.bodySmall
            )
            BasicTextField(
                value = directiva, onValueChange = { directiva = it },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
                decorationBox = { inner -> if (directiva.isEmpty()) Text("Escribe aquí...", color = Ink3); inner() }
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    GlowButton("GUARDAR »", accent = Amber, glow = AmberGlow, enabled = descarga.isNotBlank() || directiva.isNotBlank(), onClick = {
        scope.launch {
            repo.despertarDao().insertar(
                DespertarEntity(fecha = hoyYyyyMMdd(), descargaMental = descarga, directivaPrincipal = directiva, timestamp = System.currentTimeMillis())
            )
            descarga = ""; directiva = ""
        }
    })

    Spacer(Modifier.height(20.dp))
    Text("HISTORIAL", color = Ink0, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(historial, key = { it.id }) { h ->
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(h.fecha, color = Ink3, style = MaterialTheme.typography.labelSmall)
                    Text("Descarga mental: ${h.descargaMental}", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    Text("Directiva principal: ${h.directivaPrincipal}", color = Ink1, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CierreDiaTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val historial by repo.cierreDiaDao().historial().collectAsState(initial = emptyList())
    val historialDormir by repo.cierreDiaDao().historialAntesDormir().collectAsState(initial = emptyList())

    var anclas by remember { mutableStateOf<String?>(null) }
    var evidencia by remember { mutableStateOf("") }
    var desviacion by remember { mutableStateOf("") }
    var porque by remember { mutableStateOf("") }
    var modificacion by remember { mutableStateOf("") }
    var pendiente by remember { mutableStateOf("") }
    var victorias by remember { mutableStateOf("") }

    Text("CIERRE DEL DÍA", color = Amber, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("¿Hice las principales anclas del día?", color = Amber, style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("SI" to "SÍ", "PARCIAL" to "PARCIAL", "NO" to "NO").forEach { (valor, etiqueta) ->
                    OpcionRadio(etiqueta, anclas == valor) { anclas = valor }
                }
            }
            CampoTexto(evidencia, { evidencia = it }, "¿Qué evidencia de avance produje hoy?", Amber)
            CampoTexto(desviacion, { desviacion = it }, "¿Cuál fue mi principal desviación?", Amber)
            CampoTexto(porque, { porque = it }, "¿Por qué ocurrió?", Amber)
            CampoTexto(modificacion, { modificacion = it }, "Modificación para mañana", Amber)
            GlowButton("GUARDAR", accent = Amber, glow = AmberGlow, enabled = anclas != null, onClick = {
                scope.launch {
                    repo.cierreDiaDao().insertar(
                        CierreDiaEntity(
                            fecha = hoyYyyyMMdd(), anclas = anclas!!, evidencia = evidencia, desviacion = desviacion,
                            porque = porque, modificacion = modificacion, timestamp = System.currentTimeMillis()
                        )
                    )
                    anclas = null; evidencia = ""; desviacion = ""; porque = ""; modificacion = ""
                }
            })
        }
    }

    Spacer(Modifier.height(16.dp))
    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ANTES DE DORMIR", color = Amber, style = MaterialTheme.typography.titleSmall)
            Text("Pendientes para mañana", color = Ink3, style = MaterialTheme.typography.labelSmall)
            CampoTexto(pendiente, { pendiente = it }, "Escribe aquí...", Amber)
            Text("Victorias reales o hechos concretos que salieron bien hoy", color = Ink3, style = MaterialTheme.typography.labelSmall)
            CampoTexto(victorias, { victorias = it }, "Escribe aquí...", Amber)
            GlowButton("GUARDAR", accent = Amber, glow = AmberGlow, enabled = pendiente.isNotBlank() || victorias.isNotBlank(), onClick = {
                scope.launch {
                    repo.cierreDiaDao().insertarAntesDormir(
                        AntesDormirEntity(fecha = hoyYyyyMMdd(), pendiente = pendiente, victorias = victorias, timestamp = System.currentTimeMillis())
                    )
                    pendiente = ""; victorias = ""
                }
            })
        }
    }

    Spacer(Modifier.height(20.dp))
    Text("HISTORIAL", color = Ink0, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(historial, key = { it.id }) { h ->
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(h.fecha, color = Ink3, style = MaterialTheme.typography.labelSmall)
                        Text("Anclas: ${h.anclas}", color = if (h.anclas == "SI") Ok else Warn, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("Evidencia: ${h.evidencia}", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    Text("Desviación: ${h.desviacion}", color = Ink1, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun OpcionRadio(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(16.dp).background(if (seleccionado) Amber else Hairline)
        )
        Spacer(Modifier.width(6.dp))
        Text(texto, color = if (seleccionado) Amber else Ink1, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun RevisionSemanalTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val revisiones by repo.revisionDao().revisiones().collectAsState(initial = emptyList())
    val respuestas = remember { mutableStateMapOf<String, String>() }
    var diseño by remember { mutableStateOf<String?>(null) }

    Text("REVISIÓN SEMANAL", color = Amber, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))

    REVISION_SECCIONES.forEach { seccion ->
        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${seccion.numero}. ${seccion.titulo}", color = Amber, style = MaterialTheme.typography.titleSmall)
                seccion.preguntas.forEach { p ->
                    Text(p.texto, color = Ink2, style = MaterialTheme.typography.labelSmall)
                    val valor = respuestas[p.id] ?: ""
                    BasicTextField(
                        value = valor, onValueChange = { respuestas[p.id] = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("SÍNTESIS", color = Amber, style = MaterialTheme.typography.titleSmall)
            REVISION_SINTESIS.forEach { p ->
                Text(p.texto, color = Ink2, style = MaterialTheme.typography.labelSmall)
                val valor = respuestas[p.id] ?: ""
                BasicTextField(
                    value = valor, onValueChange = { respuestas[p.id] = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber)
                )
            }
            Text(REVISION_Q16, color = Ink2, style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                REVISION_Q16_OPCIONES.forEach { (valor, etiqueta) ->
                    OpcionRadio(etiqueta, diseño == valor) { diseño = valor }
                }
            }
            Spacer(Modifier.height(6.dp))
            REVISION_SINTESIS2.forEach { p ->
                Text(p.texto, color = Ink2, style = MaterialTheme.typography.labelSmall)
                val valor = respuestas[p.id] ?: ""
                BasicTextField(
                    value = valor, onValueChange = { respuestas[p.id] = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber)
                )
            }
            GlowButton("GUARDAR REVISIÓN", accent = Amber, glow = AmberGlow, onClick = {
                scope.launch {
                    val todas = respuestas.toMutableMap()
                    diseño?.let { todas["q16"] = it }
                    repo.revisionDao().insertar(
                        RevisionSemanalEntity(
                            semana = semanaIso(),
                            respuestasJson = org.json.JSONObject(todas as Map<*, *>).toString(),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    respuestas.clear(); diseño = null
                }
            })
        }
    }

    Spacer(Modifier.height(20.dp))
    Text("HISTORIAL", color = Ink0, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(revisiones, key = { it.id }) { r ->
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Text(r.semana, color = Amber, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
fun EstadisticasTab() {
    val repo = rememberRepo()
    var racha by remember { mutableStateOf(0) }
    var cumplimientoDias by remember { mutableStateOf(listOf<Pair<String, Float>>()) }
    var kcalPorDia by remember { mutableStateOf(listOf<Pair<String, Float>>()) }
    var anclasOkRatio by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {

        val cierres = repo.cierreDiaDao().todosOrdenados()
        val porFecha = cierres.associateBy { it.fecha }
        var cursor = java.util.Calendar.getInstance()
        var contador = 0
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US)
        if (porFecha[fmt.format(cursor.time)]?.anclas != "SI") cursor.add(java.util.Calendar.DAY_OF_YEAR, -1)
        while (true) {
            val clave = fmt.format(cursor.time)
            if (porFecha[clave]?.anclas == "SI") {
                contador++
                cursor.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        racha = contador

        val fmt2 = SimpleDateFormat("yyyyMMdd", Locale.US)
        val hoy = java.util.Calendar.getInstance()
        val dias = (0..6).map { i ->
            val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -i) }
            fmt2.format(c.time)
        }.reversed()
        val logs = repo.rutinaBloqueDao().logsEnRango(dias.first(), dias.last())
        val bloquesTotal = maxOf(1, repo.rutinaBloqueDao().deTipoSuspend(TIPO_ENTRE_SEMANA).size)
        cumplimientoDias = dias.map { d -> d.takeLast(2) to (logs.count { it.fecha == d }.toFloat() / bloquesTotal * 100f) }

        val kcal = repo.nutricionDao().kcalPorDia(dias.first(), dias.last())
        val porFechaKcal = kcal.associate { it.fecha to it.total }
        kcalPorDia = dias.map { d -> d.takeLast(2) to (porFechaKcal[d] ?: 0f) }

        anclasOkRatio = if (cierres.isNotEmpty()) cierres.count { it.anclas == "SI" }.toFloat() / cierres.size else 0f
    }

    Text("ESTADÍSTICAS", color = Cyan, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AngularPanel {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DonutChart(value = anclasOkRatio, color = Cyan, label = "${(anclasOkRatio * 100).toInt()}%")
                Spacer(Modifier.height(6.dp))
                Text("ANCLAS CUMPLIDAS", color = Ink2, style = MaterialTheme.typography.labelSmall)
            }
        }
        AngularPanel {
            Column {
                Text("$racha", color = Amber, style = MaterialTheme.typography.displayMedium)
                Text("DÍAS DE RACHA (CIERRE)", color = Ink2, style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Text("Cumplimiento de rutina (últimos 7 días)", color = Cyan, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    if (cumplimientoDias.isNotEmpty()) {
        BarChartSP(
            labels = cumplimientoDias.map { it.first },
            values = cumplimientoDias.map { it.second },
            max = 100f, color = Cyan
        )
    }

    Spacer(Modifier.height(20.dp))
    Text("Calorías registradas por día (últimos 7 días)", color = Mint, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    if (kcalPorDia.isNotEmpty()) {
        BarChartSP(
            labels = kcalPorDia.map { it.first },
            values = kcalPorDia.map { it.second },
            max = (kcalPorDia.maxOfOrNull { it.second } ?: 1f) + 100f, color = Mint
        )
    }
}

@Composable
private fun CampoTexto(value: String, onChange: (String) -> Unit, hint: String, accent: Color) {
    BasicTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(accent),
        decorationBox = { inner -> if (value.isEmpty()) Text(hint, color = Ink3); inner() }
    )
}
