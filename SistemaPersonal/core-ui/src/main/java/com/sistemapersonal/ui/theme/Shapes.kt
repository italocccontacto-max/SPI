package com.sistemapersonal.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class AngularCutShape(private val cut: Dp = 10.dp, private val corners: Corners = Corners.TWO) : Shape {
    enum class Corners { TWO, FOUR }

    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = with(density) { cut.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (corners == Corners.FOUR) {
                moveTo(c, 0f)
                lineTo(w - c, 0f)
                lineTo(w, c)
                lineTo(w, h - c)
                lineTo(w - c, h)
                lineTo(c, h)
                lineTo(0f, h - c)
                lineTo(0f, c)
                close()
            } else {
                moveTo(c, 0f)
                lineTo(w, 0f)
                lineTo(w, h - c)
                lineTo(w - c, h)
                lineTo(0f, h)
                lineTo(0f, c)
                close()
            }
        }
        return Outline.Generic(path)
    }
}

val AngularPanelShape = AngularCutShape(10.dp, AngularCutShape.Corners.FOUR)
val AngularPanelShapeSm = AngularCutShape(6.dp, AngularCutShape.Corners.FOUR)

val AngularPanelShapeXs = androidx.compose.foundation.shape.CutCornerShape(3.dp)
