package com.centinela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.sistemapersonal.data.entity.RutinaBloqueEntity
import com.sistemapersonal.data.entity.RutinaBloqueLogEntity
import com.sistemapersonal.model.EjecucionTab
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun EjecucionScreen() {
    var tab by remember { mutableStateOf(EjecucionTab.HOY) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("VIII · EJECUCIÓN", color = Amber, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EjecucionTab.entries) { t ->
                val activo = t == tab
                AngularPanel(
                    modifier = Modifier.clickable { tab = t },
                    fill = if (activo) AmberGlow else PanelFillSoft
                ) {
                    Text(t.label, color = if (activo) Amber else Ink1, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (tab) {
            EjecucionTab.HOY -> HoyTab()
            EjecucionTab.RUTINAS -> RutinasTab()
            EjecucionTab.NUTRICION -> NutricionTab()
            EjecucionTab.ENTRENAMIENTO -> EntrenamientoTab()
            EjecucionTab.DESVIACION -> DesviacionTab()
            EjecucionTab.DESPERTAR -> DespertarTab()
            EjecucionTab.CIERRE -> CierreDiaTab()
            EjecucionTab.REVISION -> RevisionSemanalTab()
            EjecucionTab.ESTADISTICAS -> EstadisticasTab()
        }
    }
}

private fun tipoSemanaDeHoy(): String {
    val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) TIPO_FIN_SEMANA else TIPO_ENTRE_SEMANA
}

private fun hhmmAMinutos(hhmm: String): Int {
    val partes = hhmm.split(":")
    return partes[0].toInt() * 60 + partes[1].toInt()
}

@Composable
private fun HoyTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val tipo = remember { tipoSemanaDeHoy() }
    val fecha = hoyYyyyMMdd()

    LaunchedEffect(tipo) {
        if (repo.rutinaBloqueDao().deTipoSuspend(tipo).isEmpty()) {
            val seed = if (tipo == TIPO_FIN_SEMANA) SEED_FIN_SEMANA else SEED_ENTRE_SEMANA
            seed.forEachIndexed { i, b ->
                repo.rutinaBloqueDao().insertar(
                    RutinaBloqueEntity(tipoSemana = tipo, inicio = b.inicio, fin = b.fin, etiqueta = b.etiqueta, orden = i)
                )
            }
        }
    }

    val bloques by repo.rutinaBloqueDao().deTipo(tipo).collectAsState(initial = emptyList())
    val logs by repo.rutinaBloqueDao().logsDelDia(fecha).collectAsState(initial = emptyList())
    val idsCompletados = logs.map { it.bloqueId }.toSet()

    val ahoraMin = remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    val total = bloques.size
    val hechos = bloques.count { it.id in idsCompletados }

    Text(
        if (tipo == TIPO_FIN_SEMANA) "Horario de fin de semana" else "Horario entre semana",
        color = Ink2, style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(6.dp))
    Text("$hechos / $total bloques completados", color = Amber, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Hairline)) {
        Box(modifier = Modifier.fillMaxWidth(if (total > 0) hechos / total.toFloat() else 0f).height(6.dp).background(Amber))
    }
    Spacer(Modifier.height(12.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(bloques, key = { it.id }) { bloque ->
            val completado = bloque.id in idsCompletados
            val inicioMin = hhmmAMinutos(bloque.inicio)
            val finMin = hhmmAMinutos(bloque.fin)
            val enEjecucion = !completado && ahoraMin in inicioMin until finMin
            val colorEstado = when {
                completado -> Ok
                enEjecucion -> Amber
                else -> Ink3
            }
            AngularPanel(
                modifier = Modifier.fillMaxWidth().clickable {
                    scope.launch {
                        if (completado) repo.rutinaBloqueDao().desmarcarCompletado(bloque.id, fecha)
                        else repo.rutinaBloqueDao().marcarCompletado(
                            RutinaBloqueLogEntity(bloqueId = bloque.id, fecha = fecha, completadoEn = System.currentTimeMillis())
                        )
                    }
                },
                borderColor = colorEstado.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${bloque.inicio} – ${bloque.fin}", color = Ink3, style = MaterialTheme.typography.labelSmall)
                        Text(bloque.etiqueta, color = if (completado) Ink3 else Ink0, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        when { completado -> "✓"; enEjecucion -> "● ahora"; else -> "" },
                        color = colorEstado, style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RutinasTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    var tipoSeleccionado by remember { mutableStateOf(TIPO_ENTRE_SEMANA) }
    val bloques by repo.rutinaBloqueDao().deTipo(tipoSeleccionado).collectAsState(initial = emptyList())

    var nuevoInicio by remember { mutableStateOf("") }
    var nuevoFin by remember { mutableStateOf("") }
    var nuevaEtiqueta by remember { mutableStateOf("") }

    val conflictos = remember(bloques) { detectarConflictos(bloques) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectorTipoSemana("Entre semana", tipoSeleccionado == TIPO_ENTRE_SEMANA) { tipoSeleccionado = TIPO_ENTRE_SEMANA }
        SelectorTipoSemana("Fin de semana", tipoSeleccionado == TIPO_FIN_SEMANA) { tipoSeleccionado = TIPO_FIN_SEMANA }
    }
    Spacer(Modifier.height(12.dp))

    if (conflictos.isNotEmpty()) {
        Text("⚠ Hay bloques con horarios solapados (resaltados en rojo).", color = Danger, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
    }

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoHora(nuevoInicio, "Inicio HH:mm") { nuevoInicio = it }
                CampoHora(nuevoFin, "Fin HH:mm") { nuevoFin = it }
            }
            BasicTextField(
                value = nuevaEtiqueta, onValueChange = { nuevaEtiqueta = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
                decorationBox = { inner -> if (nuevaEtiqueta.isEmpty()) Text("Descripción del bloque…", color = Ink3); inner() }
            )
            GlowButton("AÑADIR BLOQUE", accent = Amber, glow = AmberGlow,
                enabled = nuevoInicio.matches(Regex("\\d{2}:\\d{2}")) && nuevoFin.matches(Regex("\\d{2}:\\d{2}")) && nuevaEtiqueta.isNotBlank(),
                onClick = {
                    scope.launch {
                        repo.rutinaBloqueDao().insertar(
                            RutinaBloqueEntity(tipoSemana = tipoSeleccionado, inicio = nuevoInicio, fin = nuevoFin, etiqueta = nuevaEtiqueta, orden = bloques.size)
                        )
                        nuevoInicio = ""; nuevoFin = ""; nuevaEtiqueta = ""
                    }
                })
        }
    }

    Spacer(Modifier.height(16.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(bloques, key = { it.id }) { bloque ->
            val enConflicto = bloque.id in conflictos
            AngularPanel(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (enConflicto) Danger else Hairline
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${bloque.inicio} – ${bloque.fin}", color = if (enConflicto) Danger else Ink2, style = MaterialTheme.typography.labelSmall)
                        Text(bloque.etiqueta, color = Ink0, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("eliminar", color = Danger, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { scope.launch { repo.rutinaBloqueDao().eliminar(bloque) } })
                }
            }
        }
    }
}

private fun detectarConflictos(bloques: List<RutinaBloqueEntity>): Set<Long> {
    val conflictos = mutableSetOf<Long>()
    val ordenados = bloques.sortedBy { hhmmAMinutos(it.inicio) }
    for (i in ordenados.indices) {
        for (j in i + 1 until ordenados.size) {
            val a = ordenados[i]; val b = ordenados[j]
            if (hhmmAMinutos(b.inicio) < hhmmAMinutos(a.fin) && hhmmAMinutos(a.inicio) < hhmmAMinutos(b.fin)) {
                conflictos += a.id; conflictos += b.id
            }
        }
    }
    return conflictos
}

@Composable
private fun SelectorTipoSemana(texto: String, activo: Boolean, onClick: () -> Unit) {
    AngularPanel(modifier = Modifier.clickable(onClick = onClick), fill = if (activo) AmberGlow else PanelFillSoft) {
        Text(texto, color = if (activo) Amber else Ink1, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CampoHora(valor: String, hint: String, onChange: (String) -> Unit) {
    BasicTextField(
        value = valor, onValueChange = { if (it.length <= 5) onChange(it) },
        modifier = Modifier.width(100.dp),
        textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
        decorationBox = { inner -> if (valor.isEmpty()) Text(hint, color = Ink3, style = MaterialTheme.typography.bodySmall); inner() }
    )
}

@Composable
private fun DesviacionTab() {
    Text("PROTOCOLO DE DESVIACIÓN", color = Amber, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(6.dp))
    Text("Si me salgo de la rutina, esto es lo que hago — y lo que no hago.", color = Ink1, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AngularPanel(modifier = Modifier.weight(1f), borderColor = Red.copy(alpha = 0.5f)) {
            Column {
                Text("NO DEBO", color = Red, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                DESVIACION_NO_DEBO.forEach { Text("✕ $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(10.dp))
                Text("ZONA RESTRINGIDA", color = Red, style = MaterialTheme.typography.labelSmall)
            }
        }
        AngularPanel(modifier = Modifier.weight(1f), borderColor = Ok.copy(alpha = 0.5f)) {
            Column {
                Text("DEBO", color = Ok, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                DESVIACION_DEBO.forEachIndexed { i, t -> Text("${(i + 1).toString().padStart(2, '0')}. $t", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(10.dp))
                Text("SISTEMA EN RECUPERACIÓN", color = Ok, style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    Spacer(Modifier.height(14.dp))
    AngularPanel(modifier = Modifier.fillMaxWidth(), borderColor = Amber.copy(alpha = 0.5f)) {
        Text(DESVIACION_CALLOUT_1, color = Ink1, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(10.dp))
    AngularPanel(modifier = Modifier.fillMaxWidth(), borderColor = Cyan.copy(alpha = 0.5f)) {
        Text(DESVIACION_CALLOUT_2, color = Ink1, style = MaterialTheme.typography.bodyMedium)
    }
}
