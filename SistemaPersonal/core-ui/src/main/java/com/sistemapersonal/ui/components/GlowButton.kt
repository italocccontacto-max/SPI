package com.sistemapersonal.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.*

enum class GlowButtonState {
    IDLE, FOCUS, ACTIVE, SUCCESS, WARNING, ERROR, ACHIEVEMENT
}

enum class GlowGlyph {
    PLUS, PLAY, BACK, NEXT, SAVE, EDIT, ADD, LINK, SHIELD, CHECK, ALERT, DROP, IMAGE, BOOK, GENERIC
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color,
    glow: Color,
    enabled: Boolean = true,
    state: GlowButtonState = GlowButtonState.IDLE,
    glyph: GlowGlyph = glyphFromLabel(text),
    controlId: String = "glow:${text}:${glyph.name}",
    outcome: InstrumentOutcome = InstrumentOutcome.SUCCESS
) {
    var pressed by remember { mutableStateOf(false) }
    val semanticAccent = when (state) {
        GlowButtonState.SUCCESS, GlowButtonState.ACHIEVEMENT -> Mint
        GlowButtonState.WARNING -> Amber
        GlowButtonState.ERROR -> Danger
        else -> accent
    }

    val transition = rememberInfiniteTransition(label = "button_state")
    val statePulse by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(
                when (state) {
                    GlowButtonState.FOCUS -> 1500
                    GlowButtonState.ACTIVE -> 1900
                    GlowButtonState.WARNING -> 780
                    GlowButtonState.ERROR -> 540
                    GlowButtonState.ACHIEVEMENT -> 1150
                    else -> 3600
                }
            ),
            RepeatMode.Reverse
        ),
        label = "button_pulse"
    )

    val energy by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.18f
            pressed -> 1f
            state == GlowButtonState.ACTIVE -> 1f
            state == GlowButtonState.ACHIEVEMENT -> 1.18f
            state == GlowButtonState.FOCUS -> 0.92f
            state == GlowButtonState.WARNING -> statePulse
            state == GlowButtonState.ERROR -> statePulse
            else -> 0.50f
        },
        animationSpec = tween(180),
        label = "button_energy"
    )

    InstrumentControl(
        controlId = controlId,
        modifier = modifier
            .height(54.dp)
            .widthIn(min = 84.dp),
        accent = semanticAccent,
        enabled = enabled,
        mode = InstrumentMode.PRESS,
        value = energy.coerceIn(0f, 1f),
        onActivate = { onClick(); outcome },
        onPressState = { pressed = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .graphicsLayer {
                    translationX = when (state) {
                        GlowButtonState.ERROR -> kotlin.math.sin(statePulse * 3.14f) * 1.5f
                        GlowButtonState.WARNING -> kotlin.math.sin(statePulse * 3.14f) * 0.8f
                        else -> 0f
                    }
                    val pressScale = if (pressed) 0.965f else 1f
                    scaleX = pressScale * if (state == GlowButtonState.ACHIEVEMENT) 1.01f else 1f
                    scaleY = pressScale * if (state == GlowButtonState.ACHIEVEMENT) 1.01f else 1f
                },
            contentAlignment = Alignment.Center
        ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val e = energy.coerceIn(0f, 1.2f)

            val glyphCenter = androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height / 2f)
            drawGlowGlyph(
                glyph = glyph,
                color = semanticAccent,
                center = glyphCenter,
                scale = 1.28f + if (state == GlowButtonState.ACHIEVEMENT) 0.10f else 0f,
                alpha = if (enabled) (0.78f + 0.20f * e).coerceIn(0f, 1f) else 0.24f
            )

            when (state) {
                GlowButtonState.FOCUS -> {
                    val sweepX = size.width * (0.05f + 0.90f * statePulse)
                    drawLine(
                        semanticAccent.copy(alpha = 0.34f * e),
                        androidx.compose.ui.geometry.Offset(sweepX, 4f),
                        androidx.compose.ui.geometry.Offset(sweepX, size.height - 4f),
                        1.4f
                    )
                }
                GlowButtonState.ACTIVE -> {
                    drawArc(
                        semanticAccent.copy(alpha = 0.72f * e),
                        -90f,
                        120f,
                        false,
                        androidx.compose.ui.geometry.Offset(glyphCenter.x - 18f, glyphCenter.y - 18f),
                        androidx.compose.ui.geometry.Size(36f, 36f),
                        style = Stroke(1.5f, cap = StrokeCap.Round)
                    )
                }
                GlowButtonState.SUCCESS -> {
                    drawCircle(
                        Mint.copy(alpha = 0.32f * e),
                        17f,
                        glyphCenter,
                        style = Stroke(1.4f)
                    )
                    drawGlowGlyph(GlowGlyph.CHECK, Mint, glyphCenter, 0.78f, 0.95f)
                }
                GlowButtonState.WARNING -> {
                    drawCircle(
                        Amber.copy(alpha = 0.20f * e),
                        17f + statePulse * 2f,
                        glyphCenter,
                        style = Stroke(1.3f)
                    )
                }
                GlowButtonState.ERROR -> {
                    repeat(3) { idx ->
                        val y = glyphCenter.y + (idx - 1) * 6f
                        drawLine(
                            Danger.copy(alpha = 0.20f * e),
                            androidx.compose.ui.geometry.Offset(glyphCenter.x - 14f, y),
                            androidx.compose.ui.geometry.Offset(glyphCenter.x + 14f, y + kotlin.math.sin(statePulse * 18f + idx) * 2.2f),
                            1.1f
                        )
                    }
                }
                GlowButtonState.ACHIEVEMENT -> {
                    repeat(6) { index ->
                        val a = index / 6f * 6.28318f + statePulse * 0.45f
                        val rr = 16f + statePulse * 3f
                        val c = androidx.compose.ui.geometry.Offset(
                            glyphCenter.x + kotlin.math.cos(a) * rr,
                            glyphCenter.y + kotlin.math.sin(a) * rr
                        )
                        drawCircle(Mint.copy(alpha = 0.52f * e), 1.7f, c)
                    }
                }
                GlowButtonState.IDLE -> Unit
            }

            drawLine(
                color = semanticAccent.copy(alpha = 0.60f * e),
                start = androidx.compose.ui.geometry.Offset(8f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width * (0.35f + 0.46f * e), 0f),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = semanticAccent.copy(alpha = 0.26f * e),
                start = androidx.compose.ui.geometry.Offset(size.width * 0.64f, size.height),
                end = androidx.compose.ui.geometry.Offset(size.width - 8f, size.height),
                strokeWidth = 1f
            )
            drawCircle(
                color = semanticAccent.copy(alpha = 0.18f * e),
                radius = 11f + 3f * e,
                center = androidx.compose.ui.geometry.Offset(size.width - 13f, size.height / 2f)
            )

            drawArc(
                semanticAccent.copy(alpha = 0.32f * e),
                -68f, 94f, false,
                androidx.compose.ui.geometry.Offset(size.width - 29f, size.height / 2f - 15f),
                androidx.compose.ui.geometry.Size(30f, 30f),
                style = Stroke(0.85f, cap = StrokeCap.Round)
            )
            drawLine(
                semanticAccent.copy(alpha = 0.16f * e),
                androidx.compose.ui.geometry.Offset(9f, size.height - 8f),
                androidx.compose.ui.geometry.Offset(size.width * (0.25f + e * 0.30f), size.height - 8f),
                1f
            )
            drawCircle(
                color = semanticAccent.copy(alpha = 0.78f * e),
                radius = 2.3f,
                center = androidx.compose.ui.geometry.Offset(size.width - 13f, size.height / 2f)
            )

            if (state == GlowButtonState.ACHIEVEMENT) {
                repeat(4) { index ->
                    val a = index / 4f * 6.28318f + statePulse
                    val c = androidx.compose.ui.geometry.Offset(
                        size.width * 0.22f + kotlin.math.cos(a) * 15f,
                        size.height / 2f + kotlin.math.sin(a) * 15f
                    )
                    drawCircle(semanticAccent.copy(alpha = 0.42f * e), 1.5f, c)
                }
            }
        }

        Text(
            text = text.uppercase(),
            color = if (enabled) Ink0 else Ink3,
            textAlign = TextAlign.Center,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
        }
    }
}

private fun glyphFromLabel(label: String): GlowGlyph {
    val s = label.uppercase()
    return when {
        s.contains("AÑADIR") || s.startsWith("+") || s.contains("NUEVO") -> GlowGlyph.PLUS
        s.contains("INICIAR") || s.contains("SIMULACIÓN") -> GlowGlyph.PLAY
        s.contains("ATRÁS") || s.contains("CANCELAR") -> GlowGlyph.BACK
        s.contains("SIGUIENTE") -> GlowGlyph.NEXT
        s.contains("GUARDAR") || s.contains("REGISTRAR") || s.contains("CREAR") -> GlowGlyph.SAVE
        s.contains("EDITAR") || s.contains("CAMBIAR") -> GlowGlyph.EDIT
        s.contains("VINCULAR") || s.contains("CÓDIGO") -> GlowGlyph.LINK
        s.contains("GUARDIÁN") || s.contains("ROOT") -> GlowGlyph.SHIELD
        s.contains("VERIFIC") -> GlowGlyph.CHECK
        s.contains("ADJUNTAR") || s.contains("IMAGEN") -> GlowGlyph.IMAGE
        s.contains("ABRIR") -> GlowGlyph.SHIELD
        s.contains("LIMPIAR") || s.contains("ELIMINAR") -> GlowGlyph.ALERT
        else -> GlowGlyph.GENERIC
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlowGlyph(
    glyph: GlowGlyph,
    color: Color,
    center: androidx.compose.ui.geometry.Offset,
    scale: Float,
    alpha: Float
) {
    val r = 9f * scale
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, w: Float = 1.8f) {
        drawLine(
            color.copy(alpha = alpha),
            androidx.compose.ui.geometry.Offset(center.x + x1, center.y + y1),
            androidx.compose.ui.geometry.Offset(center.x + x2, center.y + y2),
            strokeWidth = w,
            cap = StrokeCap.Round
        )
    }
    when (glyph) {
        GlowGlyph.PLUS, GlowGlyph.ADD -> { line(-r,0f,r,0f,2f); line(0f,-r,0f,r,2f) }
        GlowGlyph.PLAY -> {
            val p = Path().apply { moveTo(center.x - r*0.5f, center.y-r); lineTo(center.x+r, center.y); lineTo(center.x-r*0.5f, center.y+r); close() }
            drawPath(p, color.copy(alpha=alpha), style=Stroke(1.8f))
        }
        GlowGlyph.BACK -> { line(r*0.72f,0f,-r*0.58f,0f,2f); line(-r*0.58f,0f,0f,-r*0.58f,2f); line(-r*0.58f,0f,0f,r*0.58f,2f) }
        GlowGlyph.NEXT -> { line(-r*0.72f,0f,r*0.58f,0f,2f); line(r*0.58f,0f,0f,-r*0.58f,2f); line(r*0.58f,0f,0f,r*0.58f,2f) }
        GlowGlyph.SAVE -> {
            drawRoundRect(color.copy(alpha=alpha), topLeft=androidx.compose.ui.geometry.Offset(center.x-r,center.y-r), size=androidx.compose.ui.geometry.Size(2*r,2*r), cornerRadius=androidx.compose.ui.geometry.CornerRadius(3f,3f), style=Stroke(1.7f))
            line(-r*0.45f,-r*0.55f,r*0.45f,-r*0.55f,1.2f)
            line(-r*0.45f,r*0.22f,r*0.45f,r*0.22f,1.2f)
        }
        GlowGlyph.EDIT -> { line(-r*0.68f,r*0.55f,r*0.58f,-r*0.67f,2.1f); line(r*0.58f,-r*0.67f,r*0.82f,-r*0.42f,1.4f) }
        GlowGlyph.LINK -> {
            drawArc(color.copy(alpha=alpha), 205f, 140f, false, androidx.compose.ui.geometry.Offset(center.x-r*0.95f,center.y-r*0.55f), androidx.compose.ui.geometry.Size(r*1.2f,r*1.1f), style=Stroke(1.7f))
            drawArc(color.copy(alpha=alpha), 25f, 140f, false, androidx.compose.ui.geometry.Offset(center.x-r*0.25f,center.y-r*0.55f), androidx.compose.ui.geometry.Size(r*1.2f,r*1.1f), style=Stroke(1.7f))
        }
        GlowGlyph.SHIELD -> {
            val p=Path().apply{moveTo(center.x,center.y-r);lineTo(center.x+r*0.8f,center.y-r*0.42f);lineTo(center.x+r*0.64f,center.y+r*0.58f);lineTo(center.x,center.y+r);lineTo(center.x-r*0.64f,center.y+r*0.58f);lineTo(center.x-r*0.8f,center.y-r*0.42f);close()}
            drawPath(p,color.copy(alpha=alpha),style=Stroke(1.7f))
        }
        GlowGlyph.CHECK -> { line(-r*0.74f,0f,-r*0.15f,r*0.62f,2.1f); line(-r*0.15f,r*0.62f,r*0.9f,-r*0.72f,2.1f) }
        GlowGlyph.ALERT -> { drawCircle(color.copy(alpha=alpha),r*0.92f,center,style=Stroke(1.6f)); line(0f,-r*0.5f,0f,r*0.25f,2f); drawCircle(color.copy(alpha=alpha),1.6f,androidx.compose.ui.geometry.Offset(center.x,center.y+r*0.55f)) }
        GlowGlyph.DROP -> { drawCircle(color.copy(alpha=alpha),r*0.75f,center,style=Stroke(1.6f)); drawCircle(color.copy(alpha=alpha),r*0.22f,center) }
        GlowGlyph.IMAGE -> { drawRect(color.copy(alpha=alpha),topLeft=androidx.compose.ui.geometry.Offset(center.x-r,center.y-r*0.75f),size=androidx.compose.ui.geometry.Size(2*r,1.5f*r),style=Stroke(1.6f)); drawCircle(color.copy(alpha=alpha),2f,androidx.compose.ui.geometry.Offset(center.x+r*0.35f,center.y-r*0.32f)); line(-r*0.72f,r*0.40f,-r*0.1f,-r*0.12f,1.3f); line(-r*0.1f,-r*0.12f,r*0.72f,r*0.42f,1.3f) }
        GlowGlyph.BOOK -> { drawRoundRect(color.copy(alpha=alpha),topLeft=androidx.compose.ui.geometry.Offset(center.x-r,center.y-r),size=androidx.compose.ui.geometry.Size(2*r,2*r),cornerRadius=androidx.compose.ui.geometry.CornerRadius(3f,3f),style=Stroke(1.6f)); line(0f,-r,0f,r,1.0f) }
        GlowGlyph.GENERIC -> { drawCircle(color.copy(alpha=alpha),r*0.82f,center,style=Stroke(1.5f)); drawCircle(color.copy(alpha=alpha),2.2f,center) }
    }
}
