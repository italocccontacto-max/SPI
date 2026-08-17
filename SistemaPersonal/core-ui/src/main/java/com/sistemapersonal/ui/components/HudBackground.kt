package com.sistemapersonal.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.Bg0
import com.sistemapersonal.ui.theme.Bg1
import com.sistemapersonal.ui.theme.Bg2
import com.sistemapersonal.ui.theme.Hairline
import com.sistemapersonal.ui.theme.Void
import kotlin.math.abs
import kotlin.random.Random







private val grainTile: ImageBitmap by lazy {
    val size = 128
    val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ALPHA_8)
    val rnd = Random(52411)
    for (y in 0 until size) {
        for (x in 0 until size) {
            val v = (rnd.nextFloat() * 255).toInt().coerceIn(0, 255)
            bmp.setPixel(x, y, (v shl 24))
        }
    }
    bmp.asImageBitmap()
}

private fun DrawScope.drawFilmGrain(alpha: Float = 0.035f, tint: Color = Color.White) {
    val paint = android.graphics.Paint().apply {
        shader = android.graphics.BitmapShader(
            grainTile.asAndroidBitmap(),
            android.graphics.Shader.TileMode.REPEAT,
            android.graphics.Shader.TileMode.REPEAT
        )
        colorFilter = android.graphics.PorterDuffColorFilter(
            tint.copy(alpha = alpha).toArgb(),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    drawContext.canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
}

@Composable
fun HudBackground(
    modifier: Modifier = Modifier,
    accent: Color = com.sistemapersonal.ui.theme.Amber,
    heroScale: Float = 0.84f,
    heroAlpha: Float = 0.16f,
    content: @Composable () -> Unit
) {
    val scroll = remember { mutableFloatStateOf(0f) }
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                scroll.floatValue = (scroll.floatValue + available.y * 0.14f).coerceIn(-48f, 48f)
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "hud_atmosphere")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(18000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "hud_drift"
    )
    val scan by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(6200, easing = LinearEasing)
        ),
        label = "hud_scan"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(3600),
            RepeatMode.Reverse
        ),
        label = "hud_pulse"
    )

    val particleSeeds = remember {
        List(96) { Random(8147 + it).nextFloat() }
    }
    val microSeeds = remember {
        List(42) { Random(17331 + it).nextFloat() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection)
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = drift * 3.5f - scroll.floatValue * 0.16f
                    scaleX = 1.018f
                    scaleY = 1.018f
                }
        ) {
            drawAtmosphereVolume(accent, pulse, microSeeds)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = drift * 5.5f - scroll.floatValue * 0.42f
                }
        ) {
            drawPerspectiveGrid(accent, scroll.floatValue)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = drift * 8.5f - scroll.floatValue * 0.92f
                }
        ) {
            drawAtmosphereDetail(accent, scan, particleSeeds)
        }



        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFilmGrain(alpha = 0.030f)
        }

        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAtmosphereVolume(
    accent: Color,
    pulse: Float,
    microSeeds: List<Float>
) {
    drawRect(Bg0)

    val center = androidx.compose.ui.geometry.Offset(
        size.width * (0.53f + pulse * 0.012f),
        size.height * (0.26f + (1.08f - pulse) * 0.035f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            0f to accent.copy(alpha = 0.15f),
            0.28f to accent.copy(alpha = 0.055f),
            0.62f to Bg1.copy(alpha = 0.28f),
            1f to Color.Transparent
        ),
        radius = size.minDimension * 0.74f,
        center = center
    )

    repeat(4) { index ->
        val y = size.height * (0.10f + index * 0.19f)
        val bandAlpha = 0.018f + index * 0.004f
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    accent.copy(alpha = bandAlpha),
                    Color.Transparent
                )
            ),
            topLeft = androidx.compose.ui.geometry.Offset(0f, y),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.09f)
        )
    }

    microSeeds.forEachIndexed { index, seed ->
        val x = ((seed * 1.71f + index * 0.021f) % 1f) * size.width
        val y = ((seed * 0.57f + index * 0.047f) % 1f) * size.height
        val r = 0.25f + ((seed * 3.7f) % 1f) * 0.85f
        drawCircle(
            color = accent.copy(alpha = 0.012f + ((seed * 9f) % 1f) * 0.022f),
            radius = r,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPerspectiveGrid(
    accent: Color,
    parallax: Float
) {
    val step = 38.dp.toPx()
    val vanishingX = size.width * 0.54f
    val vanishingY = size.height * 0.17f + parallax * 0.35f

    var x = -size.width
    while (x <= size.width * 2f) {
        val targetX = vanishingX + (x - vanishingX) * 0.28f
        drawLine(
            color = Hairline.copy(alpha = 0.13f),
            start = androidx.compose.ui.geometry.Offset(targetX, vanishingY),
            end = androidx.compose.ui.geometry.Offset(x, size.height),
            strokeWidth = 0.75f
        )
        x += step
    }

    var y = vanishingY
    while (y < size.height * 1.08f) {
        val normalized = ((y - vanishingY) / (size.height - vanishingY)).coerceIn(0f, 1f)
        drawLine(
            color = Hairline.copy(alpha = 0.09f + normalized * 0.075f),
            start = androidx.compose.ui.geometry.Offset(0f, y),
            end = androidx.compose.ui.geometry.Offset(size.width, y),
            strokeWidth = 0.7f
        )
        y += step * (0.54f + normalized * 0.62f)
    }

    repeat(5) { index ->
        val y = size.height * (0.18f + index * 0.15f)
        val span = size.width * (0.24f + index * 0.055f)
        drawLine(
            color = accent.copy(alpha = 0.018f + index * 0.003f),
            start = androidx.compose.ui.geometry.Offset(size.width * 0.12f, y),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.12f + span, y),
            strokeWidth = 1f
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAtmosphereDetail(
    accent: Color,
    scan: Float,
    particleSeeds: List<Float>
) {


    val scanPhase = (scan * (size.height + 24.dp.toPx())) % (size.height + 24.dp.toPx())
    repeat(2) { band ->
        val y = scanPhase - band * 12.dp.toPx()
        if (y in 0f..size.height) {
            drawLine(
                color = accent.copy(alpha = if (band == 0) 0.042f else 0.018f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = if (band == 0) 1.2f else 0.7f
            )
        }
    }



    var scanY = 0f
    while (scanY < size.height) {
        drawLine(
            color = Void.copy(alpha = 0.16f),
            start = androidx.compose.ui.geometry.Offset(0f, scanY),
            end = androidx.compose.ui.geometry.Offset(size.width, scanY),
            strokeWidth = 1f
        )
        scanY += 4.dp.toPx()
    }

    particleSeeds.forEachIndexed { index, seed ->
        val x = ((seed * 1.73f + index * 0.013f) % 1f) * size.width
        val y = ((seed * 0.93f + index * 0.071f) % 1f) * size.height
        val alpha = 0.014f + ((seed * 7f) % 1f) * 0.054f
        drawCircle(
            color = accent.copy(alpha = alpha),
            radius = 0.55f + (seed % 1f) * 1.4f,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }

    drawRect(
        brush = Brush.radialGradient(
            0f to Color.Transparent,
            0.56f to Color.Transparent,
            1f to Void.copy(alpha = 0.74f)
        )
    )
}
