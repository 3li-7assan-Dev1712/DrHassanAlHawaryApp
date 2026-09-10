package com.example.feature.article.presentation.share.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

/**
 * The full article body as one continuous, long-press-then-drag-selectable
 * block: long-press and drag to pick a start/end character range (a fresh
 * long-press starts a new selection; long-pressing near an existing handle
 * adjusts just that edge instead). A plain drag with no preceding long-press
 * is left unconsumed so the surrounding column can still scroll normally.
 * Mirrors how [TrimTimeline][com.example.feature.share.presentation.components.TrimTimeline]
 * lets the audio-share flow pick a free-form window instead of fixed points.
 *
 * Deliberately renders as a single [Text] (not one composable per paragraph,
 * like the reading screen's `ArticleContent`) so there's one [TextLayoutResult]
 * to convert a touch position into a character offset against - spanning a
 * drag across multiple separately-laid-out Text composables would need each
 * one's own coordinate math.
 */
@Composable
fun SelectableQuoteText(
    text: String,
    selectionStart: Int,
    selectionEnd: Int,
    onSelectionChanged: (start: Int, end: Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF342C2B),
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val textState = rememberUpdatedState(text)
    val selectionStartState = rememberUpdatedState(selectionStart)
    val selectionEndState = rememberUpdatedState(selectionEnd)
    val onSelectionChangedState = rememberUpdatedState(onSelectionChanged)
    var dragMode by remember { mutableStateOf(DragMode.NONE) }
    var anchor by remember { mutableStateOf(0) }
    val hapticState = rememberUpdatedState(LocalHapticFeedback.current)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                // A plain tap (no preceding long-press, so it doesn't fight the drag
                // detector above) outside the current selection clears it - mirrors
                // the "clear selection" bottom-bar button for a tap-away gesture.
                detectTapGestures(
                    onTap = { offset ->
                        val lr = layoutResult ?: return@detectTapGestures
                        if (selectionEndState.value <= selectionStartState.value) return@detectTapGestures
                        val charIndex = lr.getOffsetForPosition(offset).coerceIn(0, textState.value.length)
                        if (charIndex < selectionStartState.value || charIndex > selectionEndState.value) {
                            onSelectionChangedState.value(0, 0)
                        }
                    },
                )
            },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 1.8.em,
                textDirection = TextDirection.Rtl,
            ),
            textAlign = TextAlign.Justify,
            onTextLayout = { layoutResult = it },
            modifier = Modifier
                .pointerInput(Unit) {
                    // AfterLongPress, not detectDragGestures: a plain drag must be free to
                    // scroll the surrounding column (detectDragGestures claims every drag
                    // immediately, which blocked scrolling entirely). Long-press then drag
                    // - whether starting a new selection or dragging an existing handle -
                    // is what claims the gesture here instead.
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            val lr = layoutResult ?: return@detectDragGesturesAfterLongPress
                            val charIndex = lr.getOffsetForPosition(offset).coerceIn(0, textState.value.length)
                            val hasSelection = selectionEndState.value > selectionStartState.value
                            val touchPx = HANDLE_TOUCH_DP.dp.toPx()
                            dragMode = when {
                                hasSelection && offset.isNear(lr.getCursorRect(selectionStartState.value), touchPx) ->
                                    DragMode.ADJUST_START

                                hasSelection && offset.isNear(lr.getCursorRect(selectionEndState.value), touchPx) ->
                                    DragMode.ADJUST_END

                                else -> {
                                    anchor = charIndex
                                    DragMode.NEW
                                }
                            }
                            hapticState.value.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = { dragMode = DragMode.NONE },
                        onDragCancel = { dragMode = DragMode.NONE },
                    ) { change, _ ->
                        if (dragMode == DragMode.NONE) return@detectDragGesturesAfterLongPress
                        val lr = layoutResult ?: return@detectDragGesturesAfterLongPress
                        change.consume()
                        val current = lr.getOffsetForPosition(change.position).coerceIn(0, textState.value.length)

                        when (dragMode) {
                            DragMode.NEW -> onSelectionChangedState.value(
                                minOf(anchor, current),
                                maxOf(anchor, current),
                            )

                            DragMode.ADJUST_START -> onSelectionChangedState.value(
                                current.coerceIn(0, (selectionEndState.value - 1).coerceAtLeast(0)),
                                selectionEndState.value,
                            )

                            DragMode.ADJUST_END -> onSelectionChangedState.value(
                                selectionStartState.value,
                                current.coerceIn(selectionStartState.value + 1, textState.value.length),
                            )

                            DragMode.NONE -> Unit
                        }
                    }
                }
                .drawBehind {
                    val lr = layoutResult ?: return@drawBehind
                    if (selectionEnd > selectionStart) {
                        val path = lr.getPathForRange(
                            selectionStart.coerceIn(0, text.length),
                            selectionEnd.coerceIn(0, text.length),
                        )
                        drawPath(path, color = accentColor.copy(alpha = 0.28f))
                    }
                },
        )

        if (selectionEnd > selectionStart) {
            layoutResult?.let { lr ->
                SelectionHandle(rect = lr.getCursorRect(selectionStart.coerceIn(0, text.length)), color = accentColor)
                SelectionHandle(rect = lr.getCursorRect(selectionEnd.coerceIn(0, text.length)), color = accentColor)
            }
        }
    }
}

@Composable
private fun SelectionHandle(rect: Rect, color: Color) {
    Box(
        modifier = Modifier
            .offset {
                val radiusPx = HANDLE_SIZE_DP.roundToPx() / 2
                IntOffset(rect.left.toInt() - radiusPx, rect.bottom.toInt())
            }
            .size(HANDLE_SIZE_DP)
            .background(color, CircleShape),
    )
}

private fun Offset.isNear(rect: Rect, tolerancePx: Float): Boolean =
    x in (rect.left - tolerancePx)..(rect.right + tolerancePx) &&
        y in (rect.top - tolerancePx)..(rect.bottom + tolerancePx)

private enum class DragMode { NONE, NEW, ADJUST_START, ADJUST_END }

private const val HANDLE_TOUCH_DP = 24
private val HANDLE_SIZE_DP = 16.dp
