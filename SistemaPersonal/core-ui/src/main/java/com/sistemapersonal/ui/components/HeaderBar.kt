package com.sistemapersonal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sistemapersonal.model.Modulo
import com.sistemapersonal.ui.theme.*

/**
 * Barra superior estándar de todas las pantallas: botón de menú, título del
 * módulo (numeral romano + nombre) y badge de estado del sistema a la derecha.
 */
@Composable
fun HeaderBar(
    modulo: Modulo,
    accent: Color,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    badgeText: String = "SISTEMA PERSONAL"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PanelFillSoft)
                    .drawBehind {
                        drawRoundRect(
                            color = accent.copy(alpha = 0.34f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(1f)
                        )
                    }
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val lineW = size.width * 0.78f
                    val startX = (size.width - lineW) / 2f
                    listOf(0.28f, 0.5f, 0.72f).forEach { fy ->
                        drawLine(
                            color = Ink0,
                            start = androidx.compose.ui.geometry.Offset(startX, size.height * fy),
                            end = androidx.compose.ui.geometry.Offset(startX + lineW, size.height * fy),
                            strokeWidth = 1.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    (modulo.roman ?: "•") + ".",
                    color = accent,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "SISTEMA DE ${modulo.label.uppercase()}",
                    color = Ink0,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Info)
            )
            Text(badgeText, color = Info, style = MaterialTheme.typography.labelMedium)
        }
    }
}
