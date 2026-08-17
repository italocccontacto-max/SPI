package com.centinela.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.centinela.app.ui.rememberRepo
import com.sistemapersonal.data.entity.CarpetaEntity
import com.sistemapersonal.data.entity.EtiquetaEntity
import com.sistemapersonal.data.entity.EvolucionEventoEntity
import com.sistemapersonal.model.EvolucionTab
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvolucionScreen() {
    var tab by remember { mutableStateOf(EvolucionTab.EVENTOS) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("IX · EVOLUCIÓN", color = Neon, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EvolucionTab.entries) { t ->
                val activo = t == tab
                AngularPanel(
                    modifier = Modifier.clickable { tab = t },
                    fill = if (activo) NeonGlow else PanelFillSoft
                ) {
                    Text(t.label, color = if (activo) Neon else Ink1, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (tab) {
            EvolucionTab.EVENTOS -> EventosTab()
            EvolucionTab.COMPARAR -> CompararTab()
            EvolucionTab.PAPELERA -> PapeleraTab()
            EvolucionTab.CARPETAS -> CarpetasTab()
        }
    }
}

@Composable
private fun EventosTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val carpetas by repo.carpetaDao().observarTodas().collectAsState(initial = emptyList())
    val etiquetas by repo.etiquetaDao().observarTodas().collectAsState(initial = emptyList())
    val activos by repo.evolucionDao().activos().collectAsState(initial = emptyList())

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var carpetaSeleccionada by remember { mutableStateOf<CarpetaEntity?>(null) }
    val etiquetasSeleccionadas = remember { mutableStateListOf<String>() }
    var imagenPath by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val archivo = repo.archivos.copiarImagenExterna(context, uri, "evolucion")
            imagenPath = archivo?.absolutePath
        }
    }

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                value = titulo, onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Neon),
                decorationBox = { inner -> if (titulo.isEmpty()) Text("Título del hito / evento…", color = Ink3); inner() }
            )
            BasicTextField(
                value = descripcion, onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                textStyle = TextStyle(color = Ink1), cursorBrush = SolidColor(Neon),
                decorationBox = { inner -> if (descripcion.isEmpty()) Text("Descripción…", color = Ink3); inner() }
            )

            Text("Carpeta", color = Ink3, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    ChipSeleccionable("Sin carpeta", carpetaSeleccionada == null) { carpetaSeleccionada = null }
                }
                items(carpetas) { c ->
                    ChipSeleccionable(c.nombre, carpetaSeleccionada?.id == c.id) { carpetaSeleccionada = c }
                }
            }

            Text("Etiquetas", color = Ink3, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(etiquetas) { e ->
                    ChipSeleccionable(e.nombre, e.nombre in etiquetasSeleccionadas) {
                        if (e.nombre in etiquetasSeleccionadas) etiquetasSeleccionadas.remove(e.nombre)
                        else etiquetasSeleccionadas.add(e.nombre)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                GlowButton(
                    if (imagenPath == null) "ADJUNTAR IMAGEN" else "CAMBIAR IMAGEN",
                    accent = Neon, glow = NeonGlow, onClick = { pickImage.launch("image/*") }
                )
                if (imagenPath != null) {
                    Spacer(Modifier.width(10.dp))
                    AsyncImage(model = imagenPath, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }

            GlowButton("REGISTRAR", accent = Neon, glow = NeonGlow, enabled = titulo.isNotBlank(), onClick = {
                scope.launch {
                    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    repo.evolucionDao().insertar(
                        EvolucionEventoEntity(
                            titulo = titulo, descripcion = descripcion, fecha = fecha,
                            carpetaId = carpetaSeleccionada?.id,
                            etiquetas = etiquetasSeleccionadas.joinToString(","),
                            imagenPath = imagenPath,
                            creadoEn = System.currentTimeMillis()
                        )
                    )
                    titulo = ""; descripcion = ""; carpetaSeleccionada = null
                    etiquetasSeleccionadas.clear(); imagenPath = null
                }
            })
        }
    }

    Spacer(Modifier.height(16.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(activos, key = { it.id }) { evento ->
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(evento.titulo, color = Ink0, style = MaterialTheme.typography.titleMedium)
                            Text(evento.fecha, color = Ink2, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            "enviar a papelera", color = Danger,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    repo.evolucionDao().actualizar(
                                        evento.copy(enPapelera = true, eliminadoEn = System.currentTimeMillis())
                                    )
                                }
                            }
                        )
                    }
                    if (evento.imagenPath != null) {
                        Spacer(Modifier.height(6.dp))
                        AsyncImage(model = evento.imagenPath, contentDescription = null, modifier = Modifier.fillMaxWidth().height(140.dp))
                    }
                    if (evento.etiquetas.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(evento.etiquetas.split(",").joinToString(" · "), color = Neon, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompararTab() {
    val repo = rememberRepo()
    val activos by repo.evolucionDao().activos().collectAsState(initial = emptyList())
    var idA by remember { mutableStateOf<Long?>(null) }
    var idB by remember { mutableStateOf<Long?>(null) }

    if (activos.size < 2) {
        Text("Necesitás al menos 2 eventos registrados en Eventos para comparar (${activos.size}/2).", color = Ink2)
        return
    }

    Text("Elegí dos eventos:", color = Ink1, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(activos) { e -> ChipSeleccionable(e.titulo, idA == e.id) { idA = e.id } }
    }
    Spacer(Modifier.height(6.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(activos) { e -> ChipSeleccionable(e.titulo, idB == e.id) { idB = e.id } }
    }
    Spacer(Modifier.height(16.dp))

    val eventoA = activos.find { it.id == idA }
    val eventoB = activos.find { it.id == idB }
    if (eventoA != null && eventoB != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PanelComparacion(eventoA, Modifier.weight(1f))
            PanelComparacion(eventoB, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PanelComparacion(evento: EvolucionEventoEntity, modifier: Modifier) {
    AngularPanel(modifier = modifier) {
        Column {
            Text(evento.titulo, color = Neon, style = MaterialTheme.typography.titleSmall)
            Text(evento.fecha, color = Ink3, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Text(evento.descripcion, color = Ink1, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PapeleraTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val papelera by repo.evolucionDao().enPapelera().collectAsState(initial = emptyList())

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(papelera, key = { it.id }) { evento ->
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(evento.titulo, color = Ink3, style = MaterialTheme.typography.titleMedium)
                    Row {
                        Text("restaurar", color = Ok, style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable {
                                scope.launch { repo.evolucionDao().actualizar(evento.copy(enPapelera = false, eliminadoEn = null)) }
                            })
                        Spacer(Modifier.width(12.dp))
                        Text("eliminar definitivo", color = Danger, style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable {
                                scope.launch { repo.evolucionDao().eliminarDefinitivo(evento) }
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun CarpetasTab() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val carpetas by repo.carpetaDao().observarTodas().collectAsState(initial = emptyList())
    val etiquetas by repo.etiquetaDao().observarTodas().collectAsState(initial = emptyList())
    var nuevaCarpeta by remember { mutableStateOf("") }
    var nuevaEtiqueta by remember { mutableStateOf("") }

    Text("Carpetas", color = Neon, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = nuevaCarpeta, onValueChange = { nuevaCarpeta = it },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Neon),
            decorationBox = { inner -> if (nuevaCarpeta.isEmpty()) Text("Nueva carpeta…", color = Ink3); inner() }
        )
        Spacer(Modifier.width(8.dp))
        GlowButton("CREAR", accent = Neon, glow = NeonGlow, onClick = {
            if (nuevaCarpeta.isNotBlank()) {
                scope.launch { repo.carpetaDao().insertar(CarpetaEntity(nombre = nuevaCarpeta)); nuevaCarpeta = "" }
            }
        })
    }
    Spacer(Modifier.height(8.dp))
    carpetas.forEach { c ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(c.nombre, color = Ink1)
            Text("eliminar", color = Danger, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { scope.launch { repo.carpetaDao().eliminar(c) } })
        }
    }

    Spacer(Modifier.height(24.dp))
    Text("Etiquetas", color = Neon, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = nuevaEtiqueta, onValueChange = { nuevaEtiqueta = it },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Neon),
            decorationBox = { inner -> if (nuevaEtiqueta.isEmpty()) Text("Nueva etiqueta…", color = Ink3); inner() }
        )
        Spacer(Modifier.width(8.dp))
        GlowButton("CREAR", accent = Neon, glow = NeonGlow, onClick = {
            if (nuevaEtiqueta.isNotBlank()) {
                scope.launch { repo.etiquetaDao().insertar(EtiquetaEntity(nombre = nuevaEtiqueta)); nuevaEtiqueta = "" }
            }
        })
    }
    Spacer(Modifier.height(8.dp))
    etiquetas.forEach { e ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(e.nombre, color = Ink1)
            Text("eliminar", color = Danger, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { scope.launch { repo.etiquetaDao().eliminar(e) } })
        }
    }
}

@Composable
private fun ChipSeleccionable(texto: String, activo: Boolean, onClick: () -> Unit) {
    AngularPanel(
        modifier = Modifier.clickable(onClick = onClick),
        fill = if (activo) NeonGlow else PanelFillSoft
    ) {
        Text(texto, color = if (activo) Neon else Ink1, style = MaterialTheme.typography.labelSmall)
    }
}
