package com.centinela.app.ui.screens

import androidx.compose.foundation.layout.*
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
fun AntiidentidadScreen() {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row {
                    Text("III", color = Red, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("ANTIIDENTIDAD", color = Ink0, style = MaterialTheme.typography.headlineSmall)
                        Text("PATRONES A DETECTAR, CORREGIR Y EVITAR", color = Ink2, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "La antiidentidad no define quién soy. Señala los patrones que debilitan mi carácter, " +
                        "erosionan mi conducta y me alejan de los principios que he elegido sostener.",
                    color = Ink1, style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CONVIERTE", color = Red, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    ANTIIDENTIDAD_CONVIERTE.forEach { Text("› $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item {
            Text("ÁREAS A DETECTAR", color = Red, style = MaterialTheme.typography.titleMedium)
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                modifier = Modifier.height((ANTIIDENTIDAD_AREAS.size * 140).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ANTIIDENTIDAD_AREAS) { area ->
                    AngularPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("${area.numero}  ${area.titulo}", color = Red, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            area.items.forEach { Text("– $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("SEÑALES DE QUE ESTÁ TOMANDO CONTROL", color = Red, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Empiezo a:", color = Ink3, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    ANTIIDENTIDAD_SEÑALES.forEach { Text("✕ $it", color = Ink1, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("HERRAMIENTAS QUE UTILIZA", color = Red, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(ANTIIDENTIDAD_HERRAMIENTAS.joinToString("   ·   "), color = Ink1, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CÓMO RECUPERO EL CONTROL", color = Red, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Cuando detecto una desviación:", color = Ink3, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    ANTIIDENTIDAD_RECUPERAR_CONTROL.forEachIndexed { i, paso ->
                        Text("${i + 1}. $paso", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("No necesito sentirme distinto. Necesito volver a actuar conforme a mis principios.",
                        color = Ink2, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("DECLARACIÓN FINAL", color = Red, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    ANTIIDENTIDAD_DECLARACION_FINAL.forEach {
                        Text("• $it", color = Ink1, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        item {
            AngularPanel(modifier = Modifier.fillMaxWidth(), borderColor = Amber.copy(alpha = 0.5f)) {
                Text(
                    "La aparición de un patrón incompatible no define quién soy. La incapacidad o falta de " +
                        "voluntad para corregirlo repetidamente sí constituye evidencia de un problema que debe ser abordado.",
                    color = Ink0, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
