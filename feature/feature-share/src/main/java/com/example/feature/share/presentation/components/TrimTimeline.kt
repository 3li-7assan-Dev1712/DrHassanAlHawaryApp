package com.example.feature.share.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Whole-track waveform (from [WaveformAnalyzer.analyzeTrackOverview], a cheap
 * sparse approximation - never a full decode) with a freely draggable range:
 * either edge can move independently to any position, or the whole window can
 * be dragged by its middle, letting the user pick exactly what they want to
 * share rather than a fixed-length window.
 *
 * The pointerInput block is installed once ([Unit] key) and never restarted
 * mid-gesture; live values are read through [rememberUpdatedState] so a drag
 * always sees the current range instead of a value frozen from setup time.
 */
@Composable
fun TrimTimeline(
    overviewEnvelope: FloatArray,
    totalDurationMs: Long,
    startMs: Long,
    endMs: Long,
    onRangeChanged: (startMs: Long, endMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    minDurationMs: Long = 5_000L,
    accentColor: Color = Color(0xFF342C2B),
) {
    val widthPxState = remember { mutableFloatStateOf(0f) }
    val totalDurationState = rememberUpdatedState(totalDurationMs)
    val startState = rememberUpdatedState(startMs)
    val endState = rememberUpdatedState(endMs)
    val minDurationState = rememberUpdatedState(minDurationMs)
    val onRangeChangedState = rememberUpdatedState(onRangeChanged)
    var activeHandle by remember { mutableStateOf(TrimHandle.NONE) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val total = totalDurationState.value
                        val widthPx = widthPxState.floatValue
                        if (total <= 0L || widthPx <= 0f) {
                            activeHandle = TrimHandle.NONE
                            return@detectDragGestures
                        }
                        val startX = (startState.value.toFloat() / total) * widthPx
                        val endX = (endState.value.toFloat() / total) * widthPx
                        val touchPx = HANDLE_TOUCH_DP.dp.toPx()
                        activeHandle = when {
                            abs(offset.x - startX) <= touchPx -> TrimHandle.START
                            abs(offset.x - endX) <= touchPx -> TrimHandle.END
                            offset.x in startX..endX -> TrimHandle.WINDOW
                            else -> TrimHandle.NONE
                        }
                    },
                    onDragEnd = { activeHandle = TrimHandle.NONE },
                    onDragCancel = { activeHandle = TrimHandle.NONE },
                ) { change, dragAmount ->
                    if (activeHandle == TrimHandle.NONE) return@detectDragGestures
                    change.consume()
                    val total = totalDurationState.value
                    val widthPx = widthPxState.floatValue
                    if (total <= 0L || widthPx <= 0f) return@detectDragGestures
                    val msPerPx = total.toFloat() / widthPx
                    val deltaMs = (dragAmount.x * msPerPx).roundToLong()
                    val currentStart = startState.value
                    val currentEnd = endState.value
                    val minDuration = minDurationState.value

                    when (activeHandle) {
                        TrimHandle.START -> {
                            val newStart = (currentStart + deltaMs).coerceIn(0L, currentEnd - minDuration)
                            onRangeChangedState.value(newStart, currentEnd)
                        }

                        TrimHandle.END -> {
                            val newEnd = (currentEnd + deltaMs).coerceIn(currentStart + minDuration, total)
                            onRangeChangedState.value(currentStart, newEnd)
                        }

                        TrimHandle.WINDOW -> {
                            val duration = currentEnd - currentStart
                            val newStart = (currentStart + deltaMs).coerceIn(0L, (total - duration).coerceAtLeast(0L))
                            onRangeChangedState.value(newStart, newStart + duration)
                        }

                        TrimHandle.NONE -> Unit
                    }
                }
            }
    ) {
        widthPxState.floatValue = size.width
        if (totalDurationMs <= 0L) return@Canvas

        val barCount = overviewEnvelope.size.coerceAtLeast(1)
        val barWidth = size.width / barCount
        for (i in 0 until barCount) {
            val amplitude = overviewEnvelope.getOrElse(i) { 0f }.coerceIn(0f, 1f)
            val barHeight = (size.height * amplitude).coerceAtLeast(4f)
            drawRect(
                color = Color.Gray.copy(alpha = 0.4f),
                topLeft = Offset(i * barWidth, (size.height - barHeight) / 2f),
                size = Size((barWidth * 0.7f).coerceAtLeast(1f), barHeight),
            )
        }

        val windowLeft = (startMs.toFloat() / totalDurationMs) * size.width
        val windowRight = (endMs.toFloat() / totalDurationMs) * size.width

        drawRect(
            color = accentColor.copy(alpha = 0.28f),
            topLeft = Offset(windowLeft, 0f),
            size = Size((windowRight - windowLeft).coerceAtLeast(4f), size.height),
        )
        val handleWidth = 4.dp.toPx()
        drawRect(color = accentColor, topLeft = Offset(windowLeft, 0f), size = Size(handleWidth, size.height))
        drawRect(
            color = accentColor,
            topLeft = Offset((windowRight - handleWidth).coerceAtLeast(windowLeft), 0f),
            size = Size(handleWidth, size.height),
        )
    }
}

private enum class TrimHandle { NONE, START, END, WINDOW }

private const val HANDLE_TOUCH_DP = 20
