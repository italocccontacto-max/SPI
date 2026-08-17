package com.centinela.app.ui.screens

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.content.PASOS_PUD
import com.centinela.app.ui.content.PUD_CITA
import com.centinela.app.ui.content.PUD_PRINCIPIO_PROPORCIONALIDAD
import com.centinela.app.ui.rememberRepo
import com.sistemapersonal.data.entity.PudSimulacionEntity
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PudScreen() {
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()
    val historial by repo.pudDao().historial().collectAsState(initial = emptyList())

    var enSimulacion by remember { mutableStateOf(false) }
    var decision by remember { mutableStateOf("") }
    var pasoActual by remember { mutableStateOf(0) }
    val respuestas = remember { mutableStateMapOf<String, String>() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("VI", color = Purple, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("PUD", color = Ink0, style = MaterialTheme.typography.headlineSmall)
                Text("PROTOCOLO UNIVERSAL DE DECISIÓN", color = Ink2, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(PUD_PRINCIPIO_PROPORCIONALIDAD, color = Ink1, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        if (!enSimulacion) {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("¿Qué decisión necesitás tomar?", color = Purple, style = MaterialTheme.typography.titleSmall)
                    BasicTextField(
                        value = decision, onValueChange = { decision = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Purple),
                        decorationBox = { inner -> if (decision.isEmpty()) Text("Escribí la decisión…", color = Ink3); inner() }
                    )
                    GlowButton("INICIAR SIMULACIÓN", accent = Purple, glow = PurpleGlow, enabled = decision.isNotBlank(), onClick = {
                        pasoActual = 0
                        respuestas.clear()
                        enSimulacion = true
                    })
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Historial de decisiones", color = Ink0, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historial, key = { it.id }) { sim ->
                    AngularPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(sim.decision, color = Purple, style = MaterialTheme.typography.titleSmall)
                            Text(
                                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(sim.timestamp)),
                                color = Ink3, style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Actúa: ${sim.respuestaActua}", color = Ink1, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Text(PUD_CITA, color = Ink1, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val paso = PASOS_PUD[pasoActual]
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Decisión: $decision", color = Ink2, style = MaterialTheme.typography.labelSmall)
                    Text("Paso ${pasoActual + 1}/6 · ${paso.etiqueta}", color = Purple, style = MaterialTheme.typography.labelMedium)
                    Text(paso.titulo, color = Ink0, style = MaterialTheme.typography.titleLarge)
                    Text(paso.pregunta, color = Ink1, style = MaterialTheme.typography.bodyMedium)

                    val valorActual = respuestas[paso.clave] ?: ""
                    BasicTextField(
                        value = valorActual,
                        onValueChange = { respuestas[paso.clave] = it },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Purple),
                        decorationBox = { inner -> if (valorActual.isEmpty()) Text("Tu respuesta…", color = Ink3); inner() }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (pasoActual > 0) {
                            GlowButton("ATRÁS", accent = Ink2, glow = Hairline, onClick = { pasoActual-- })
                        }
                        if (pasoActual < PASOS_PUD.lastIndex) {
                            GlowButton("SIGUIENTE", accent = Purple, glow = PurpleGlow, onClick = { pasoActual++ })
                        } else {
                            GlowButton("GUARDAR DECISIÓN", accent = Purple, glow = PurpleGlow, onClick = {
                                scope.launch {
                                    repo.pudDao().insertar(
                                        PudSimulacionEntity(
                                            decision = decision,
                                            respuestaImporta = respuestas["importa"] ?: "",
                                            respuestaEntiende = respuestas["entiende"] ?: "",
                                            respuestaExplora = respuestas["explora"] ?: "",
                                            respuestaFiltra = respuestas["filtra"] ?: "",
                                            respuestaEvalua = respuestas["evalua"] ?: "",
                                            respuestaActua = respuestas["actua"] ?: "",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    enSimulacion = false
                                    decision = ""
                                }
                            })
                        }
                    }
                    Text(
                        "cancelar simulación", color = Ink3, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickableSinRipple { enSimulacion = false }
                    )
                }
            }
        }
    }
}

private fun Modifier.clickableSinRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
