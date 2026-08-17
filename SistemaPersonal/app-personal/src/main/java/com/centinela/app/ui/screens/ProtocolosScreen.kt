package com.centinela.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.content.PROTOCOLOS
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.theme.*

@Composable
fun ProtocolosScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("VII", color = Amber, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("PROTOCOLOS", color = Ink0, style = MaterialTheme.typography.headlineSmall)
                Text("PROCEDIMIENTOS OPERATIVOS ESTÁNDAR", color = Ink2, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Protocolos para mantener el sistema estable, resolver desviaciones y asegurar el " +
                "cumplimiento de la misión personal.",
            color = Ink1, style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(PROTOCOLOS) { proto ->
                AngularPanel(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(proto.numero, color = Amber, style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(10.dp))
                            Text(proto.titulo, color = Ink0, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("PROCEDIMIENTO:", color = Ink3, style = MaterialTheme.typography.labelSmall)
                        proto.pasos.forEachIndexed { i, paso ->
                            Text("${i + 1}. $paso", color = Ink1, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("SEÑAL DE SALIDA:", color = Ink3, style = MaterialTheme.typography.labelSmall)
                        Text(proto.señalSalida, color = Ink1, style = MaterialTheme.typography.bodySmall)
                        if (proto.avisoProfesional) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "(Este protocolo no sustituye soporte médico/psicológico).",
                                color = Amber, style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Los protocolos no eliminan los problemas. Eliminan la improvisación ante ellos.",
                color = Ink1, fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
