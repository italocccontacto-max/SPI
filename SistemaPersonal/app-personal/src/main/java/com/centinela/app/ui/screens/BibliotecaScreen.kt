package com.centinela.app.ui.screens

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.rememberRepo
import com.sistemapersonal.data.entity.BibliotecaItemEntity
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch

private val CATEGORIAS = listOf("Libro", "Artículo", "Curso", "Video", "Nota")

@Composable
fun BibliotecaScreen() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()

    var categoriaFiltro by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var editando by remember { mutableStateOf<BibliotecaItemEntity?>(null) }
    var mostrandoFormulario by remember { mutableStateOf(false) }

    val todos by repo.bibliotecaDao().observarTodos().collectAsState(initial = emptyList())
    val visibles = todos
        .filter { categoriaFiltro == null || it.categoria == categoriaFiltro }
        .filter { busqueda.isBlank() || it.titulo.contains(busqueda, true) || it.resumen.contains(busqueda, true) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("X · BIBLIOTECA", color = Amber, style = MaterialTheme.typography.headlineLarge)
            GlowButton("+ NUEVO", accent = Amber, glow = AmberGlow, onClick = {
                editando = null
                mostrandoFormulario = true
            })
        }
        Spacer(Modifier.height(12.dp))

        BasicTextField(
            value = busqueda, onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
            decorationBox = { inner -> if (busqueda.isEmpty()) Text("Buscar…", color = Ink3); inner() }
        )
        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FiltroChip("Todas", categoriaFiltro == null) { categoriaFiltro = null }
            }
            items(CATEGORIAS) { cat ->
                FiltroChip(cat, categoriaFiltro == cat) { categoriaFiltro = cat }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (mostrandoFormulario) {
            FormularioBiblioteca(
                inicial = editando,
                onCancelar = { mostrandoFormulario = false },
                onGuardar = { titulo, categoria, resumen, enlace ->
                    scope.launch {
                        val ahora = System.currentTimeMillis()
                        if (editando != null) {
                            repo.bibliotecaDao().actualizar(
                                editando!!.copy(titulo = titulo, categoria = categoria, resumen = resumen, enlace = enlace, actualizadoEn = ahora)
                            )
                        } else {
                            repo.bibliotecaDao().insertar(
                                BibliotecaItemEntity(
                                    titulo = titulo, categoria = categoria, resumen = resumen, enlace = enlace,
                                    creadoEn = ahora, actualizadoEn = ahora
                                )
                            )
                        }
                        mostrandoFormulario = false
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (visibles.isEmpty()) {
            Text("Sin entradas todavía.", color = Ink2)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibles, key = { it.id }) { entrada ->
                    AngularPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(entrada.titulo, color = Amber, style = MaterialTheme.typography.titleMedium)
                                    Text(entrada.categoria, color = Ink3, style = MaterialTheme.typography.labelSmall)
                                }
                                Row {
                                    Text("editar", color = Cyan, style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.clickable { editando = entrada; mostrandoFormulario = true })
                                    Spacer(Modifier.width(12.dp))
                                    Text("eliminar", color = Danger, style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.clickable { scope.launch { repo.bibliotecaDao().eliminar(entrada) } })
                                }
                            }
                            if (entrada.resumen.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(entrada.resumen, color = Ink1, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (entrada.enlace.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(entrada.enlace, color = Blue, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    AngularPanel(
        modifier = Modifier.clickable(onClick = onClick),
        fill = if (activo) AmberGlow else PanelFillSoft
    ) {
        Text(texto, color = if (activo) Amber else Ink1, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FormularioBiblioteca(
    inicial: BibliotecaItemEntity?,
    onCancelar: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(inicial?.titulo ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: CATEGORIAS.first()) }
    var resumen by remember { mutableStateOf(inicial?.resumen ?: "") }
    var enlace by remember { mutableStateOf(inicial?.enlace ?: "") }

    AngularPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                value = titulo, onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
                decorationBox = { inner -> if (titulo.isEmpty()) Text("Título…", color = Ink3); inner() }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CATEGORIAS) { cat ->
                    FiltroChip(cat, categoria == cat) { categoria = cat }
                }
            }
            BasicTextField(
                value = resumen, onValueChange = { resumen = it },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                textStyle = TextStyle(color = Ink1), cursorBrush = SolidColor(Amber),
                decorationBox = { inner -> if (resumen.isEmpty()) Text("Resumen…", color = Ink3); inner() }
            )
            BasicTextField(
                value = enlace, onValueChange = { enlace = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Ink1), cursorBrush = SolidColor(Amber),
                decorationBox = { inner -> if (enlace.isEmpty()) Text("Enlace (opcional)…", color = Ink3); inner() }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowButton("GUARDAR", accent = Amber, glow = AmberGlow, enabled = titulo.isNotBlank(), onClick = {
                    onGuardar(titulo, categoria, resumen, enlace)
                })
                GlowButton("CANCELAR", accent = Ink2, glow = Hairline, onClick = onCancelar)
            }
        }
    }
}
