package com.example.feature.article.presentation.share.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.BrandPalette
import com.example.domain.text.SelectableArticle

/**
 * The cleaned article as ONE text (so a selection can run across paragraphs), selected
 * like any Android text:
 *  - long-press selects the word under the finger; keep dragging to extend it freely;
 *  - afterwards, press either round handle (no long-press needed) and drag it to keep
 *    selecting from that end - forwards or backwards, even past the other end;
 *  - dragging near the top or bottom of [viewport] auto-scrolls [scrollState];
 *  - a tap outside the selection clears it; a plain swipe just scrolls the page.
 *
 * All touch handling lives on this (non-moving) text, not on separate handle views: a
 * handle that moves while it's being dragged gets distorted finger deltas and stalls.
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
    val density = LocalDensity.current
    val text = remember(article, colors) { styled(article, colors) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val drag = remember { SelectionDrag() }
    val length = article.text.length

    val current by rememberUpdatedState(selectionStart to selectionEnd)
    val onChange by rememberUpdatedState(onSelectionChanged)

    val knobRadius = with(density) { KNOB_RADIUS.toPx() }
    val knobGap = with(density) { KNOB_GAP.toPx() }
    val touchRadius = with(density) { HANDLE_TOUCH_RADIUS.toPx() }
    val edgePx = with(density) { AUTO_SCROLL_EDGE.toPx() }
    val maxStepPx = with(density) { AUTO_SCROLL_MAX_STEP.toPx() }

    fun offsetAt(position: Offset): Int = (layout?.getOffsetForPosition(position) ?: 0).coerceIn(0, length)

    /** Where a selection edge's knob is drawn (and grabbed): under the edge's cursor. */
    fun knobCenter(lr: TextLayoutResult, edge: Int): Offset {
        val r = lr.getCursorRect(edge.coerceIn(0, length))
        return Offset(r.left, r.bottom + knobGap + knobRadius)
    }

    /** The finger at [finger] (text coordinates) moves the active edge; the anchor stays. */
    fun update(finger: Offset) {
        drag.finger = finger
        val at = offsetAt(finger + drag.grab)
        val start = minOf(drag.anchorStart, at)
        val end = maxOf(drag.anchorEnd, at)
        if (end > start && (start to end) != current) onChange(start, end)
    }

    // Edge auto-scroll while a drag is active: the finger stays put on screen, the text
    // moves under it, so the selection keeps growing.
    LaunchedEffect(drag.active) {
        while (drag.active) {
            withFrameNanos { }
            val coords = coordinates ?: continue
            val bounds = viewport() ?: continue
            val y = coords.localToWindow(drag.finger).y
            val speed = when {
                y < bounds.top + edgePx -> -((bounds.top + edgePx - y) / edgePx).coerceIn(0f, 1f) * maxStepPx
                y > bounds.bottom - edgePx -> ((y - (bounds.bottom - edgePx)) / edgePx).coerceIn(0f, 1f) * maxStepPx
                else -> 0f
            }
            if (speed != 0f) {
                val moved = scrollState.scrollBy(speed)
                if (moved != 0f) update(drag.finger.copy(y = drag.finger.y + moved))
            }
        }
    }

    /** Follows pointer [id] until it lifts, moving the active edge. */
    suspend fun AwaitPointerEventScope.track(id: PointerId) {
        try {
            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == id } ?: break
                if (!change.pressed) break
                change.consume()
                update(change.position)
            }
        } finally {
            drag.active = false
        }
    }

    // Absolute (left-based) placement: text-layout rects are left-based even in RTL.
    Box(
        contentAlignment = AbsoluteAlignment.TopLeft,
        modifier = modifier
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
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val lr = layout ?: return@awaitEachGesture
                    val (start, end) = current

                    // 1. A press on a handle takes over at once: drag that end.
                    if (end > start) {
                        val toStart = (down.position - knobCenter(lr, start)).getDistance()
                        val toEnd = (down.position - knobCenter(lr, end)).getDistance()
                        if (minOf(toStart, toEnd) <= touchRadius) {
                            val (edge, fixed) = if (toStart <= toEnd) start to end else end to start
                            val cursor = lr.getCursorRect(edge)
                            // Keep the knob's offset from the finger: aim at the edge's line.
                            drag.grab = Offset(cursor.left, cursor.center.y) - down.position
                            drag.anchorStart = fixed
                            drag.anchorEnd = fixed
                            drag.finger = down.position
                            drag.active = true
                            down.consume()
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            track(down.id)
                            return@awaitEachGesture
                        }
                    }

                    // 2. Otherwise wait for a long-press (a swipe before it scrolls the page).
                    val longPress = awaitLongPressOrCancellation(down.id)
                    if (longPress == null) {
                        // 3. A plain tap outside the selection clears it.
                        val up = currentEvent.changes.firstOrNull { it.id == down.id }
                        val isTap = up != null && !up.pressed && !up.isConsumed &&
                            (up.position - down.position).getDistance() < viewConfiguration.touchSlop
                        if (isTap && end > start) {
                            val at = offsetAt(up!!.position)
                            if (at < start || at > end) onChange(0, 0)
                        }
                        return@awaitEachGesture
                    }
                    longPress.consume()
                    val word = lr.getWordBoundary(offsetAt(longPress.position).coerceAtMost((length - 1).coerceAtLeast(0)))
                    val wordStart = word.start.coerceIn(0, length)
                    val wordEnd = word.end.coerceIn(wordStart, length).let { if (it == wordStart) (it + 1).coerceAtMost(length) else it }
                    drag.grab = Offset.Zero
                    drag.anchorStart = wordStart
                    drag.anchorEnd = wordEnd
                    drag.finger = longPress.position
                    drag.active = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onChange(wordStart, wordEnd)
                    track(longPress.id)
                }
            },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 1.9.em),
            color = colors.textSecondary,
            onTextLayout = { layout = it },
            modifier = Modifier
                .fillMaxWidth()
                // Room under the last line for its handle (bottom only: the text's origin,
                // which all offsets use, doesn't move).
                .padding(bottom = HANDLE_TOUCH_RADIUS)
                .onGloballyPositioned { coordinates = it }
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
                            cornerRadius = CornerRadius(2.dp.toPx()),
                        )
                    }
                    if (selectionEnd > selectionStart) {
                        // The highlight hugs each line of the selection.
                        drawPath(
                            lr.getPathForRange(selectionStart.coerceIn(0, length), selectionEnd.coerceIn(0, length)),
                            color = colors.accentStrong.copy(alpha = 0.32f),
                        )
                        // Each end: a cursor stem and a round knob under it.
                        listOf(selectionStart, selectionEnd).forEach { edge ->
                            val r = lr.getCursorRect(edge.coerceIn(0, length))
                            drawLine(colors.accentStrong, Offset(r.left, r.top), Offset(r.left, r.bottom + knobGap), strokeWidth = 2.dp.toPx())
                            drawCircle(colors.accentStrong, radius = knobRadius, center = knobCenter(lr, edge))
                        }
                    }
                },
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

/**
 * The one active drag. The selection is always [anchorStart]..[anchorEnd] stretched to
 * the finger: a new selection anchors on the long-pressed word, a handle drag on the
 * OTHER end - so a handle can be dragged either way, even past the other end.
 */
private class SelectionDrag {
    var active by mutableStateOf(false)
    var finger = Offset.Zero
    /** Added to the finger to aim at the edge's text line, not at the knob under it. */
    var grab = Offset.Zero
    var anchorStart = 0
    var anchorEnd = 0
}

private val KNOB_RADIUS = 7.dp
private val KNOB_GAP = 2.dp
/** How far from a knob's centre a press still grabs it. */
private val HANDLE_TOUCH_RADIUS = 24.dp
/** How close to the viewport's top/bottom a drag starts auto-scrolling. */
private val AUTO_SCROLL_EDGE = 56.dp
/** Scroll per frame at (or past) the very edge. */
private val AUTO_SCROLL_MAX_STEP = 10.dp
