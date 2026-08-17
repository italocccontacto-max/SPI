package com.sistemapersonal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.Amber
import com.sistemapersonal.ui.theme.AmberGlow
import com.sistemapersonal.ui.theme.AngularPanelShapeSm
import com.sistemapersonal.ui.theme.Ink1
import com.sistemapersonal.ui.theme.Ink2
import com.sistemapersonal.ui.theme.Ink3
import com.sistemapersonal.ui.theme.PanelFillSoft

data class LeaderboardEntry(
    val etiqueta: String,
    val puntos: Int,
    val esActual: Boolean = false
)

@Composable
fun LeaderboardCard(
    entradas: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    val maxPuntos = (entradas.maxOfOrNull { it.puntos } ?: 0).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelFillSoft, AngularPanelShapeSm)
            .padding(20.dp)
    ) {
        Text(
            "TU RACHA DE PUNTOS",
            color = Ink3,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(12.dp))

        if (entradas.isEmpty()) {
            Text(
                "Todavía no hay puntos registrados. Cuando completes tu primer objetivo o mantengas una racha, aparece acá.",
                color = Ink2,
                style = MaterialTheme.typography.bodySmall
            )
            return@Column
        }

        entradas.forEachIndexed { index, entrada ->
            LeaderboardRow(
                posicion = index + 1,
                entrada = entrada,
                proporcion = entrada.puntos.toFloat() / maxPuntos.toFloat()
            )
            if (index != entradas.lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LeaderboardRow(posicion: Int, entrada: LeaderboardEntry, proporcion: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "#$posicion",
            color = if (entrada.esActual) Amber else Ink3,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entrada.etiqueta,
                color = if (entrada.esActual) Ink1 else Ink2,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (entrada.esActual) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Ink3.copy(alpha = 0.15f), AngularPanelShapeSm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(proporcion.coerceIn(0.02f, 1f))
                        .height(6.dp)
                        .background(
                            if (entrada.esActual) Amber else Ink2.copy(alpha = 0.5f),
                            AngularPanelShapeSm
                        )
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "${entrada.puntos}",
            color = if (entrada.esActual) Amber else Ink1,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
