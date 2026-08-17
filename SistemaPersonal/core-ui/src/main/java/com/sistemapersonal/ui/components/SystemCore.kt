package com.sistemapersonal.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SystemCore(
    accent: Color,
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    density: Float = 1f,
    seed: Int = 7
) {
    val system = LocalVisualEnergy.current
    val transition = rememberInfiniteTransition(label = "system_core")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
        label = "core_spin"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "core_pulse"
    )
    val orbitDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing)),
        label = "core_orbit"
    )

    val particles = androidx.compose.runtime.remember(seed, density) {
        val random = Random(seed)
        List((44 * density).toInt().coerceIn(18, 96)) {
            Triple(random.nextFloat(), random.nextFloat(), random.nextFloat())
        }
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val base = minOf(size.width, size.height) * 0.5f
        val glowAlpha = (0.18f * intensity).coerceIn(0f, 0.48f)

        drawCircle(
            brush = Brush.radialGradient(
                0f to accent.copy(alpha = glowAlpha * 2.8f),
                0.32f to accent.copy(alpha = glowAlpha),
                0.72f to accent.copy(alpha = glowAlpha * 0.14f),
                1f to Color.Transparent
            ),
            radius = base * 0.96f,
            center = Offset(cx, cy)
        )

        for (ring in 0..5) {
            val radius = base * (0.24f + ring * 0.135f)
            val alpha = (0.27f - ring * 0.034f) * intensity
            drawCircle(
                color = accent.copy(alpha = alpha.coerceAtLeast(0.018f)),
                radius = radius * if (ring == 0) pulse else 1f,
                center = Offset(cx, cy),
                style = Stroke(
                    width = if (ring == 0) 2.5f else 1.05f,
                    cap = StrokeCap.Round
                )
            )
        }

        val arcRadius = base * 0.58f
        drawArc(
            color = accent.copy(alpha = (0.86f * intensity).coerceIn(0f, 1f)),
            startAngle = spin,
            sweepAngle = 92f,
            useCenter = false,
            topLeft = Offset(cx - arcRadius, cy - arcRadius),
            size = androidx.compose.ui.geometry.Size(arcRadius * 2f, arcRadius * 2f),
            style = Stroke(2.35f, cap = StrokeCap.Round)
        )
        drawArc(
            color = accent.copy(alpha = (0.42f * intensity).coerceIn(0f, 1f)),
            startAngle = spin + 160f,
            sweepAngle = 52f,
            useCenter = false,
            topLeft = Offset(cx - arcRadius * 1.16f, cy - arcRadius * 1.16f),
            size = androidx.compose.ui.geometry.Size(arcRadius * 2.32f, arcRadius * 2.32f),
            style = Stroke(1.15f, cap = StrokeCap.Round)
        )

        particles.forEachIndexed { index, (radiusSeed, angleSeed, alphaSeed) ->
            val radius = base * (0.36f + radiusSeed * 0.62f)
            val direction = if (index % 2 == 0) 1f else -1f
            val angle = angleSeed * Math.PI * 2 +
                spin * 0.0012f * direction +
                orbitDrift * (0.24f + (index % 5) * 0.05f)
            val x = cx + cos(angle).toFloat() * radius
            val y = cy + sin(angle).toFloat() * radius
            val dot = (1.0f + alphaSeed * 1.8f).dp.toPx()
            drawCircle(
                accent.copy(alpha = ((0.06f + alphaSeed * 0.34f) * intensity).coerceIn(0f, 1f)),
                dot,
                Offset(x, y)
            )
        }

        val coreRadius = base * 0.095f * pulse
        drawCircle(accent.copy(alpha = 0.18f * intensity), coreRadius * 2.8f, Offset(cx, cy))
        drawCircle(accent.copy(alpha = 0.92f * intensity), coreRadius, Offset(cx, cy))
        drawCircle(com.sistemapersonal.ui.theme.Ink0.copy(alpha = 0.58f * intensity), coreRadius * 0.26f, Offset(cx, cy))

        if (system.glitchLevel > 0.02f) {
            val g = system.glitchLevel.coerceIn(0f, 1f)
            repeat(5) { index ->
                val a = spin * 0.01745f + index * 1.257f
                val rr = base * (0.68f + index * 0.035f)
                val start = Offset(cx + cos(a).toFloat() * rr, cy + sin(a).toFloat() * rr)
                val end = Offset(cx + cos(a + 0.16f).toFloat() * rr, cy + sin(a + 0.16f).toFloat() * rr)
                drawLine(accent.copy(alpha = 0.12f * g * intensity), start, end, 1.8f)
            }
        }
    }
}

@Composable
fun ModuleTransition(
    transitionKey: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val system = LocalVisualEnergy.current
    val progress = remember { Animatable(1f) }
    val seed = remember(transitionKey) { transitionKey.hashCode() }
    val glitchSeeds = remember(transitionKey) {
        val random = Random(seed)
        List(14) { Triple(random.nextFloat(), random.nextFloat(), random.nextFloat()) }
    }

    LaunchedEffect(transitionKey) {
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            tween(680, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val reveal = ((p - 0.08f) / 0.84f).coerceIn(0f, 1f)
        val fade = when {
            p < 0.14f -> p / 0.14f
            p > 0.88f -> (1f - p) / 0.12f
            else -> 1f
        }.coerceIn(0f, 1f)

        if (p in 0.08f..0.20f) {
            drawRect(accent.copy(alpha = 0.025f * ((0.20f - p) / 0.12f)))
        }

        val xNear = size.width * (p * 1.16f - 0.08f)
        drawLine(
            color = accent.copy(alpha = 0.90f * fade),
            start = Offset(xNear, 0f),
            end = Offset(xNear, size.height),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent.copy(alpha = 0.18f * fade),
            start = Offset(xNear - 18f, 0f),
            end = Offset(xNear - 18f, size.height),
            strokeWidth = 7f
        )

        repeat(9) { index ->
            val normalized = index / 9f
            val y = size.height * (0.08f + normalized * 0.84f)
            val width = (26f + index * 12f) * (0.35f + fade * 0.65f)
            val skew = ((index % 2) * 2 - 1) * 6f
            drawLine(
                color = accent.copy(alpha = 0.20f * fade),
                start = Offset(xNear - width + skew, y),
                end = Offset(xNear + width, y + skew),
                strokeWidth = if (index % 3 == 0) 1.4f else 0.7f,
                cap = StrokeCap.Round
            )
        }

        if (p in 0.23f..0.72f || system.glitchLevel > 0.25f) {
            glitchSeeds.forEachIndexed { index, (_, ySeed, widthSeed) ->
                val wobble = sin((p * 24f + index) * 2f) * 2.2f
                val y = (ySeed * size.height + wobble) % size.height
                val width = 12f + widthSeed * 68f
                val x = xNear + ((index % 5) - 2) * 42f
                val alpha = 0.035f + (1f - kotlin.math.abs(p - 0.5f) * 2f).coerceIn(0f, 1f) * (0.08f + system.glitchLevel * 0.08f)
                drawRect(
                    color = accent.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(width, if (index % 3 == 0) 2.2f else 1f)
                )
            }
        }

        if (p > 0.72f) {
            val edge = size.width * reveal
            drawLine(
                color = accent.copy(alpha = 0.42f * fade),
                start = Offset(edge, 0f),
                end = Offset(edge, size.height),
                strokeWidth = 0.8f
            )
        }
    }
}
