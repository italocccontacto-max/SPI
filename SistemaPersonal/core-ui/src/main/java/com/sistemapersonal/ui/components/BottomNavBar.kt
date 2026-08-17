package com.sistemapersonal.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sistemapersonal.model.Modulo
import com.sistemapersonal.ui.theme.*

/**
 * Barra de navegación inferior. Reemplaza al antiguo NavigationRailSP (lateral)
 * como punto único de navegación entre módulos, en toda la aplicación.
 */
@Composable
fun BottomNavBar(
    modulos: List<Modulo>,
    seleccionado: Modulo,
    onSeleccionar: (Modulo) -> Unit,
    modifier: Modifier = Modifier
) {
    val system = LocalVisualEnergy.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Bg2.copy(alpha = 0.94f),
                        Bg0.copy(alpha = 0.96f),
                        Bg1.copy(alpha = 0.98f)
                    )
                )
            )
            .drawBehind {
                drawLine(
                    color = Hairline.copy(alpha = 0.72f),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Hairline.copy(alpha = 0.22f),
                    start = androidx.compose.ui.geometry.Offset(0f, 5f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 5f),
                    strokeWidth = 1f
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp)
        ) {
            items(modulos) { modulo ->
                val active = modulo == seleccionado
                val colors = accentFor(modulo.theme)
                val transition = rememberInfiniteTransition(label = "bottomnav_energy_${modulo.id}")
                val pulse by transition.animateFloat(
                    initialValue = 0.72f,
                    targetValue = 1.16f,
                    animationSpec = infiniteRepeatable(
                        tween(if (active) 2100 else 4800),
                        RepeatMode.Reverse
                    ),
                    label = "bottomnav_pulse_${modulo.id}"
                )
                val scale by animateFloatAsState(
                    targetValue = if (active) 1.06f else 1f,
                    animationSpec = tween(280),
                    label = "bottomnav_scale_${modulo.id}"
                )

                InstrumentControl(
                    modifier = Modifier
                        .width(74.dp)
                        .height(64.dp),
                    accent = colors.accent,
                    enabled = true,
                    mode = InstrumentMode.PRESS,
                    value = if (active) 1f else 0.18f,
                    controlId = "navigation.module.${modulo.id}",
                    onActivate = {
                        system.navigate(modulo.id)
                        onSeleccionar(modulo)
                        InstrumentOutcome.SUCCESS
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationY = if (active) -1.5f else 0f
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                                if (active) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                colors.accent.copy(alpha = 0.17f * pulse),
                                                colors.accent.copy(alpha = 0.04f),
                                                androidx.compose.ui.graphics.Color.Transparent
                                            )
                                        ),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                                    )
                                    val frame = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(9f, 0f)
                                        lineTo(size.width - 9f, 0f)
                                        lineTo(size.width, 9f)
                                        lineTo(size.width, size.height - 9f)
                                        lineTo(size.width - 9f, size.height)
                                        lineTo(9f, size.height)
                                        lineTo(0f, size.height - 9f)
                                        lineTo(0f, 9f)
                                        close()
                                    }
                                    drawPath(frame, colors.accent.copy(alpha = 0.82f), style = Stroke(1.1f))
                                    drawLine(
                                        colors.accent.copy(alpha = 0.95f * pulse),
                                        androidx.compose.ui.geometry.Offset(9f, size.height - 4f),
                                        androidx.compose.ui.geometry.Offset(size.width - 9f, size.height - 4f),
                                        strokeWidth = 2.4f,
                                        cap = StrokeCap.Round
                                    )
                                    drawArc(
                                        colors.accent.copy(alpha = 0.48f * pulse),
                                        -90f, 240f, false,
                                        androidx.compose.ui.geometry.Offset(c.x - 18f, c.y - 18f - 3f),
                                        androidx.compose.ui.geometry.Size(36f, 36f),
                                        style = Stroke(1.0f, cap = StrokeCap.Round)
                                    )
                                } else {
                                    drawRoundRect(
                                        color = Ink3.copy(alpha = 0.06f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                }
                                drawModuleGlyphBottom(
                                    modulo,
                                    colors.accent,
                                    if (active) 1f else 0.5f,
                                    sizeBoost = if (active) 1.0f else 0.88f
                                )
                            }
                        }
                        Text(
                            modulo.label.uppercase(),
                            color = if (active) colors.accent else Ink3,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawModuleGlyphBottom(
    modulo: Modulo,
    color: androidx.compose.ui.graphics.Color,
    alpha: Float,
    sizeBoost: Float = 1f
) {
    val cx = size.width / 2f
    val cy = size.height / 2f - 4f
    val r = 9f * sizeBoost

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = 1.6f) {
        drawLine(
            color.copy(alpha = alpha),
            androidx.compose.ui.geometry.Offset(x1, y1),
            androidx.compose.ui.geometry.Offset(x2, y2),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }

    when (modulo) {
        Modulo.INICIO -> {
            val p = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy - r); lineTo(cx + r, cy + r * 0.15f); lineTo(cx + r * 0.62f, cy + r * 0.15f)
                lineTo(cx + r * 0.62f, cy + r); lineTo(cx - r * 0.62f, cy + r); lineTo(cx - r * 0.62f, cy + r * 0.15f)
                lineTo(cx - r, cy + r * 0.15f); close()
            }
            drawPath(p, color.copy(alpha = alpha), style = Stroke(1.5f))
        }
        Modulo.CONSTITUCION -> {
            val p = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy - r); lineTo(cx + r * 0.82f, cy - r * 0.26f); lineTo(cx + r * 0.62f, cy + r * 0.72f)
                lineTo(cx, cy + r); lineTo(cx - r * 0.62f, cy + r * 0.72f); lineTo(cx - r * 0.82f, cy - r * 0.26f); close()
            }
            drawPath(p, color.copy(alpha = alpha), style = Stroke(1.5f))
        }
        Modulo.IDENTIDAD -> {
            drawCircle(color.copy(alpha = alpha), r * 0.78f, androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(1.5f))
            drawCircle(color.copy(alpha = alpha * 0.6f), r * 0.34f, androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(1.0f))
        }
        Modulo.ANTIIDENTIDAD -> {
            line(cx - r * 0.82f, cy + r * 0.82f, cx + r * 0.82f, cy - r * 0.82f, 1.9f)
            drawCircle(color.copy(alpha = alpha * 0.75f), r * 0.86f, androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(1f))
        }
        Modulo.DIRECCION -> {
            line(cx - r * 0.92f, cy + r * 0.74f, cx + r * 0.72f, cy - r * 0.72f, 1.8f)
            line(cx + r * 0.72f, cy - r * 0.72f, cx + r * 0.26f, cy - r * 0.74f, 1.4f)
            line(cx + r * 0.72f, cy - r * 0.72f, cx + r * 0.72f, cy - r * 0.25f, 1.4f)
        }
        Modulo.OBJETIVOS -> {
            drawCircle(color.copy(alpha = alpha), r, androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(1.4f))
            drawCircle(color.copy(alpha = alpha), r * 0.55f, androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(1.1f))
            drawCircle(color.copy(alpha = alpha), r * 0.16f, androidx.compose.ui.geometry.Offset(cx, cy))
        }
        Modulo.PUD -> {
            drawArc(color.copy(alpha = alpha), -100f, 250f, false, androidx.compose.ui.geometry.Offset(cx-r, cy-r), androidx.compose.ui.geometry.Size(r*2f,r*2f), style = Stroke(1.8f))
            line(cx-r*0.65f,cy+2f,cx+r*0.62f,cy+2f,0.9f)
        }
        Modulo.PROTOCOLOS -> {
            val p = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx-r*0.66f, cy-r); lineTo(cx+r*0.66f, cy-r); lineTo(cx+r, cy-r*0.66f); lineTo(cx+r, cy+r)
                lineTo(cx-r*0.66f, cy+r); lineTo(cx-r, cy+r*0.66f); lineTo(cx-r, cy-r*0.66f); close()
            }
            drawPath(p, color.copy(alpha = alpha), style = Stroke(1.4f))
        }
        Modulo.EJECUCION -> {
            line(cx, cy-r, cx, cy+r, 1.8f)
            line(cx-r*0.58f, cy+r*0.18f, cx+r*0.58f, cy+r*0.18f, 1.0f)
        }
        Modulo.EVOLUCION -> {
            drawArc(color.copy(alpha = alpha), 200f, 140f, false, androidx.compose.ui.geometry.Offset(cx-r,cy-r), androidx.compose.ui.geometry.Size(r*2f,r*2f), style = Stroke(1.8f))
            line(cx-r*0.42f,cy+r*0.44f,cx+r*0.55f,cy-r*0.42f,1.5f)
        }
        Modulo.BIBLIOTECA -> {
            drawRect(color.copy(alpha = alpha), androidx.compose.ui.geometry.Offset(cx-r*0.72f,cy-r*0.70f), androidx.compose.ui.geometry.Size(r*1.44f,r*1.5f), style = Stroke(1.4f))
            line(cx-r*0.42f,cy-r*0.20f,cx+r*0.45f,cy-r*0.20f,1.0f)
        }
        Modulo.GUARDIAN -> {
            val p = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy-r); lineTo(cx+r*0.8f, cy-r*0.42f); lineTo(cx+r*0.65f, cy+r*0.62f)
                lineTo(cx, cy+r); lineTo(cx-r*0.65f, cy+r*0.62f); lineTo(cx-r*0.8f, cy-r*0.42f); close()
            }
            drawPath(p, color.copy(alpha = alpha), style = Stroke(1.6f))
        }
    }
}
