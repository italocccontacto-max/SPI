package com.sistemapersonal.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.AngularPanelShape
import com.sistemapersonal.ui.theme.Bg0
import com.sistemapersonal.ui.theme.Ink0
import com.sistemapersonal.ui.theme.Ink1
import com.sistemapersonal.ui.theme.PanelFill
import kotlinx.coroutines.delay

@Composable
fun CelebrationPopup(
    titulo: String,
    subtitulo: String? = null,
    accent: Color,
    visible: Boolean,
    duracionMs: Int = 2600,
    onSonido: () -> Unit = {},
    onDismiss: () -> Unit
) {
    if (!visible) return

    val scale = remember { Animatable(0.74f) }
    val glow = remember { Animatable(0.45f) }

    LaunchedEffect(visible) {
        onSonido()
        scale.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        glow.animateTo(1f, tween(420))
        delay(duracionMs.toLong())
        onDismiss()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ConfettiOverlay(accent = accent, onFin = {})

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0f to accent.copy(alpha = 0.025f * glow.value),
                        0.46f to Color.Transparent,
                        1f to Color.Transparent
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = (0.78f + 0.22f * glow.value).coerceIn(0f, 1f)
                }
                .background(
                    Brush.radialGradient(
                        0f to accent.copy(alpha = 0.10f),
                        0.42f to PanelFill.copy(alpha = 0.94f),
                        1f to Bg0.copy(alpha = 0.96f)
                    ),
                    AngularPanelShape
                )
                .border(1.dp, accent.copy(alpha = 0.92f), AngularPanelShape)
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¡LO HICISTE!", color = accent, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(titulo, color = Ink0, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                if (subtitulo != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitulo, color = Ink1, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
