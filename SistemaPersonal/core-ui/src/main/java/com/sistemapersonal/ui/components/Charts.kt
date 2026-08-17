package com.sistemapersonal.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private fun Modifier.instrumentFrame(color: Color, energy: Float, density: Int = 12): Modifier = drawBehind {
    val e = energy.coerceIn(0f, 1.5f)
    val w = size.width
    val h = size.height
    val c = Offset(w / 2f, h / 2f)
    drawRect(color.copy(alpha = 0.035f + e * 0.02f), Offset(3.dp.toPx(), 3.dp.toPx()), androidx.compose.ui.geometry.Size(w - 6.dp.toPx(), h - 6.dp.toPx()), style = Stroke(1f))
    drawLine(color.copy(alpha = 0.35f), Offset(8f, 2f), Offset(w * 0.30f, 2f), 1.2f)
    drawLine(color.copy(alpha = 0.20f), Offset(w * 0.70f, h - 2f), Offset(w - 8f, h - 2f), 1f)
    repeat(density.coerceAtLeast(4)) { i ->
        val x = 8f + (w - 16f) * i / (density - 1).coerceAtLeast(1)
        val len = if (i % 3 == 0) 5f else 2.2f
        drawLine(color.copy(alpha = 0.10f + e * 0.025f), Offset(x, 4f), Offset(x, 4f + len), 0.75f)
    }
    drawCircle(color.copy(alpha = 0.14f + e * 0.05f), 4f, Offset(w - 8f, 8f))
    drawCircle(color.copy(alpha = 0.82f + e * 0.10f), 1.5f, Offset(w - 8f, 8f))
    drawCircle(color.copy(alpha = 0.08f), min(w, h) * 0.42f, c, style = Stroke(0.6f))
}

@Composable
fun DonutChart(value: Float, size: Dp = 110.dp, stroke: Dp = 9.dp, color: Color, label: String = "", sub: String = "") {
    val system = LocalVisualEnergy.current
    val progress by animateFloatAsState(value.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing), label = "donut_progress")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size).instrumentFrame(color, system.energy, 36)) {
            val d = this.size.minDimension - stroke.toPx()
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val topLeft = Offset(center.x - d / 2f, center.y - d / 2f)
            val ring = androidx.compose.ui.geometry.Size(d, d)
            repeat(36) { i ->
                val a = i / 36f * 360f - 90f
                val r = a * PI.toFloat() / 180f
                val ro = d * 0.59f
                val ri = if (i % 3 == 0) d * 0.51f else d * 0.545f
                drawLine(color.copy(alpha = if (i % 3 == 0) 0.32f else 0.10f), Offset(center.x + cos(r) * ri, center.y + sin(r) * ri), Offset(center.x + cos(r) * ro, center.y + sin(r) * ro), if (i % 3 == 0) 1.2f else 0.7f, StrokeCap.Round)
            }
            drawArc(color.copy(alpha = 0.10f), -90f, 360f, false, topLeft, ring, style = Stroke(stroke.toPx(), cap = StrokeCap.Round))
            drawArc(color.copy(alpha = 0.20f), -90f, 360f * progress, false, topLeft, ring, style = Stroke(stroke.toPx() + 4f, cap = StrokeCap.Round))
            drawArc(color, -90f, 360f * progress, false, topLeft, ring, style = Stroke(stroke.toPx(), cap = StrokeCap.Round))
            val a = (-90f + progress * 360f) * PI.toFloat() / 180f
            val tip = Offset(center.x + cos(a) * d * 0.48f, center.y + sin(a) * d * 0.48f)
            drawLine(color.copy(alpha = 0.8f), center, tip, 1.6f, StrokeCap.Round)
            drawCircle(color, 3f, center)
            drawCircle(color.copy(alpha = 0.16f), d * 0.22f, center, style = Stroke(1f))
        }
        if (label.isNotEmpty()) Text(label, color = color, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun BarChartSP(labels: List<String>, values: List<Float>, max: Float, color: Color, modifier: Modifier = Modifier.size(280.dp, 140.dp)) {
    val system = LocalVisualEnergy.current
    val reveal by animateFloatAsState(1f, tween(900, easing = FastOutSlowInEasing), label = "bar_reveal")
    Canvas(modifier.instrumentFrame(color, system.energy, 18)) {
        val n = values.size.coerceAtLeast(1)
        val gap = 8f
        val barW = (size.width - gap * (n + 1)) / n

        drawLine(color.copy(alpha = 0.28f), Offset(18f, 10f), Offset(18f, size.height - 16f), 1.2f)


        val scalePaint = Paint().apply {
            isAntiAlias = true
            textSize = 9f
              this.color = color.copy(alpha = 0.62f).toArgb()
            typeface = Typeface.MONOSPACE
        }
        repeat(5) { i ->
            val fraction = 1f - (i / 4f)
            val label = if (max > 0f) {
                val value = max * fraction
                if (max >= 10f) value.toInt().toString() else String.format(java.util.Locale.US, "%.1f", value)
            } else "0"
            val y = 10f + i * (size.height - 26f) / 4f
            drawContext.canvas.nativeCanvas.drawText(label, 1f, y + 3f, scalePaint)
        }

        repeat(5) { i ->
            val y = 10f + i * (size.height - 26f) / 4f
            drawLine(color.copy(alpha = if (i == 4) 0.20f else 0.08f), Offset(18f, y), Offset(size.width - 8f, y), 0.8f)
            drawLine(color.copy(alpha = 0.22f), Offset(13f, y), Offset(18f, y), 0.8f)
        }
        values.forEachIndexed { i, v ->
            val fraction = (v / max.coerceAtLeast(0.001f)).coerceIn(0f, 1f)
            val h = fraction * (size.height - 30f) * reveal
            val x = gap + i * (barW + gap) + 16f
            drawRoundRect(color.copy(alpha = 0.07f), Offset(x, 8f), androidx.compose.ui.geometry.Size(barW, size.height - 24f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
            drawRoundRect(color.copy(alpha = 0.72f), Offset(x, size.height - 16f - h), androidx.compose.ui.geometry.Size(barW, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            drawLine(color.copy(alpha = 0.45f), Offset(x - 2f, size.height - 16f - h), Offset(x + barW + 2f, size.height - 16f - h), 1f)
            drawCircle(color.copy(alpha = 0.84f), 1.8f, Offset(x + barW / 2f, size.height - 16f - h))
        }
    }
}

@Composable
fun RadarChartSP(labels: List<String>, values: List<Float>, max: Float, color: Color, size: Dp = 160.dp) {
    val system = LocalVisualEnergy.current
    Canvas(Modifier.size(size).instrumentFrame(color, system.energy, 24)) {
        val n = labels.size.coerceAtLeast(3)
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = min(cx, cy) * 0.72f
        fun point(i: Int, frac: Float): Offset {
            val a = -PI.toFloat() / 2f + i * (2 * PI.toFloat() / n)
            return Offset(cx + r * frac * cos(a), cy + r * frac * sin(a))
        }
        listOf(0.2f, 0.4f, 0.6f, 0.8f, 1f).forEach { f ->
            val p = Path()
            repeat(n) { i -> if (i == 0) p.moveTo(point(i, f).x, point(i, f).y) else p.lineTo(point(i, f).x, point(i, f).y) }
            p.close()
            drawPath(p, color.copy(alpha = 0.09f), style = Stroke(0.9f))
        }
        repeat(n) { i -> drawLine(color.copy(alpha = 0.14f), Offset(cx, cy), point(i, 1f), 0.8f) }
        val data = Path()
        values.forEachIndexed { i, v -> val p = point(i, (v / max.coerceAtLeast(0.001f)).coerceIn(0f, 1f)); if (i == 0) data.moveTo(p.x,p.y) else data.lineTo(p.x,p.y) }
        data.close()
        drawPath(data, color.copy(alpha = 0.13f))
        drawPath(data, color.copy(alpha = 0.88f), style = Stroke(1.9f, cap = StrokeCap.Round))
        values.forEachIndexed { i, v -> drawCircle(color, 2.5f, point(i, (v / max.coerceAtLeast(0.001f)).coerceIn(0f,1f))) }

        val sweep = ((system.eventToken % 240) / 240f) * 2f * PI.toFloat() - PI.toFloat()/2f
        drawLine(color.copy(alpha = 0.22f), Offset(cx,cy), Offset(cx + cos(sweep)*r, cy + sin(sweep)*r), 1.2f)
        drawCircle(color.copy(alpha = 0.24f), 4f, Offset(cx + cos(sweep)*r*0.82f, cy + sin(sweep)*r*0.82f))
        drawCircle(color, 2.2f, Offset(cx,cy))
    }
}

@Composable
fun WaveformChartSP(values: List<Float>, color: Color, modifier: Modifier = Modifier.size(300.dp, 90.dp)) {
    val system = LocalVisualEnergy.current
    Canvas(modifier.instrumentFrame(color, system.energy, 30)) {
        val mid = size.height / 2f
        drawLine(color.copy(alpha = 0.18f), Offset(16f, mid), Offset(size.width - 8f, mid), 1f)
        repeat(10) { i ->
            val x = 16f + i * (size.width - 24f) / 9f
            drawLine(color.copy(alpha = if (i % 2 == 0) 0.18f else 0.09f), Offset(x, mid - 7f), Offset(x, mid + 7f), 0.8f)
        }
        if (values.isEmpty()) return@Canvas
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = 16f + (i.toFloat() / values.lastIndex.coerceAtLeast(1)) * (size.width - 24f)
            val y = mid - v.coerceIn(-1f,1f) * size.height * 0.36f
            if (i == 0) path.moveTo(x,y) else path.lineTo(x,y)
        }
        drawPath(path, color.copy(alpha = 0.10f), style = Stroke(6f, cap = StrokeCap.Round))
        drawPath(path, color.copy(alpha = 0.80f), style = Stroke(1.9f, cap = StrokeCap.Round))
        val triggerX = size.width * 0.34f
        drawLine(color.copy(alpha = 0.34f), Offset(triggerX, 6f), Offset(triggerX, size.height - 6f), 1f)
        val last = values.last().coerceIn(-1f,1f)
        val endY = mid - last * size.height * 0.36f
        drawCircle(color.copy(alpha = 0.24f), 6f, Offset(size.width - 8f, endY))
        drawCircle(color, 2f, Offset(size.width - 8f, endY))
    }
}

@Composable
fun HeatmapSP(values: List<Float>, columns: Int, rows: Int, color: Color, modifier: Modifier = Modifier.size(240.dp, 96.dp)) {
    val system = LocalVisualEnergy.current
    Canvas(modifier.instrumentFrame(color, system.energy, (columns.coerceAtLeast(2) + 4))) {
        if (columns <= 0 || rows <= 0) return@Canvas
        val cellW = (size.width - 12f) / columns
        val cellH = (size.height - 10f) / rows
        values.take(columns * rows).forEachIndexed { index, value ->
            val col = index % columns
            val row = index / columns
            val intensity = value.coerceIn(0f,1f)
            drawRoundRect(color.copy(alpha = 0.04f + intensity * 0.72f), Offset(6f + col*cellW + 1.5f, 5f + row*cellH + 1.5f), androidx.compose.ui.geometry.Size(cellW-3f, cellH-3f), androidx.compose.ui.geometry.CornerRadius(2.5f,2.5f))
        }

        val probe = ((system.eventToken % columns.coerceAtLeast(1)).toFloat() / columns.coerceAtLeast(1))
        val px = 6f + probe * (size.width - 12f)
        drawLine(color.copy(alpha = 0.30f), Offset(px, 4f), Offset(px, size.height - 4f), 1.1f)
        val rowProbe = (system.eventToken / 2 % rows.coerceAtLeast(1)).toFloat() / rows.coerceAtLeast(1)
        val py = 5f + rowProbe * (size.height - 10f)
        drawLine(color.copy(alpha = 0.16f), Offset(4f, py), Offset(size.width - 4f, py), 0.9f)
        drawCircle(color.copy(alpha = 0.32f), 4f, Offset(px, py), style = Stroke(1f))
        drawCircle(color, 1.8f, Offset(px,py))
    }
}
