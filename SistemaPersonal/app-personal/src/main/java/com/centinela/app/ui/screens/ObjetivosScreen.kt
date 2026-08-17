package com.centinela.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centinela.app.ui.rememberRepo
import com.sistemapersonal.data.entity.*
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch

private val IND_FISICOS = listOf("Peso", "Bíceps", "Pecho", "Cintura", "Cuádriceps", "Pantorrillas", "Antebrazo", "Altura")
private val IND_RENDIMIENTO = listOf("Flexiones máximas", "Dominadas máximas", "Fondos máximos", "Tiempo de carrera", "Tiempo de plancha")
private val DIAS_SEMANA = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun ObjetivosScreen() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val areas by repo.areaObjetivoDao().observarTodas().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        if (repo.indicadorDao().contarPorTipo("fisicos") == 0) {
            IND_FISICOS.forEach { repo.indicadorDao().insertar(IndicadorEntity(tipo = "fisicos", nombre = it)) }
        }
        if (repo.indicadorDao().contarPorTipo("rendimiento") == 0) {
            IND_RENDIMIENTO.forEach { repo.indicadorDao().insertar(IndicadorEntity(tipo = "rendimiento", nombre = it)) }
        }
    }

    var nuevaArea by remember { mutableStateOf("") }
    var mostrarInputArea by remember { mutableStateOf(false) }
    var expandirTodo by remember { mutableStateOf(false) }
    var expandToken by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Sistema de Objetivos", color = Ink0, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Áreas en vida con objetivos macro y micro, completamente editables.\n" +
                                "Crea tantas áreas, puntos y objetivos como necesites; todo se guarda en este dispositivo.",
                            color = Ink2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    TargetGlyph(accent = Teal, modifier = Modifier.size(84.dp))
                }

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlowButton(
                        "+ Agregar área", accent = Teal, glow = TealGlow,
                        onClick = { mostrarInputArea = !mostrarInputArea }
                    )
                    GlowButton(
                        "Expandir todo", accent = Teal, glow = TealGlow,
                        onClick = { expandirTodo = !expandirTodo; expandToken++ }
                    )
                }

                if (mostrarInputArea) {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = nuevaArea, onValueChange = { nuevaArea = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = Ink0, fontFamily = ChakraPetchFamily, fontSize = 15.sp),
                            cursorBrush = SolidColor(Teal),
                            decorationBox = { inner ->
                                if (nuevaArea.isEmpty()) Text("Nueva área (ej. Salud Física)…", color = Ink3, style = MaterialTheme.typography.bodyMedium)
                                inner()
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        GlowButton("AÑADIR", accent = Teal, glow = TealGlow, onClick = {
                            if (nuevaArea.isNotBlank()) {
                                scope.launch {
                                    repo.areaObjetivoDao().insertar(AreaObjetivoEntity(nombre = nuevaArea, orden = areas.size))
                                    nuevaArea = ""
                                    mostrarInputArea = false
                                }
                            }
                        })
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                areas.forEach { area -> AreaCard(area, forzarExpandida = expandirTodo, token = expandToken) }
            }
        }

        item { SeccionTitulo("INDICADORES FÍSICOS", "Indicadores antropométricos") }
        item {
            TablaIndicadores(tipo = "fisicos", etiquetaFinal = "Realizado final")
        }

        item { SeccionTitulo("MÉTRICAS DE RENDIMIENTO", null) }
        item {
            TablaIndicadores(tipo = "rendimiento", etiquetaFinal = "Resultado final")
            Spacer(Modifier.height(4.dp))
            GlowButton("Guardar métricas", accent = Teal, glow = TealGlow, onClick = {})
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SeccionTitulo(titulo: String, subtitulo: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Purple)
            )
            Text(titulo, color = Purple, style = MaterialTheme.typography.titleMedium)
        }
        if (subtitulo != null) {
            Text(subtitulo, color = Ink3, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TargetGlyph(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val c = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.58f)
        drawCircle(accent.copy(alpha = 0.85f), size.minDimension * 0.44f, c, style = Stroke(2.4f))
        drawCircle(accent.copy(alpha = 0.55f), size.minDimension * 0.30f, c, style = Stroke(1.6f))
        drawCircle(accent.copy(alpha = 0.9f), size.minDimension * 0.06f, c)
        drawLine(
            accent.copy(alpha = 0.9f),
            androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.06f),
            c,
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )
        val arrowTip = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.06f)
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(arrowTip.x - 14f, arrowTip.y + 3f)
            lineTo(arrowTip.x - 3f, arrowTip.y + 14f)
            close()
        }
        drawPath(p, accent.copy(alpha = 0.9f))
    }
}

@Composable
private fun AreaCard(area: AreaObjetivoEntity, forzarExpandida: Boolean, token: Int) {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    var abierta by remember(area.id) { mutableStateOf(false) }
    LaunchedEffect(token) { abierta = forzarExpandida }

    val puntos by repo.puntoObjetivoDao().deArea(area.id).collectAsState(initial = emptyList())
    var nuevoPunto by remember { mutableStateOf("") }

    AngularPanel(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { abierta = !abierta },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .background(PanelFillSoft, RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ÁREA", color = Teal, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(area.nombre, color = Ink0, style = MaterialTheme.typography.titleLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${puntos.size} punto(s)", color = Ink2, style = MaterialTheme.typography.labelMedium)
                    Chevron(abierta, Teal)
                }
            }
            if (abierta) {
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    puntos.forEach { PuntoCard(it) }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = nuevoPunto, onValueChange = { nuevoPunto = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = Ink0, fontFamily = ChakraPetchFamily, fontSize = 14.sp),
                        cursorBrush = SolidColor(Teal),
                        decorationBox = { inner -> if (nuevoPunto.isEmpty()) Text("Nuevo punto/objetivo…", color = Ink3, style = MaterialTheme.typography.bodySmall); inner() }
                    )
                    Spacer(Modifier.width(8.dp))
                    GlowButton("AÑADIR", accent = Teal, glow = TealGlow, onClick = {
                        if (nuevoPunto.isNotBlank()) {
                            scope.launch {
                                repo.puntoObjetivoDao().insertar(
                                    PuntoObjetivoEntity(
                                        areaId = area.id, titulo = nuevoPunto,
                                        porQue = "", conducta = "", principio = "", pregunta = "",
                                        orden = puntos.size
                                    )
                                )
                                nuevoPunto = ""
                            }
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun Chevron(abierta: Boolean, accent: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        val p = androidx.compose.ui.graphics.Path().apply {
            if (abierta) {
                moveTo(w * 0.2f, h * 0.65f); lineTo(w * 0.5f, h * 0.35f); lineTo(w * 0.8f, h * 0.65f)
            } else {
                moveTo(w * 0.2f, h * 0.35f); lineTo(w * 0.5f, h * 0.65f); lineTo(w * 0.8f, h * 0.35f)
            }
        }
        drawPath(p, accent, style = Stroke(1.8f, cap = StrokeCap.Round))
    }
}

@Composable
private fun PuntoCard(punto: PuntoObjetivoEntity) {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    var abierto by remember { mutableStateOf(false) }
    val micros by repo.microObjetivoDao().dePunto(punto.id).collectAsState(initial = emptyList())
    var nuevoMicro by remember { mutableStateOf("") }
    var resultado by remember(punto.id) { mutableStateOf(punto.metricaResultado.toFloat()) }

    AngularPanel(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        fill = PanelFillSoft
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { abierto = !abierto },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(punto.titulo, color = Ink0, style = MaterialTheme.typography.titleMedium)
                Chevron(abierto, Ink2)
            }

            if (abierto) {
                Spacer(Modifier.height(10.dp))
                CampoEditable(punto.porQue, "¿Por qué importa?") { nuevo ->
                    scope.launch { repo.puntoObjetivoDao().actualizar(punto.copy(porQue = nuevo)) }
                }
                CampoEditable(punto.conducta, "Conducta esperada") { nuevo ->
                    scope.launch { repo.puntoObjetivoDao().actualizar(punto.copy(conducta = nuevo)) }
                }
                CampoEditable(punto.principio, "Principio relacionado") { nuevo ->
                    scope.launch { repo.puntoObjetivoDao().actualizar(punto.copy(principio = nuevo)) }
                }
                CampoEditable(punto.pregunta, "Pregunta de autoevaluación") { nuevo ->
                    scope.launch { repo.puntoObjetivoDao().actualizar(punto.copy(pregunta = nuevo)) }
                }

                Spacer(Modifier.height(12.dp))
                Text("Resultado: ${resultado.toInt()}%", color = Teal, style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = resultado,
                    onValueChange = { resultado = it },
                    onValueChangeFinished = {
                        scope.launch { repo.puntoObjetivoDao().actualizar(punto.copy(metricaResultado = resultado.toInt())) }
                    },
                    valueRange = 0f..100f,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = Teal, activeTrackColor = Teal, inactiveTrackColor = Hairline
                    )
                )

                Spacer(Modifier.height(8.dp))
                Text("Ejecución semanal", color = Teal, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    punto.metricaEjecucionSemana.padEnd(7, '0').take(7).forEachIndexed { i, c ->
                        val hecho = c == '1'
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (hecho) Teal else Hairline.copy(alpha = 0.3f))
                                .clickable {
                                    val nuevaSemana = punto.metricaEjecucionSemana.padEnd(7, '0').toCharArray()
                                    nuevaSemana[i] = if (hecho) '0' else '1'
                                    scope.launch {
                                        repo.puntoObjetivoDao().actualizar(punto.copy(metricaEjecucionSemana = String(nuevaSemana)))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(DIAS_SEMANA[i], color = if (hecho) Void else Ink2, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Micro objetivos", color = Teal, style = MaterialTheme.typography.labelMedium)
                Column {
                    micros.forEach { micro ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = micro.hecho,
                                onCheckedChange = {
                                    scope.launch { repo.microObjetivoDao().actualizar(micro.copy(hecho = it)) }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Teal, uncheckedColor = Ink3)
                            )
                            Text(micro.texto, color = if (micro.hecho) Ink3 else Ink1, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = nuevoMicro, onValueChange = { nuevoMicro = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = Ink0, fontFamily = ChakraPetchFamily, fontSize = 14.sp),
                        cursorBrush = SolidColor(Teal),
                        decorationBox = { inner -> if (nuevoMicro.isEmpty()) Text("Nuevo micro objetivo…", color = Ink3, style = MaterialTheme.typography.bodySmall); inner() }
                    )
                    Spacer(Modifier.width(8.dp))
                    GlowButton("AÑADIR", accent = Teal, glow = TealGlow, onClick = {
                        if (nuevoMicro.isNotBlank()) {
                            scope.launch {
                                repo.microObjetivoDao().insertar(
                                    MicroObjetivoEntity(puntoId = punto.id, texto = nuevoMicro, orden = micros.size)
                                )
                                nuevoMicro = ""
                            }
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun CampoEditable(valor: String, hint: String, onGuardar: (String) -> Unit) {
    var texto by remember(valor) { mutableStateOf(valor) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(hint, color = Ink3, style = MaterialTheme.typography.labelSmall)
        BasicTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Ink1, fontFamily = ChakraPetchFamily, fontSize = 14.sp),
            cursorBrush = SolidColor(Teal)
        )
        Text(
            "guardar", color = Teal, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable { onGuardar(texto) }
        )
    }
}

@Composable
private fun TablaIndicadores(tipo: String, etiquetaFinal: String) {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val filas by repo.indicadorDao().deTipo(tipo).collectAsState(initial = emptyList())

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("INDICADOR", color = Purple, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.3f))
                Text("LÍNEA BASE", color = Purple, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text("META", color = Purple, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text("ACTUAL", color = Purple, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text(etiquetaFinal.uppercase(), color = Purple, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.2f))
                Text("¿CUMPLIDO?", color = Purple, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline.copy(alpha = 0.4f)))
            Spacer(Modifier.height(6.dp))

            filas.forEachIndexed { index, fila ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fila.nombre, color = Ink0, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.3f))
                    CeldaIndicador(fila.base, Modifier.weight(1f)) { scope.launch { repo.indicadorDao().actualizar(fila.copy(base = it)) } }
                    CeldaIndicador(fila.meta, Modifier.weight(1f)) { scope.launch { repo.indicadorDao().actualizar(fila.copy(meta = it)) } }
                    CeldaIndicador(fila.actual, Modifier.weight(1f)) { scope.launch { repo.indicadorDao().actualizar(fila.copy(actual = it)) } }
                    CeldaIndicador(fila.valorFinal, Modifier.weight(1.2f)) { scope.launch { repo.indicadorDao().actualizar(fila.copy(valorFinal = it)) } }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Bg1.copy(alpha = 0.6f))
                                .clickable { scope.launch { repo.indicadorDao().actualizar(fila.copy(cumplido = !fila.cumplido)) } }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(if (fila.cumplido) "Sí" else "—", color = if (fila.cumplido) Ok else Ink3, style = MaterialTheme.typography.bodySmall)
                            Chevron(false, Ink3)
                        }
                    }
                }
                if (index < filas.lastIndex) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline.copy(alpha = 0.18f)))
                }
            }
        }
    }
}

@Composable
private fun CeldaIndicador(valor: String, modifier: Modifier, onGuardar: (String) -> Unit) {
    var texto by remember(valor) { mutableStateOf(valor) }
    Box(
        modifier = modifier
            .padding(end = 8.dp)
            .background(Bg1.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        BasicTextField(
            value = texto,
            onValueChange = { texto = it; onGuardar(it) },
            textStyle = TextStyle(color = Ink1, fontFamily = ChakraPetchFamily, fontSize = 13.sp),
            cursorBrush = SolidColor(Teal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

