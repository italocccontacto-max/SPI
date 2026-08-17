package com.sistemapersonal.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sistemapersonal.model.Modulo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ModuleHero(
    modulo: Modulo,
    accent: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 0.34f,
    scale: Float = 1f
) {
    val system = LocalVisualEnergy.current
    val transition = rememberInfiniteTransition(label = "module_hero_${modulo.id}")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "hero_rotation_${modulo.id}"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse),
        label = "hero_breathe_${modulo.id}"
    )
    val scan by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "hero_scan_${modulo.id}"
    )
    val entry by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(850),
        label = "hero_entry"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            rotationZ = rotation * if (modulo.ordinal % 2 == 0) 0.025f else -0.018f
            scaleX = scale * (0.98f + 0.025f * breathe) * entry
            scaleY = scale * (0.98f + 0.025f * breathe) * entry
            this.alpha = alpha
        }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(size.width, size.height) * 0.39f
        val dim = accent.copy(alpha = 0.10f)
        val hair = accent.copy(alpha = 0.20f)
        val strong = accent.copy(alpha = 0.72f)

        drawCircle(
            brush = Brush.radialGradient(
                0f to accent.copy(alpha = 0.18f * breathe),
                0.36f to accent.copy(alpha = 0.065f),
                1f to Color.Transparent
            ),
            radius = r * 1.55f,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )





        val fringeR = r * 0.97f
        drawCircle(
            color = com.sistemapersonal.ui.theme.Red.copy(alpha = 0.16f * breathe),
            radius = fringeR,
            center = androidx.compose.ui.geometry.Offset(cx - 1.1f, cy),
            style = Stroke(1f)
        )
        drawCircle(
            color = com.sistemapersonal.ui.theme.Cyan.copy(alpha = 0.16f * breathe),
            radius = fringeR,
            center = androidx.compose.ui.geometry.Offset(cx + 1.1f, cy),
            style = Stroke(1f)
        )

        repeat(4) { ring ->
            drawCircle(
                color = accent.copy(alpha = 0.055f + ring * 0.012f),
                radius = r * (0.52f + ring * 0.13f),
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = Stroke(if (ring == 0) 1.8f else 0.8f)
            )
        }

        when (modulo) {
            Modulo.INICIO -> drawCore(cx, cy, r, accent, rotation, breathe, scan)
            Modulo.CONSTITUCION -> drawShield(cx, cy, r, accent, breathe)
            Modulo.IDENTIDAD -> drawOrbit(cx, cy, r, accent, rotation, breathe)
            Modulo.ANTIIDENTIDAD -> drawBrokenCore(cx, cy, r, accent, rotation, breathe)
            Modulo.DIRECCION -> drawCompass(cx, cy, r, accent, rotation, breathe)
            Modulo.OBJETIVOS -> drawTarget(cx, cy, r, accent, breathe, scan)
            Modulo.PUD -> drawWaveGrid(cx, cy, r, accent, scan)
            Modulo.PROTOCOLOS -> drawProtocolMatrix(cx, cy, r, accent, scan)
            Modulo.EJECUCION -> drawExecution(cx, cy, r, accent, rotation)
            Modulo.EVOLUCION -> drawGrowth(cx, cy, r, accent, rotation, breathe)
            Modulo.BIBLIOTECA -> drawLibrary(cx, cy, r, accent, breathe)
            Modulo.GUARDIAN -> drawGuardian(cx, cy, r, accent, rotation, scan)
        }

        drawCircle(
            color = accent.copy(alpha = 0.038f),
            radius = r * (1.34f + 0.02f * breathe),
            center = androidx.compose.ui.geometry.Offset(cx + r * 0.08f, cy - r * 0.06f),
            style = Stroke(0.7f)
        )
        drawCircle(
            color = accent.copy(alpha = 0.022f),
            radius = r * 1.48f,
            center = androidx.compose.ui.geometry.Offset(cx - r * 0.06f, cy + r * 0.04f),
            style = Stroke(0.55f)
        )
        repeat(12) { i ->
            val a = i / 12f * 6.28318f + rotation * 0.002f
            val rr = r * (1.10f + (i % 3) * 0.055f)
            val dot = androidx.compose.ui.geometry.Offset(cx + kotlin.math.cos(a) * rr, cy + kotlin.math.sin(a) * rr)
            drawCircle(accent.copy(alpha = 0.08f + if (i % 4 == 0) 0.08f else 0f), if (i % 4 == 0) 1.4f else 0.7f, dot)
        }

        drawLine(
            dim,
            androidx.compose.ui.geometry.Offset(cx - r * 1.08f, cy),
            androidx.compose.ui.geometry.Offset(cx - r * 0.88f, cy),
            1f
        )
        drawLine(
            dim,
            androidx.compose.ui.geometry.Offset(cx + r * 0.88f, cy),
            androidx.compose.ui.geometry.Offset(cx + r * 1.08f, cy),
            1f
        )
        drawLine(
            dim,
            androidx.compose.ui.geometry.Offset(cx, cy - r * 1.08f),
            androidx.compose.ui.geometry.Offset(cx, cy - r * 0.88f),
            1f
        )
        drawLine(
            strong,
            androidx.compose.ui.geometry.Offset(cx, cy + r * 0.88f),
            androidx.compose.ui.geometry.Offset(cx, cy + r * 1.06f),
            1f
        )

        if (system.glitchLevel > 0.02f) {
            val g = system.glitchLevel.coerceIn(0f, 1f)
            repeat(3 + (g * 5f).toInt()) { index ->
                val gy = cy + (index - 2) * r * 0.12f + sin(system.eventToken * 0.7f + index) * r * 0.04f
                val gx = cx - r * (0.78f - (index % 4) * 0.23f)
                drawLine(
                    accent.copy(alpha = 0.09f * g),
                    androidx.compose.ui.geometry.Offset(gx, gy),
                    androidx.compose.ui.geometry.Offset(gx + r * (0.16f + index * 0.04f), gy),
                    1f
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCore(
    cx: Float, cy: Float, r: Float, color: Color, rotation: Float, pulse: Float, scan: Float
) {
    drawCircle(color.copy(alpha = 0.14f), r * 0.28f * pulse, androidx.compose.ui.geometry.Offset(cx, cy))
    drawCircle(color.copy(alpha = 0.9f), r * 0.12f * pulse, androidx.compose.ui.geometry.Offset(cx, cy))
    drawArc(color.copy(alpha = 0.9f), rotation, 78f, false, androidx.compose.ui.geometry.Offset(cx-r,cy-r), androidx.compose.ui.geometry.Size(r*2f,r*2f), style=Stroke(2.2f, cap=StrokeCap.Round))
    drawArc(color.copy(alpha = 0.48f), rotation + 164f, 54f, false, androidx.compose.ui.geometry.Offset(cx-r*0.82f,cy-r*0.82f), androidx.compose.ui.geometry.Size(r*1.64f,r*1.64f), style=Stroke(1.2f, cap=StrokeCap.Round))
    val sy = cy - r * 0.72f + scan * r * 1.44f
    drawLine(color.copy(alpha = 0.28f), androidx.compose.ui.geometry.Offset(cx-r*0.65f,sy), androidx.compose.ui.geometry.Offset(cx+r*0.65f,sy), 1.2f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShield(cx: Float, cy: Float, r: Float, color: Color, pulse: Float) {
    val p = Path().apply {
        moveTo(cx, cy-r)
        lineTo(cx+r*0.76f, cy-r*0.46f)
        lineTo(cx+r*0.60f, cy+r*0.62f)
        lineTo(cx, cy+r)
        lineTo(cx-r*0.60f, cy+r*0.62f)
        lineTo(cx-r*0.76f, cy-r*0.46f)
        close()
    }
    drawPath(p, color.copy(alpha = 0.72f), style=Stroke(2f))
    drawPath(p, color.copy(alpha = 0.08f * pulse))
    drawCircle(color.copy(alpha = 0.65f), r*0.18f, androidx.compose.ui.geometry.Offset(cx,cy), style=Stroke(1.3f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrbit(cx: Float, cy: Float, r: Float, color: Color, rotation: Float, pulse: Float) {
    drawOval(color.copy(alpha=0.38f), androidx.compose.ui.geometry.Offset(cx-r,cy-r*0.52f), androidx.compose.ui.geometry.Size(r*2f,r*1.04f), style=Stroke(1.6f))
    drawOval(color.copy(alpha=0.50f), androidx.compose.ui.geometry.Offset(cx-r*0.52f,cy-r), androidx.compose.ui.geometry.Size(r*1.04f,r*2f), style=Stroke(1.2f))
    val a = rotation * Math.PI.toFloat() / 180f
    val px = cx + cos(a) * r
    val py = cy + sin(a) * r * 0.52f
    drawCircle(color.copy(alpha=0.9f), r*0.10f*pulse, androidx.compose.ui.geometry.Offset(px,py))
    drawCircle(color.copy(alpha=0.25f), r*0.20f, androidx.compose.ui.geometry.Offset(cx,cy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBrokenCore(cx: Float, cy: Float, r: Float, color: Color, rotation: Float, pulse: Float) {
    drawArc(color.copy(alpha=0.75f), rotation, 92f, false, androidx.compose.ui.geometry.Offset(cx-r,cy-r), androidx.compose.ui.geometry.Size(r*2f,r*2f), style=Stroke(2.2f))
    drawArc(color.copy(alpha=0.38f), rotation+135f, 52f, false, androidx.compose.ui.geometry.Offset(cx-r*0.82f,cy-r*0.82f), androidx.compose.ui.geometry.Size(r*1.64f,r*1.64f), style=Stroke(1.4f))
    drawArc(color.copy(alpha=0.50f), rotation+226f, 78f, false, androidx.compose.ui.geometry.Offset(cx-r*1.02f,cy-r*1.02f), androidx.compose.ui.geometry.Size(r*2.04f,r*2.04f), style=Stroke(1.0f))
    drawCircle(color.copy(alpha=0.22f), r*0.23f*pulse, androidx.compose.ui.geometry.Offset(cx,cy))
    drawLine(color.copy(alpha=0.78f), androidx.compose.ui.geometry.Offset(cx-r*0.18f,cy-r*0.18f), androidx.compose.ui.geometry.Offset(cx+r*0.18f,cy+r*0.18f), 2f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCompass(cx: Float, cy: Float, r: Float, color: Color, rotation: Float, pulse: Float) {
    drawCircle(color.copy(alpha=0.52f), r*0.78f, androidx.compose.ui.geometry.Offset(cx,cy), style=Stroke(1.3f))
    for (i in 0 until 8) {
        val a = i/8f * 2f * Math.PI.toFloat() + rotation*0.012f
        val inner = r*0.64f
        val outer = r*0.78f
        drawLine(
            color.copy(alpha=if(i%2==0)0.65f else 0.28f),
            androidx.compose.ui.geometry.Offset(cx+cos(a)*inner,cy+sin(a)*inner),
            androidx.compose.ui.geometry.Offset(cx+cos(a)*outer,cy+sin(a)*outer),
            if(i%2==0)1.5f else 0.8f
        )
    }
    val a = -0.76f
    drawLine(color.copy(alpha=0.82f), androidx.compose.ui.geometry.Offset(cx,cy), androidx.compose.ui.geometry.Offset(cx+cos(a)*r*0.72f,cy+sin(a)*r*0.72f), 2.2f)
    drawCircle(color.copy(alpha=0.92f*pulse), r*0.09f, androidx.compose.ui.geometry.Offset(cx,cy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTarget(cx: Float, cy: Float, r: Float, color: Color, pulse: Float, scan: Float) {
    drawCircle(color.copy(alpha=0.55f), r*0.78f, androidx.compose.ui.geometry.Offset(cx,cy), style=Stroke(1.5f))
    drawCircle(color.copy(alpha=0.42f), r*0.52f, androidx.compose.ui.geometry.Offset(cx,cy), style=Stroke(1.0f))
    drawCircle(color.copy(alpha=0.88f), r*0.15f*pulse, androidx.compose.ui.geometry.Offset(cx,cy))
    val sy = cy-r*0.68f+scan*r*1.36f
    drawLine(color.copy(alpha=0.30f), androidx.compose.ui.geometry.Offset(cx-r*0.75f,sy), androidx.compose.ui.geometry.Offset(cx+r*0.75f,sy),1f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWaveGrid(cx: Float, cy: Float, r: Float, color: Color, scan: Float) {
    repeat(5) { i ->
        val y = cy-r*0.68f+i*r*0.34f
        drawLine(color.copy(alpha=0.12f), androidx.compose.ui.geometry.Offset(cx-r*0.9f,y), androidx.compose.ui.geometry.Offset(cx+r*0.9f,y), 0.8f)
    }
    val path=Path()
    val points=18
    for(i in 0 until points){
        val x=cx-r*0.86f+i/(points-1f)*r*1.72f
        val wave=sin(i*0.88f+scan*6.283f)*r*0.19f
        val y=cy+wave
        if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
    }
    drawPath(path,color.copy(alpha=0.78f),style=Stroke(2f,cap=StrokeCap.Round))
    drawCircle(color.copy(alpha=0.82f),r*0.07f,androidx.compose.ui.geometry.Offset(cx+r*0.54f,cy+sin((17)*0.88f+scan*6.283f)*r*0.19f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProtocolMatrix(cx: Float, cy: Float, r: Float, color: Color, scan: Float) {
    val cols=4; val rows=4
    for(row in 0 until rows){
        for(col in 0 until cols){
            val active=((row*cols+col)/16f+scan)%1f
            val alpha=0.12f+0.62f*((1f-kotlin.math.abs(active-0.5f)*2f).coerceIn(0f,1f))
            val x=cx-r*0.7f+col*r*0.46f
            val y=cy-r*0.7f+row*r*0.46f
            drawRoundRect(color.copy(alpha=alpha),androidx.compose.ui.geometry.Offset(x,y),androidx.compose.ui.geometry.Size(r*0.30f,r*0.30f),androidx.compose.ui.geometry.CornerRadius(2f,2f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawExecution(cx: Float, cy: Float, r: Float, color: Color, rotation: Float) {
    val bars=6
    for(i in 0 until bars){
        val h=r*(0.24f+i*0.105f)
        val x=cx-r*0.68f+i*r*0.27f
        drawRoundRect(color.copy(alpha=0.10f),androidx.compose.ui.geometry.Offset(x,cy-h),androidx.compose.ui.geometry.Size(r*0.16f,r*2f*h),androidx.compose.ui.geometry.CornerRadius(3f,3f))
        drawRoundRect(color.copy(alpha=0.56f+0.05f*sin(rotation*0.02f+i)),androidx.compose.ui.geometry.Offset(x,cy+h*0.20f-h),androidx.compose.ui.geometry.Size(r*0.16f,r*1.12f*h),androidx.compose.ui.geometry.CornerRadius(3f,3f))
    }
    drawLine(color.copy(alpha=0.28f),androidx.compose.ui.geometry.Offset(cx-r*0.86f,cy+r*0.78f),androidx.compose.ui.geometry.Offset(cx+r*0.86f,cy+r*0.78f),1f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrowth(cx: Float, cy: Float, r: Float, color: Color, rotation: Float, pulse: Float) {
    drawArc(color.copy(alpha=0.30f),180f,180f,false,androidx.compose.ui.geometry.Offset(cx-r,cy-r),androidx.compose.ui.geometry.Size(r*2f,r*2f),style=Stroke(1f))
    drawArc(color.copy(alpha=0.48f),190f,160f,false,androidx.compose.ui.geometry.Offset(cx-r*0.78f,cy-r*0.78f),androidx.compose.ui.geometry.Size(r*1.56f,r*1.56f),style=Stroke(1.2f))
    val path=Path().apply{
        moveTo(cx-r*0.72f,cy+r*0.56f)
        lineTo(cx-r*0.26f,cy+r*0.18f)
        lineTo(cx+r*0.02f,cy+r*0.28f)
        lineTo(cx+r*0.35f,cy-r*0.10f)
        lineTo(cx+r*0.70f,cy-r*0.58f)
    }
    drawPath(path,color.copy(alpha=0.86f),style=Stroke(2f,cap=StrokeCap.Round))
    drawCircle(color.copy(alpha=0.9f*pulse),r*0.09f,androidx.compose.ui.geometry.Offset(cx+r*0.70f,cy-r*0.58f))
    drawLine(color.copy(alpha=0.70f),androidx.compose.ui.geometry.Offset(cx+r*0.70f,cy-r*0.58f),androidx.compose.ui.geometry.Offset(cx+r*0.34f,cy-r*0.56f),1.4f)
    drawLine(color.copy(alpha=0.70f),androidx.compose.ui.geometry.Offset(cx+r*0.70f,cy-r*0.58f),androidx.compose.ui.geometry.Offset(cx+r*0.68f,cy-r*0.25f),1.4f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLibrary(cx: Float, cy: Float, r: Float, color: Color, pulse: Float) {
    repeat(3){i->
        val x=cx-r*0.58f+i*r*0.54f
        drawRoundRect(color.copy(alpha=0.16f+i*0.08f),androidx.compose.ui.geometry.Offset(x,cy-r*0.72f+i*4f),androidx.compose.ui.geometry.Size(r*0.34f,r*1.42f),androidx.compose.ui.geometry.CornerRadius(3f,3f),style=Stroke(1.4f))
        drawLine(color.copy(alpha=0.30f+i*0.08f),androidx.compose.ui.geometry.Offset(x+4f,cy-r*0.34f),androidx.compose.ui.geometry.Offset(x+r*0.30f,cy-r*0.34f),1.0f)
    }
    drawCircle(color.copy(alpha=0.20f*pulse),r*0.20f,androidx.compose.ui.geometry.Offset(cx,cy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGuardian(cx: Float, cy: Float, r: Float, color: Color, rotation: Float, scan: Float) {
    val p=Path().apply{
        moveTo(cx,cy-r*0.92f); lineTo(cx+r*0.72f,cy-r*0.44f); lineTo(cx+r*0.58f,cy+r*0.58f)
        lineTo(cx,cy+r*0.92f); lineTo(cx-r*0.58f,cy+r*0.58f); lineTo(cx-r*0.72f,cy-r*0.44f); close()
    }
    drawPath(p,color.copy(alpha=0.74f),style=Stroke(1.8f))
    drawCircle(color.copy(alpha=0.10f),r*0.66f,androidx.compose.ui.geometry.Offset(cx,cy))
    val sweep=rotation*0.6f
    drawArc(color.copy(alpha=0.74f),sweep,72f,false,androidx.compose.ui.geometry.Offset(cx-r*0.66f,cy-r*0.66f),androidx.compose.ui.geometry.Size(r*1.32f,r*1.32f),style=Stroke(2.2f,cap=StrokeCap.Round))
    val sy=cy-r*0.66f+scan*r*1.32f
    drawLine(color.copy(alpha=0.26f),androidx.compose.ui.geometry.Offset(cx-r*0.62f,sy),androidx.compose.ui.geometry.Offset(cx+r*0.62f,sy),1f)
}
