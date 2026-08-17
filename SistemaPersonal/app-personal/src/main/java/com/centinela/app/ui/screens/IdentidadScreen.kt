package com.centinela.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.content.*
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.RadarChartSP
import com.sistemapersonal.ui.theme.*

@Composable
fun IdentidadScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            androidx.compose.foundation.layout.Row {
                Text("II", color = Blue, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.width(12.dp))
                androidx.compose.foundation.layout.Column {
                    Text("IDENTIDAD", color = Ink0, style = MaterialTheme.typography.headlineSmall)
                    Text("QUIÉN SOY Y CÓMO ACTÚO", color = Ink2, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column {
                    Text("MI ESENCIA", color = Blue, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(IDENTIDAD_ESENCIA, color = Ink1, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                ListaConCheck("¿QUÉ CLASE DE PERSONA ELIJO SER?", IDENTIDAD_CLASE_PERSONA, Blue)
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                ListaConCheck("¿QUÉ PRINCIPIOS GOBIERNAN MI CONDUCTA?", IDENTIDAD_PRINCIPIOS, Blue)
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¿QUÉ CAPACIDADES DESARROLLO?", color = Blue, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    RadarChartSP(labels = IDENTIDAD_CAP_LABELS, values = IDENTIDAD_CAP_VALUES, max = 100f, color = Blue)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No considero estos rasgos cualidades innatas. Son capacidades que se fortalecen mediante la práctica constante.",
                        color = Ink2, style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column {
                    Text("¿QUÉ CONSIDERO EL ESTÁNDAR MÍNIMO?", color = Blue, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    IDENTIDAD_ESTANDAR_MINIMO.forEach {
                        Text("✓ $it", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Estos no son logros extraordinarios. Son el estándar mínimo de la persona que he decidido ser.",
                        color = Blue, style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column {
                    Text("¿CÓMO OPERO? · CICLO DE OPERACIÓN", color = Blue, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(IDENTIDAD_CICLO.size) { i ->
                            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${i + 1}", color = Blue, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    IDENTIDAD_CICLO[i], color = Ink1,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.widthIn(max = 90.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column {
                    Text("DECLARACIÓN FINAL", color = Blue, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    IDENTIDAD_DECLARACION_FINAL.forEach {
                        Text("• $it", color = Ink1, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        IDENTIDAD_CIERRE, color = Blue,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ListaConCheck(titulo: String, items: List<String>, accent: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.layout.Column {
        Text(titulo, color = accent, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        items.forEach {
            Text("✓ $it", color = Ink1, style = MaterialTheme.typography.bodySmall)
        }
    }
}
