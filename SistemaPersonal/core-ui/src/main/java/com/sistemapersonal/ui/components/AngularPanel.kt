package com.sistemapersonal.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.*

@Composable
fun AngularPanel(
    modifier: Modifier = Modifier,
    fill: Color = PanelFill,
    borderColor: Color = Hairline,
    glowColor: Color? = null,
    assemblyKey: Any = Unit,
    content: @Composable () -> Unit
) {
    val accent = glowColor ?: borderColor
    val transition = rememberInfiniteTransition(label = "panel_dynamics")
    val sweep by transition.animateFloat(-0.15f, 1.15f, infiniteRepeatable(tween(6200, easing = LinearEasing)), label = "sweep")
    val breathe by transition.animateFloat(0.90f, 1.08f, infiniteRepeatable(tween(2900), RepeatMode.Reverse), label = "breathe")
    val system = LocalVisualEnergy.current
    val resolvedAssemblyKey = arrayOf(assemblyKey, system.transitionToken).contentHashCode()
    val assemblyProgress = remember(resolvedAssemblyKey) { Animatable(0f) }
    LaunchedEffect(resolvedAssemblyKey) {
        assemblyProgress.snapTo(0f)
        assemblyProgress.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
    }
    val assembly = assemblyProgress.value
    val emphasized = glowColor != null

    Box(
        modifier = modifier.shadow(
            elevation = if (emphasized) 22.dp else 6.dp,
            shape = AngularPanelShape,
            ambientColor = accent.copy(alpha = if (emphasized) 0.38f else 0.10f),
            spotColor = accent.copy(alpha = if (emphasized) 0.30f else 0.08f)
        )
    ) {




        if (emphasized) {
            Box(
                Modifier.matchParentSize().drawBehind {
                    drawChromaticFringe(accent, AngularPanelShape, breathe)
                }
            )
        }
        Box(
            Modifier.matchParentSize()
                .blur(0.45.dp)
                .background(
                    Brush.linearGradient(
                        0f to fill.copy(alpha = 0.985f),
                        0.48f to Bg1.copy(alpha = 0.94f),
                        1f to Bg0.copy(alpha = 0.90f)
                    ), AngularPanelShape
                )
                .border(1.dp, borderColor.copy(alpha = if (emphasized) 0.72f else 0.56f), AngularPanelShape)
                .drawBehind { drawPanelAssembly(accent, sweep, breathe, assembly, emphasized) }
        )
        Box(
            Modifier.matchParentSize().drawBehind { drawPanelDepth(accent, sweep, breathe, system.energy, system.interactionEnergy) }
        )
        Box(Modifier.padding(16.dp)) { content() }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPanelAssembly(
    accent: Color, sweep: Float, breathe: Float, assembly: Float, emphasized: Boolean
) {
    val w = size.width; val h = size.height
    val e = if (emphasized) 0.82f else 0.44f
    val c = accent.copy(alpha = e)

    drawLine(c.copy(alpha = c.alpha * 0.82f), androidx.compose.ui.geometry.Offset(12f, 0f), androidx.compose.ui.geometry.Offset(w * 0.30f, 0f), 1.6f)
    drawLine(c.copy(alpha = c.alpha * 0.55f), androidx.compose.ui.geometry.Offset(w * 0.70f, h), androidx.compose.ui.geometry.Offset(w - 12f, h), 1.1f)
    drawLine(c.copy(alpha = c.alpha * 0.40f), androidx.compose.ui.geometry.Offset(0f, h * 0.62f), androidx.compose.ui.geometry.Offset(0f, h * 0.22f), 1f)

    val corner = 22f + 28f * assembly
    drawLine(c.copy(alpha = 0.55f), androidx.compose.ui.geometry.Offset(1f, corner), androidx.compose.ui.geometry.Offset(1f, 4f), 1.1f)
    drawLine(c.copy(alpha = 0.55f), androidx.compose.ui.geometry.Offset(1f, 4f), androidx.compose.ui.geometry.Offset(corner, 4f), 1.1f)
    drawLine(c.copy(alpha = 0.42f), androidx.compose.ui.geometry.Offset(w - corner, h - 4f), androidx.compose.ui.geometry.Offset(w - 4f, h - 4f), 1.1f)
    drawLine(c.copy(alpha = 0.42f), androidx.compose.ui.geometry.Offset(w - 4f, h - 4f), androidx.compose.ui.geometry.Offset(w - 4f, h - corner), 1.1f)

    val x = (w + 64f) * sweep - 32f
    drawRect(
        Brush.horizontalGradient(listOf(Color.Transparent, accent.copy(alpha = 0.055f * breathe), accent.copy(alpha = 0.012f), Color.Transparent)),
        androidx.compose.ui.geometry.Offset(x, 2f), androidx.compose.ui.geometry.Size(64f, h - 4f)
    )

    drawLine(accent.copy(alpha = 0.18f), androidx.compose.ui.geometry.Offset(w - 28f, 8f), androidx.compose.ui.geometry.Offset(w - 8f, 8f), 0.8f)
    drawLine(accent.copy(alpha = 0.16f), androidx.compose.ui.geometry.Offset(w - 8f, 8f), androidx.compose.ui.geometry.Offset(w - 8f, 28f), 0.8f)
}







private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChromaticFringe(
    accent: Color,
    shape: androidx.compose.ui.graphics.Shape,
    breathe: Float
) {
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = (outline as? androidx.compose.ui.graphics.Outline.Generic)?.path ?: return
    val strokeStyle = androidx.compose.ui.graphics.drawscope.Stroke(1.1f)
    val offset = 1.1f * breathe

    translate(left = -offset, top = 0f) {
        drawPath(path, Red.copy(alpha = 0.22f), style = strokeStyle)
    }
    translate(left = offset, top = 0f) {
        drawPath(path, Cyan.copy(alpha = 0.22f), style = strokeStyle)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPanelDepth(accent: Color, sweep: Float, breathe: Float, energy: Float, interaction: Float) {
    val w = size.width; val h = size.height
    drawPath(
        Path().apply { moveTo(9f,1f); lineTo(w-9f,1f); lineTo(w-1f,9f); lineTo(w-1f,h-9f); lineTo(w-9f,h-1f); lineTo(9f,h-1f); lineTo(1f,h-9f); lineTo(1f,9f); close() },
        accent.copy(alpha = (0.035f + 0.026f * energy + 0.025f * interaction) * breathe),
        style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f)
    )
    val x = (w + 30f) * sweep
    drawLine(accent.copy(alpha = 0.08f), androidx.compose.ui.geometry.Offset(x, 4f), androidx.compose.ui.geometry.Offset(x, h-4f), 2.4f)
}
