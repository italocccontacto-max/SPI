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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.Void
import kotlinx.coroutines.delay

enum class InstrumentEvent {
    FOCUS, PRESS, RELEASE, SUCCESS, WARNING, ERROR, TOGGLE_ON, TOGGLE_OFF, ROTARY_START, ROTARY_END
}

enum class InstrumentOutcome { SUCCESS, WARNING, ERROR }

@Stable
class VisualEnergyController internal constructor() {
    var energy by mutableFloatStateOf(0.42f)
        private set
    var interactionEnergy by mutableFloatStateOf(0f)
        private set
    var transitionEnergy by mutableFloatStateOf(0f)
        private set
    var glitchLevel by mutableFloatStateOf(0f)
        private set
    var residual by mutableFloatStateOf(0f)
        private set
    var x by mutableFloatStateOf(0f)
        private set
    var y by mutableFloatStateOf(0f)
        private set
    var transitionToken by mutableIntStateOf(0)
        private set
    var glitchToken by mutableIntStateOf(0)
        private set
    var eventToken by mutableIntStateOf(0)
        private set
    var activeModule by mutableStateOf("")
        private set

    private val instrumentStates = mutableStateMapOf<String, InstrumentState>()

    fun stateFor(controlId: String): InstrumentState =
        instrumentStates[controlId] ?: InstrumentState.IDLE

    fun instrumentEvent(controlId: String, event: InstrumentEvent, px: Float = x, py: Float = y): InstrumentState {
        val current = stateFor(controlId)
        val next = when (event) {
            InstrumentEvent.FOCUS -> when (current) {
                InstrumentState.IDLE, InstrumentState.SUCCESS, InstrumentState.WARNING -> InstrumentState.FOCUS
                else -> current
            }
            InstrumentEvent.PRESS, InstrumentEvent.ROTARY_START -> when (current) {
                InstrumentState.FOCUS, InstrumentState.IDLE -> InstrumentState.ACTIVE
                else -> InstrumentState.ACTIVE
            }
            InstrumentEvent.RELEASE, InstrumentEvent.ROTARY_END -> when (current) {
                InstrumentState.ACTIVE -> InstrumentState.FOCUS
                else -> current
            }
            InstrumentEvent.SUCCESS, InstrumentEvent.TOGGLE_ON -> InstrumentState.SUCCESS
            InstrumentEvent.WARNING, InstrumentEvent.TOGGLE_OFF -> InstrumentState.WARNING
            InstrumentEvent.ERROR -> InstrumentState.ERROR
        }
        instrumentStates[controlId] = next
        x = px
        y = py
        eventToken += 1
        when (next) {
            InstrumentState.SUCCESS -> result(true, px, py)
            InstrumentState.WARNING -> {
                energy = (energy + 0.05f).coerceAtMost(1.6f)
                residual = (residual + 0.36f).coerceAtMost(1.8f)
            }
            InstrumentState.ERROR -> fail(px, py)
            InstrumentState.ACTIVE, InstrumentState.FOCUS, InstrumentState.IDLE -> Unit
        }
        return next
    }

    fun resetInstrument(controlId: String) {
        instrumentStates[controlId] = InstrumentState.IDLE
        eventToken += 1
    }

    fun navigate(moduleId: String) {
        if (activeModule == moduleId) return
        activeModule = moduleId
        transitionToken += 1
        transitionEnergy = 1f
        energy = (energy + 0.32f).coerceAtMost(1.35f)

        glitchLevel = (glitchLevel + 0.34f).coerceAtMost(1f)
        glitchToken += 1
        eventToken += 1
    }

    fun interact(px: Float, py: Float, strength: Float = 1f) {
        x = px
        y = py
        interactionEnergy = strength.coerceIn(0f, 1.4f)
        residual = (residual + strength * 0.92f).coerceAtMost(1.6f)
        energy = (energy + strength * 0.12f).coerceAtMost(1.5f)
        eventToken += 1
    }

    fun physicalPress(px: Float, py: Float, strength: Float = 1f) {
        interact(px, py, strength)
    }

    fun result(success: Boolean, px: Float = x, py: Float = y) {
        x = px
        y = py
        if (success) {
            energy = (energy + 0.24f).coerceAtMost(1.6f)
            residual = (residual + 0.72f).coerceAtMost(1.8f)
        } else {
            fail(px, py)
            return
        }
        eventToken += 1
    }

    fun fail(px: Float = x, py: Float = y) {
        x = px
        y = py
        energy = (energy - 0.14f).coerceAtLeast(0.08f)
        glitchLevel = (glitchLevel + 0.55f).coerceAtMost(1f)
        glitchToken += 1
        residual = (residual + 0.48f).coerceAtMost(1.7f)
        eventToken += 1
    }

    fun anomaly(strength: Float = 1f) {
        glitchLevel = (glitchLevel + strength).coerceAtMost(1.5f)
        glitchToken += 1
        eventToken += 1
    }

    fun decay(delta: Float = 0.022f) {
        energy = approach(energy, 0.42f, delta)
        interactionEnergy = approach(interactionEnergy, 0f, delta * 1.6f)
        transitionEnergy = approach(transitionEnergy, 0f, delta * 2.2f)

        glitchLevel = approach(glitchLevel, 0f, delta * 1.8f)
        residual = approach(residual, 0f, delta * 0.72f)
    }

    private fun approach(value: Float, target: Float, amount: Float): Float =
        if (value > target) (value - amount).coerceAtLeast(target)
        else (value + amount).coerceAtMost(target)
}

@Composable
fun rememberVisualEnergyController(): VisualEnergyController = remember { VisualEnergyController() }

val LocalVisualEnergy = staticCompositionLocalOf<VisualEnergyController> {
    error("VisualEnergyController must be provided by the application root")
}

@Composable
fun VisualSystemLayer(
    modifier: Modifier = Modifier,
    accent: Color,
    system: VisualEnergyController,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "visual_system")
    val phase by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(7800, easing = LinearEasing)),
        label = "phase"
    )
    val breathe by transition.animateFloat(
        0.90f, 1.10f,
        infiniteRepeatable(tween(3100), RepeatMode.Reverse),
        label = "breathe"
    )
    val pulseAnim = remember { Animatable(0f) }

    LaunchedEffect(system.eventToken) {
        if (system.interactionEnergy > 0f) {
            pulseAnim.snapTo(1f)
            pulseAnim.animateTo(0f, tween(680, easing = FastOutSlowInEasing))
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            system.decay()
            delay(48)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(system) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    system.interact(down.position.x, down.position.y, 0.82f)
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.54f, size.height * 0.28f)
            val stateEnergy = (system.energy * (0.88f + 0.12f * breathe)).coerceIn(0f, 1.6f)
            val anomaly = system.glitchLevel

            drawCircle(
                brush = Brush.radialGradient(
                    0f to accent.copy(alpha = 0.18f * stateEnergy),
                    0.28f to accent.copy(alpha = 0.060f * stateEnergy),
                    0.62f to Color.Transparent,
                    1f to Color.Transparent
                ),
                radius = size.minDimension * (0.58f + stateEnergy * 0.09f),
                center = center
            )

            drawRect(
                brush = Brush.radialGradient(
                    0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1f to Void.copy(alpha = 0.86f)
                )
            )

            val edgeAlpha = (0.03f + stateEnergy * 0.045f).coerceAtMost(0.10f)
            drawLine(accent.copy(alpha = edgeAlpha), Offset(0f, size.height * 0.18f), Offset(size.width * 0.18f, size.height * 0.18f), 1.2f)
            drawLine(accent.copy(alpha = edgeAlpha * 0.8f), Offset(size.width * 0.80f, size.height * 0.82f), Offset(size.width, size.height * 0.82f), 1.2f)

            val scanY = (phase * (size.height + 32.dp.toPx())) - 16.dp.toPx()
            drawLine(accent.copy(alpha = 0.018f + stateEnergy * 0.008f), Offset(0f, scanY), Offset(size.width, scanY), 1.1f)
            drawLine(accent.copy(alpha = 0.008f + stateEnergy * 0.004f), Offset(0f, scanY + 8f), Offset(size.width, scanY + 8f), 0.7f)

            if (anomaly > 0.02f) {
                val burst = anomaly.coerceIn(0f, 1f)
                val centerY = size.height * (0.18f + (system.eventToken % 7) * 0.095f)
                repeat(4 + (burst * 7).toInt()) { index ->
                    val offset = (index % 3) * 23f
                    val width = size.width * (0.10f + ((index * 17) % 31) / 100f)
                    drawRect(
                        accent.copy(alpha = (0.010f + burst * 0.045f).coerceAtMost(0.12f)),
                        Offset(((system.eventToken * 19 + index * 37) % 100) / 100f * size.width, centerY + offset),
                        androidx.compose.ui.geometry.Size(width, if (index % 3 == 0) 2.4f else 0.9f)
                    )
                }
                drawLine(accent.copy(alpha = 0.08f + burst * 0.10f), Offset(0f, centerY), Offset(size.width, centerY), 1f)
            }

            if (system.residual > 0.01f) {
                val maxR = size.maxDimension * 0.30f
                val r = maxR * (1f - system.residual.coerceIn(0f, 1f) * 0.72f)
                val c = Offset(system.x, system.y)
                drawCircle(accent.copy(alpha = 0.04f + system.residual * 0.10f), r, c, style = Stroke(1.2f))
                drawCircle(accent.copy(alpha = 0.20f), r * 0.16f, c, style = Stroke(0.8f))
            }
        }
        content()
    }
}

@Composable
fun VisualEnergyProvider(system: VisualEnergyController, content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(LocalVisualEnergy provides system, content = content)
}
