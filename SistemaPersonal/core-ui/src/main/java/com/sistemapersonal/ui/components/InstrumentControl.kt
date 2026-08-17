package com.sistemapersonal.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sistemapersonal.ui.theme.Danger
import com.sistemapersonal.ui.theme.Ink3
import com.sistemapersonal.ui.theme.Mint
import com.sistemapersonal.ui.theme.PanelFill
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

enum class InstrumentMode { PRESS, TOGGLE, ROTARY }

enum class InstrumentState { IDLE, FOCUS, ACTIVE, SUCCESS, WARNING, ERROR }

@Composable
fun InstrumentControl(
    controlId: String,
    modifier: Modifier = Modifier,
    accent: Color,
    enabled: Boolean = true,
    mode: InstrumentMode = InstrumentMode.PRESS,
    value: Float = 0.5f,
    onValueChange: (Float) -> Unit = {},
    onActivate: () -> InstrumentOutcome = { InstrumentOutcome.SUCCESS },
    onPressState: (Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val system = LocalVisualEnergy.current
    val state = system.stateFor(controlId)
    val pressDepth = remember { mutableFloatStateOf(0f) }
    val visualDepth by animateFloatAsState(
        targetValue = if (pressDepth.floatValue > 0f) 0.035f else 0f,
        animationSpec = tween(90),
        label = "instrument_depth"
    )
    val stateAccent = when (state) {
        InstrumentState.SUCCESS -> Mint
        InstrumentState.WARNING -> com.sistemapersonal.ui.theme.Amber
        InstrumentState.ERROR -> Danger
        else -> accent
    }

    LaunchedEffect(state, controlId) {
        when (state) {
            InstrumentState.SUCCESS, InstrumentState.ERROR -> {
                delay(1100)
                if (system.stateFor(controlId) == state) system.resetInstrument(controlId)
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier.pointerInput(enabled, mode, value, controlId) {
            if (!enabled) return@pointerInput
            if (mode == InstrumentMode.ROTARY) {
                detectDragGestures(
                    onDragStart = { offset ->
                        pressDepth.floatValue = 1f
                        onPressState(true)
                        system.instrumentEvent(controlId, InstrumentEvent.FOCUS, offset.x, offset.y)
                        system.physicalPress(offset.x, offset.y, 0.7f)
                    },
                    onDragEnd = {
                        pressDepth.floatValue = 0f
                        onPressState(false)
                        system.instrumentEvent(controlId, InstrumentEvent.ROTARY_END)
                        system.instrumentEvent(controlId, InstrumentEvent.SUCCESS)
                    },
                    onDragCancel = {
                        pressDepth.floatValue = 0f
                        onPressState(false)
                        system.instrumentEvent(controlId, InstrumentEvent.ERROR)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        system.instrumentEvent(controlId, InstrumentEvent.ROTARY_START, change.position.x, change.position.y)
                        onValueChange((value - dragAmount.y / 260f).coerceIn(0f, 1f))
                        system.interact(change.position.x, change.position.y, 0.18f)
                    }
                )
            } else {
                detectTapGestures(
                    onPress = { offset ->
                        pressDepth.floatValue = 1f
                        onPressState(true)
                        system.instrumentEvent(controlId, InstrumentEvent.FOCUS, offset.x, offset.y)
                        delay(55)
                        system.instrumentEvent(controlId, InstrumentEvent.PRESS, offset.x, offset.y)
                        system.physicalPress(offset.x, offset.y, if (mode == InstrumentMode.TOGGLE) 0.82f else 0.95f)
                        try {
                            tryAwaitRelease()
                        } finally {
                            pressDepth.floatValue = 0f
                            onPressState(false)
                            system.instrumentEvent(controlId, InstrumentEvent.RELEASE, offset.x, offset.y)
                        }
                    },
                    onTap = { offset ->
                        if (mode == InstrumentMode.TOGGLE) {
                            val next = if (value > 0.5f) 0f else 1f
                            onValueChange(next)
                            system.instrumentEvent(
                                controlId,
                                if (next > 0.5f) InstrumentEvent.TOGGLE_ON else InstrumentEvent.TOGGLE_OFF,
                                offset.x,
                                offset.y
                            )
                        } else {
                            val outcome = onActivate()
                            system.instrumentEvent(
                                controlId,
                                when (outcome) {
                                    InstrumentOutcome.SUCCESS -> InstrumentEvent.SUCCESS
                                    InstrumentOutcome.WARNING -> InstrumentEvent.WARNING
                                    InstrumentOutcome.ERROR -> InstrumentEvent.ERROR
                                },
                                offset.x,
                                offset.y
                            )
                        }
                    }
                )
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val inset = size.minDimension * (0.08f + visualDepth)
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.23f
            val energy = if (!enabled) 0.18f else 0.70f + 0.30f * value.coerceIn(0f, 1f)

            drawRoundRect(
                brush = Brush.linearGradient(listOf(
                    PanelFill.copy(alpha = 0.94f),
                    stateAccent.copy(alpha = 0.025f + energy * 0.06f),
                    PanelFill.copy(alpha = 0.80f)
                )),
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - inset * 2f, size.height - inset * 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = stateAccent.copy(alpha = 0.55f * energy),
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - inset * 2f, size.height - inset * 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(1.1f)
            )

            drawCircle(stateAccent.copy(alpha = 0.12f + 0.07f * energy), r, c)
            drawCircle(stateAccent.copy(alpha = 0.26f), r, c, style = Stroke(1f))
            drawCircle(stateAccent.copy(alpha = 0.65f), r * 0.72f, c, style = Stroke(1.4f))

            val needleAngle = -1.92f + value.coerceIn(0f, 1f) * 3.84f
            val needleEnd = androidx.compose.ui.geometry.Offset(c.x + cos(needleAngle) * r * 0.78f, c.y + sin(needleAngle) * r * 0.78f)
            drawLine(stateAccent.copy(alpha = 0.92f), c, needleEnd, 2.1f, StrokeCap.Round)
            drawCircle(stateAccent, 3.2f, c)

            when (state) {
                InstrumentState.FOCUS -> drawCircle(stateAccent.copy(alpha = 0.22f), r * 1.16f, c, style = Stroke(1f))
                InstrumentState.ACTIVE -> drawArc(stateAccent.copy(alpha = 0.68f), -90f, 130f, false, androidx.compose.ui.geometry.Offset(c.x-r*1.05f,c.y-r*1.05f), androidx.compose.ui.geometry.Size(r*2.1f,r*2.1f), style = Stroke(2f, cap = StrokeCap.Round))
                InstrumentState.SUCCESS -> drawCircle(Mint.copy(alpha = 0.35f), r * 1.22f, c, style = Stroke(1.4f))
                InstrumentState.WARNING -> drawCircle(com.sistemapersonal.ui.theme.Amber.copy(alpha = 0.35f), r * 1.18f, c, style = Stroke(1.2f))
                InstrumentState.ERROR -> repeat(3) { i ->
                    val yy = c.y + (i - 1) * 5f
                    drawLine(Danger.copy(alpha = 0.35f), androidx.compose.ui.geometry.Offset(c.x-r, yy), androidx.compose.ui.geometry.Offset(c.x+r, yy+sin(i.toFloat())*2f), 1f)
                }
                InstrumentState.IDLE -> Unit
            }
        }
        content()
    }
}
