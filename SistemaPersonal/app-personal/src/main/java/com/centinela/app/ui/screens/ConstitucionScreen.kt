package com.centinela.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.content.ARTICULOS_CONSTITUCION
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.theme.*

@Composable
fun ConstitucionScreen() {
    var abiertoIndex by remember { mutableStateOf(-1) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("I", color = Amber, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("MIS PRINCIPIOS FUNDAMENTALES", color = Ink0, style = MaterialTheme.typography.headlineSmall)
                Text("Los principios que rigen mis decisiones y mi vida.", color = Ink2, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ARTICULOS_CONSTITUCION.size) { i ->
                val articulo = ARTICULOS_CONSTITUCION[i]
                val abierto = abiertoIndex == i
                AngularPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { abiertoIndex = if (abierto) -1 else i }
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "ART. ${(i + 1).toString().padStart(2, '0')}",
                                color = Amber, style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                articulo.titulo, color = Ink0,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(if (abierto) "▲" else "▼", color = Ink2)
                        }
                        if (abierto) {
                            Spacer(Modifier.height(10.dp))
                            Text(articulo.cuerpo, color = Ink1, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "◆ MIS PRINCIPIOS · MI NORTE ◆", color = Ink3,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
