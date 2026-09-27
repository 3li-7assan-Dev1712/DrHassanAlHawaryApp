package com.example.feature.article.presentation.share.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.BrandPalette
import com.example.domain.text.SelectableArticle

/**
 * The cleaned article as ONE text (so a selection can run across paragraphs), selected
 * like any Android text: long-press selects the word under the finger, keep dragging to
 * extend it freely, then drag either round handle to trim or grow it. Dragging near the
 * top or bottom of [viewport] scrolls [scrollState]. A tap outside the selection clears
 * it. A plain drag (no long-press) is left alone, so the page scrolls normally.
 *
 * [selectionStart]/[selectionEnd] are offsets in [SelectableArticle.text].
 */
@Composable
fun SelectableArticleText(
    article: SelectableArticle,
    selectionStart: Int,
    selectionEnd: Int,
    onSelectionChanged: (start: Int, end: Int) -> Unit,
    scrollState: ScrollState,
    /** The scroll container's bounds in window coordinates (for edge auto-scroll). */
    viewport: () -> Rect?,
    selectAllLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    val haptics = LocalHapticFeedback.current
    val text = remember(article, colors) { styled(article, colors) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val drag = remember { SelectionDrag() }
    val length = article.text.length

    val current by rememberUpdatedState(selectionStart to selectionEnd)
    val onChange by rememberUpdatedState(onSelectionChanged)
    val hasSelection = selectionEnd > selectionStart

    fun offsetAt(position: Offset): Int = (layout?.getOffsetForPosition(position) ?: 0).coerceIn(0, length)

    /** Applies the finger at [pointer] (Text coordinates) to the active drag. */
    fun update(pointer: Offset) {
        drag.pointer = pointer
        val at = offsetAt(pointer)
        val (start, end) = current
        when (drag.mode) {
            DragMode.New -> onChange(minOf(drag.anchorStart, at), maxOf(drag.anchorEnd, at))
            DragMode.Start -> onChange(at.coerceAtMost(end - 1).coerceAtLeast(0), end)
            DragMode.End -> onChange(start, at.coerceAtLeast(start + 1).coerceAtMost(length))
            DragMode.None -> Unit
        }
    }

    // Edge auto-scroll while any drag is active: the finger stays put on screen, the text
    // moves under it, so the selection keeps growing.
    val density = LocalDensity.current
    val edgePx = with(density) { AUTO_SCROLL_EDGE.toPx() }
    val maxStepPx = with(density) { AUTO_SCROLL_MAX_STEP.toPx() }
    LaunchedEffect(drag.active) {
        if (!drag.active) return@LaunchedEffect
        while (drag.active) {
            withFrameNanos { }
            val coords = coordinates ?: continue
            val bounds = viewport() ?: continue
            val y = coords.localToWindow(drag.pointer).y
            // Faster the closer the finger is to (or the further past) the edge.
            val speed = when {
                y < bounds.top + edgePx -> -((bounds.top + edgePx - y) / edgePx).coerceIn(0f, 1f) * maxStepPx
                y > bounds.bottom - edgePx -> ((y - (bounds.bottom - edgePx)) / edgePx).coerceIn(0f, 1f) * maxStepPx
                else -> 0f
            }
            if (speed != 0f) {
                val moved = scrollState.scrollBy(speed)
                if (moved != 0f) update(drag.pointer.copy(y = drag.pointer.y + moved))
            }
        }
    }

    // Absolute (left-based) placement: the layout's cursor rects are left-based, and a
    // mirrored RTL offset would put the handles on the wrong side.
    Box(modifier = modifier, contentAlignment = AbsoluteAlignment.TopLeft) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 1.9.em),
            color = colors.textSecondary,
            onTextLayout = { layout = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates = it }
                .semantics {
                    // Drag selection isn't usable with TalkBack: offer "select everything".
                    customActions = listOf(
                        CustomAccessibilityAction(selectAllLabel) {
                            onChange(0, length)
                            true
                        },
                    )
                }
                .pointerInput(article) {
                    detectTapGestures { position ->
                        val (start, end) = current
                        if (end <= start) return@detectTapGestures
                        val at = offsetAt(position)
                        if (at < start || at > end) onChange(0, 0)
                    }
                }
                .pointerInput(article) {
                    // After a long-press only: a plain drag must stay free to scroll the page.
                    detectDragGesturesAfterLongPress(
                        onDragStart = { position ->
                            val lr = layout ?: return@detectDragGesturesAfterLongPress
                            val at = offsetAt(position)
                            val word: TextRange = lr.getWordBoundary(at.coerceAtMost((length - 1).coerceAtLeast(0)))
                            drag.anchorStart = word.start.coerceIn(0, length)
                            drag.anchorEnd = word.end.coerceIn(drag.anchorStart, length).let { if (it == drag.anchorStart) (it + 1).coerceAtMost(length) else it }
                            drag.mode = DragMode.New
                            drag.pointer = position
                            drag.active = true
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onChange(drag.anchorStart, drag.anchorEnd)
                        },
                        onDragEnd = { drag.stop() },
                        onDragCancel = { drag.stop() },
                    ) { change, _ ->
                        change.consume()
                        update(change.position)
                    }
                }
                .drawBehind {
                    val lr = layout ?: return@drawBehind
                    // A short accent bar at the start (right, in RTL) of each heading.
                    article.blocks.filter { it.kind == SelectableArticle.Kind.Heading }.forEach { block ->
                        val line = lr.getLineForOffset(block.start)
                        val top = lr.getLineTop(line)
                        val bottom = lr.getLineBottom(line)
                        val barHeight = 18.dp.toPx().coerceAtMost(bottom - top)
                        drawRoundRect(
                            color = colors.accentStrong,
                            topLeft = Offset(size.width - 3.dp.toPx(), (top + bottom - barHeight) / 2),
                            size = Size(3.dp.toPx(), barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                        )
                    }
                    if (selectionEnd > selectionStart) {
                        // The highlight hugs each line of the selection.
                        drawPath(
                            lr.getPathForRange(selectionStart.coerceIn(0, length), selectionEnd.coerceIn(0, length)),
                            color = colors.accentStrong.copy(alpha = 0.32f),
                        )
                        listOf(selectionStart, selectionEnd).forEach { edge ->
                            val r = lr.getCursorRect(edge.coerceIn(0, length))
                            drawLine(colors.accentStrong, Offset(r.left, r.top), Offset(r.left, r.bottom), strokeWidth = 2.dp.toPx())
                        }
                    }
                },
        )

        if (hasSelection) {
            layout?.let { lr ->
                listOf(DragMode.Start to selectionStart, DragMode.End to selectionEnd).forEach { (mode, edge) ->
                    Handle(
                        rect = lr.getCursorRect(edge.coerceIn(0, length)),
                        onStart = { pointer ->
                            drag.mode = mode
                            drag.pointer = pointer
                            drag.active = true
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onMove = { pointer -> update(pointer) },
                        onEnd = { drag.stop() },
                    )
                }
            }
        }
    }
}

/**
 * A round knob under a selection edge, with a 44dp touch target that takes a plain drag
 * right away. Reports the finger in the text's coordinates, aimed at the line's middle.
 */
@Composable
private fun Handle(rect: Rect, onStart: (Offset) -> Unit, onMove: (Offset) -> Unit, onEnd: () -> Unit) {
    val colors = Brand.colors
    val currentRect by rememberUpdatedState(rect)
    Box(
        modifier = Modifier
            .absoluteOffset {
                val half = TOUCH_SIZE.roundToPx() / 2
                IntOffset(rect.left.toInt() - half, rect.bottom.toInt() - 6.dp.roundToPx())
            }
            .size(TOUCH_SIZE)
            // Taps on a knob must not count as "tap outside to clear".
            .pointerInput(Unit) { detectTapGestures { } }
            .pointerInput(Unit) {
                var pointer = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        pointer = Offset(currentRect.left, currentRect.center.y)
                        onStart(pointer)
                    },
                    onDragEnd = onEnd,
                    onDragCancel = onEnd,
                ) { change, amount ->
                    change.consume()
                    pointer += amount
                    onMove(pointer)
                }
            },
    ) {
        Box(
            Modifier
                .absoluteOffset { IntOffset(((TOUCH_SIZE - KNOB_SIZE) / 2).roundToPx(), 0) }
                .size(KNOB_SIZE)
                .background(colors.accentStrong, CircleShape),
        )
    }
}

/** The article, styled like the reader: basmala centred, headings bold, ornaments gold. */
private fun styled(article: SelectableArticle, colors: BrandPalette): AnnotatedString = buildAnnotatedString {
    append(article.text)
    article.blocks.forEach { block ->
        when (block.kind) {
            SelectableArticle.Kind.Body ->
                addStyle(ParagraphStyle(textAlign = TextAlign.Start), block.start, block.end)
            SelectableArticle.Kind.Basmala -> {
                addStyle(ParagraphStyle(textAlign = TextAlign.Center), block.start, block.end)
                addStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.textPrimary), block.start, block.end)
            }
            SelectableArticle.Kind.Heading -> {
                // Room at the start for the accent bar drawn behind the text.
                addStyle(ParagraphStyle(textAlign = TextAlign.Start, textIndent = TextIndent(firstLine = 12.sp)), block.start, block.end)
                addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.textPrimary), block.start, block.end)
            }
            SelectableArticle.Kind.Ornament -> {
                addStyle(ParagraphStyle(textAlign = TextAlign.Center), block.start, block.end)
                addStyle(SpanStyle(color = colors.accent), block.start, block.end)
            }
        }
    }
}

private enum class DragMode { None, New, Start, End }

/** The one active drag (new selection or a handle), shared with the auto-scroll loop. */
private class SelectionDrag {
    var mode by mutableStateOf(DragMode.None)
    var active by mutableStateOf(false)
    var pointer = Offset.Zero
    var anchorStart = 0
    var anchorEnd = 0

    fun stop() {
        active = false
        mode = DragMode.None
    }
}

private val TOUCH_SIZE = 44.dp
private val KNOB_SIZE = 16.dp
/** How close to the viewport's top/bottom a drag starts auto-scrolling. */
private val AUTO_SCROLL_EDGE = 56.dp
/** Scroll per frame at (or past) the very edge. */
private val AUTO_SCROLL_MAX_STEP = 10.dp
