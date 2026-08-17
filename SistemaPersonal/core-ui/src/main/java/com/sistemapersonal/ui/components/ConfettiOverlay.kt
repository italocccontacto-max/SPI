package com.sistemapersonal.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Float,
    val radius: Float,
    val speed: Float,
    val size: Float,
    val type: Int,
    val phase: Float,
    val orbit: Float
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    cantidad: Int = 52,
    accent: androidx.compose.ui.graphics.Color = com.sistemapersonal.ui.theme.Mint,
    onFin: () -> Unit = {}
) {
    val particles = remember {
        val random = Random(4242)
        List(cantidad) {
            Particle(
                angle = random.nextFloat() * 6.28318f,
                radius = 0.12f + random.nextFloat() * 0.86f,
                speed = 0.58f + random.nextFloat() * 0.96f,
                size = 1.2f + random.nextFloat() * 4.8f,
                type = random.nextInt(4),
                phase = random.nextFloat(),
                orbit = -0.7f + random.nextFloat() * 1.4f
            )
        }
    }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2100, easing = FastOutSlowInEasing),
        label = "celebration_particles"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2100)
        onFin()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(size.width, size.height) * 0.60f
        val fade = if (progress > 0.72f) ((1f - progress) / 0.28f) else 1f
        val ignition = (progress / 0.20f).coerceIn(0f, 1f)
        val ringScale = FastOutSlowInEasing.transform(progress.coerceIn(0f, 1f))

        drawCircle(
            brush = Brush.radialGradient(
                0f to accent.copy(alpha = 0.26f * fade),
                0.25f to accent.copy(alpha = 0.13f * fade),
                0.58f to accent.copy(alpha = 0.03f * fade),
                1f to androidx.compose.ui.graphics.Color.Transparent
            ),
            radius = maxRadius * (0.32f + 0.22f * ignition),
            center = center
        )

        repeat(3) { index ->
            val ringProgress = ((progress - index * 0.10f) / 0.90f).coerceIn(0f, 1f)
            drawCircle(
                color = accent.copy(alpha = 0.30f * fade * (1f - ringProgress)),
                radius = maxRadius * ringProgress,
                center = center,
                style = Stroke(width = 1.4f + index * 0.9f)
            )
        }

        if (progress < 0.18f) {
            val flash = (1f - progress / 0.18f)
            drawCircle(
                color = accent.copy(alpha = 0.06f * flash),
                radius = maxRadius * (0.08f + progress * 0.80f),
                center = center
            )
        }

        repeat(16) { index ->
            val a = index / 16f * 6.28318f + progress * 0.62f
            val startR = maxRadius * (0.07f + 0.05f * ignition)
            val endR = maxRadius * (0.22f + 0.24f * ringScale)
            drawLine(
                color = accent.copy(alpha = 0.22f * fade),
                start = Offset(center.x + cos(a) * startR, center.y + sin(a) * startR),
                end = Offset(center.x + cos(a) * endR, center.y + sin(a) * endR),
                strokeWidth = if (index % 4 == 0) 1.8f else 1.0f,
                cap = StrokeCap.Round
            )
        }

        particles.forEachIndexed { index, p ->
            val t = ((progress - p.phase * 0.14f) / 0.86f).coerceIn(0f, 1f)
            val orbit = p.angle + progress * p.speed * p.orbit
            val r = maxRadius * p.radius * (0.28f + 0.72f * t)
            val x = center.x + cos(orbit) * r
            val y = center.y + sin(orbit) * r
            val localAlpha = fade * (1f - t * 0.28f)
            val size = p.size * (1f + 0.38f * t)

            when (p.type) {
                0 -> drawCircle(
                    accent.copy(alpha = localAlpha * 0.78f),
                    size,
                    Offset(x, y)
                )
                1 -> {
                    val dx = cos(orbit + 1.5708f) * size * 2.4f
                    val dy = sin(orbit + 1.5708f) * size * 2.4f
                    drawLine(
                        accent.copy(alpha = localAlpha * 0.84f),
                        Offset(x - dx, y - dy),
                        Offset(x + dx, y + dy),
                        1.2f,
                        StrokeCap.Round
                    )
                }
                2 -> {
                    val path = Path().apply {
                        moveTo(x, y - size * 1.6f)
                        lineTo(x + size * 1.1f, y + size)
                        lineTo(x - size * 1.1f, y + size)
                        close()
                    }
                    drawPath(path, accent.copy(alpha = localAlpha * 0.72f), style = Stroke(1f))
                }
                else -> {
                    drawRoundRect(
                        color = accent.copy(alpha = localAlpha * 0.66f),
                        topLeft = Offset(x - size * 1.35f, y - size * 0.42f),
                        size = androidx.compose.ui.geometry.Size(size * 2.7f, size * 0.84f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
                    )
                }
            }
        }
    }
}
