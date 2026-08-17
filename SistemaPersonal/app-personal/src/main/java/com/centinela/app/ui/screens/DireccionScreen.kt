package com.centinela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centinela.app.ui.content.*
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.theme.*

@Composable
fun DireccionScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("IV", color = Mint, style = MaterialTheme.typography.headlineLarge)
                Text("DIRECCIÓN", color = Ink0, style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "Tener una dirección fija sirve para impedir que la acción se convierta en movimiento sin sentido.",
                        color = Ink0, style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No necesito saber exactamente cómo será mi futuro, pero sí tener claro el tipo de vida que estoy " +
                            "construyendo y el rumbo al que todas mis decisiones deben acercarme.",
                        color = Ink1, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("PRINCIPIO DE DIRECCIÓN", color = Mint, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    DIRECCION_PRINCIPIOS.forEach { Text("– $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("DEFINICIÓN DE ÉXITO", color = Mint, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text("El éxito consiste en que exista una creciente coherencia entre:", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    DIRECCION_EXITO.forEach { Text("– $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(6.dp))
                    Text("Los resultados importan. Pero la congruencia sostiene esos resultados.", color = Mint, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("VISIÓN DE LARGO PLAZO", color = Mint, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text("Aspiro a construir una vida donde:", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    DIRECCION_VISION.forEach { Text("– $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item { Text("LOS SIETE EJES DEL RUMBO", color = Mint, style = MaterialTheme.typography.titleMedium) }

        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                modifier = Modifier.height((DIRECCION_EJES.size * 160).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(DIRECCION_EJES) { eje ->
                    AngularPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(eje.numero, color = Ink3, style = MaterialTheme.typography.labelSmall)
                            Text(eje.titulo, color = Mint, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(eje.cuerpo, color = Ink1, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(4.dp))
                            Text(eje.rumbo, color = Ink2, style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(6.dp))
                            BarraRumbo(eje.porcentaje)
                        }
                    }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("DECLARACIÓN FINAL", color = Mint, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    DIRECCION_DECLARACION_FINAL.forEach {
                        Text("• $it", color = Ink1, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraRumbo(porcentaje: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(10) { i ->
            val activo = i < (porcentaje / 10)
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(6.dp)
                    .background(if (activo) Mint else Hairline)
            )
        }
    }
}
