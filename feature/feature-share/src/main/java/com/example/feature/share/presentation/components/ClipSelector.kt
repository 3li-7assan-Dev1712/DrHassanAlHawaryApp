package com.example.feature.share.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.media.ClipView
import com.example.domain.media.ClipWindow
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.BidiText
import kotlin.math.abs
import kotlin.math.max

/** Quick length presets; the length itself is free (the stepper and the edge handles). */
private val CLIP_LENGTHS = listOf(15_000L, 30_000L, 60_000L)
private const val NUDGE_MS = 5_000L
private const val LENGTH_STEP_MS = 5_000L
private const val OVERVIEW_BARS = 90
private const val DETAIL_BARS = 60

/**
 * Picking the part to share, in two plain steps:
 *  1. "اختر الموضع": the WHOLE recording. Tap or drag anywhere to put the clip there -
 *     the fast way to reach minute 40 of an hour-long lecture.
 *  2. "اضبط البداية والنهاية": a zoomed strip around the clip. Drag an edge handle to change
 *     where it starts or ends (any length, 5 s up to the whole track), drag the middle to
 *     slide it, or tap outside it to jump there. The view stays still while a finger is
 *     down, so the window and handles follow the finger exactly; it re-centres afterwards.
 * Plus length presets / a ±5 s length stepper, ±5 s nudges, and a preview play button.
 * Timelines run left to right even in RTL. The maths lives in [ClipWindow] (unit-tested).
 */
@Composable
fun ClipSelector(
    overviewEnvelope: FloatArray,
    totalMs: Long,
    startMs: Long,
    clipMs: Long,
    playbackPositionMs: Long,
    isPlaying: Boolean,
    isBuffering: Boolean,
    enabled: Boolean,
    onRangeChanged: (startMs: Long, endMs: Long) -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    val window = ClipWindow(startMs, startMs + clipMs)
    val apply: (ClipWindow) -> Unit = { onRangeChanged(it.startMs, it.endMs) }
    // Absolute preview position, shown as a playhead in both strips while previewing.
    val playhead = if (isPlaying || playbackPositionMs > 0) startMs + playbackPositionMs else null

    Column(modifier = modifier.fillMaxWidth()) {
        // Length: presets, then a free stepper.
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            CLIP_LENGTHS.filter { it <= totalMs }.forEach { length ->
                val selected = abs(clipMs - length) < 500
                Surface(
                    onClick = { apply(window.withLength(length, totalMs)) },
                    enabled = enabled,
                    shape = RoundedCornerShape(50),
                    color = if (selected) colors.accentContainer else colors.surface,
                    border = BorderStroke(if (selected) 1.dp else 0.5.dp, if (selected) colors.accentStrong else colors.divider),
                ) {
                    Text(
                        text = "${ArabicNumerals.digits((length / 1000).toInt())} ث",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) colors.onAccentContainer else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            LengthStepper(window, totalMs, enabled, apply)
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column {
                Spacer(Modifier.height(10.dp))
                Caption(
                    start = stringResource(R.string.share_step_place),
                    end = ArabicNumerals.formatMediaTime(totalMs),
                )
                OverviewStrip(overviewEnvelope, totalMs, window, playhead, enabled, apply)

                Spacer(Modifier.height(10.dp))
                Caption(start = stringResource(R.string.share_step_edges), end = null)
                DetailStrip(overviewEnvelope, totalMs, window, playhead, enabled, apply)

                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallButton(stringResource(R.string.share_nudge_back), enabled) { apply(window.movedBy(-NUDGE_MS, totalMs)) }
                    Text(
                        text = BidiText.ltr(
                            "${ArabicNumerals.formatMediaTime(window.startMs)} – ${ArabicNumerals.formatMediaTime(window.endMs)}",
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    SmallButton(stringResource(R.string.share_nudge_forward), enabled) { apply(window.movedBy(NUDGE_MS, totalMs)) }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onPlayPause, shape = CircleShape, color = colors.accentStrong, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (isBuffering) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onGold)
                    } else {
                        Icon(
                            painterResource(if (isPlaying) TablerIcons.PlayerPause else TablerIcons.PlayerPlay),
                            contentDescription = stringResource(if (isPlaying) R.string.share_pause else R.string.share_play),
                            tint = colors.onGold,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.share_listen_clip), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

/** A step label on the start side and an optional value on the end side (LTR row). */
@Composable
private fun Caption(start: String, end: String?) {
    val colors = Brand.colors
    // The row is LTR (timeline direction), but the Arabic label reads from the right.
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (end != null) Text(end, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
        Spacer(Modifier.weight(1f))
        Text(start, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
    }
}

/** "− ٠:٤٥ +": the length in 5 s steps, 5 s up to the whole track. */
@Composable
private fun LengthStepper(window: ClipWindow, totalMs: Long, enabled: Boolean, apply: (ClipWindow) -> Unit) {
    val colors = Brand.colors
    val length = window.lengthMs
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(colors.surface, RoundedCornerShape(50)),
        ) {
            StepButton("−", enabled && length > ClipWindow.MIN_LENGTH_MS) {
                // Snap to the 5 s grid first, so 47 s goes to 45 s, not 42 s.
                apply(window.withLength((length - 1) / LENGTH_STEP_MS * LENGTH_STEP_MS, totalMs))
            }
            Text(
                text = ArabicNumerals.formatMediaTime(length),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            StepButton("+", enabled && length < totalMs) {
                apply(window.withLength((length / LENGTH_STEP_MS + 1) * LENGTH_STEP_MS, totalMs))
            }
        }
    }
}

@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = Brand.colors
    Surface(onClick = onClick, enabled = enabled, shape = CircleShape, color = Color.Transparent) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) colors.accentText else colors.textMuted,
            )
        }
    }
}

@Composable
private fun SmallButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = Brand.colors
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

/** Step 1: the whole recording. Tap or drag anywhere and the clip centres there. */
@Composable
private fun OverviewStrip(
    envelope: FloatArray,
    totalMs: Long,
    window: ClipWindow,
    playhead: Long?,
    enabled: Boolean,
    apply: (ClipWindow) -> Unit,
) {
    val colors = Brand.colors
    val bars = remember(envelope, totalMs) { sample(envelope, totalMs, 0L, totalMs, OVERVIEW_BARS) }
    val currentWindow by rememberUpdatedState(window)
    fun msAt(x: Float, width: Int) = ((x / width).coerceIn(0f, 1f) * totalMs).toLong()

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(colors.surface, RoundedCornerShape(8.dp))
            .pointerInput(enabled, totalMs) {
                if (!enabled || totalMs <= 0) return@pointerInput
                detectTapGestures { apply(currentWindow.centeredAt(msAt(it.x, size.width), totalMs)) }
            }
            .pointerInput(enabled, totalMs) {
                if (!enabled || totalMs <= 0) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { apply(currentWindow.centeredAt(msAt(it.x, size.width), totalMs)) },
                ) { change, _ ->
                    change.consume()
                    apply(currentWindow.centeredAt(msAt(change.position.x, size.width), totalMs))
                }
            },
    ) {
        if (totalMs <= 0) return@Canvas
        val left = size.width * window.startMs / totalMs
        // Never thinner than a finger-visible marker, even for 15 s of a 2 h lecture.
        val right = max(size.width * window.endMs / totalMs, left + 4.dp.toPx())
        drawBars(bars, left, right, heightFraction = 0.7f, active = colors.accentStrong, idle = colors.textMuted.copy(alpha = 0.45f))
        drawRoundRect(
            color = colors.accentStrong.copy(alpha = 0.18f),
            topLeft = Offset(left, 0f),
            size = Size(right - left, size.height),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )
        drawRoundRect(
            color = colors.accentStrong,
            topLeft = Offset(left, 0f),
            size = Size(right - left, size.height),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
        playhead?.let { drawPlayhead(size.width * it / totalMs, colors.textPrimary) }
    }
}

/**
 * Step 2: the clip with context on both sides. Drag a handle to move that edge, drag the
 * middle to slide the clip, tap outside it to jump there. The view is frozen while a
 * finger is down; afterwards it re-centres only if the clip no longer sits well in it.
 */
@Composable
private fun DetailStrip(
    envelope: FloatArray,
    totalMs: Long,
    window: ClipWindow,
    playhead: Long?,
    enabled: Boolean,
    apply: (ClipWindow) -> Unit,
) {
    val colors = Brand.colors
    var view by remember { mutableStateOf(ClipView.around(window, totalMs)) }
    // The window as the finger has it right now (the VM's copy can lag a frame).
    var dragWindow by remember { mutableStateOf<ClipWindow?>(null) }
    val shown = dragWindow ?: window

    LaunchedEffect(window, totalMs, dragWindow) {
        if (dragWindow == null && !view.fits(window, totalMs)) view = ClipView.around(window, totalMs)
    }

    val bars = remember(envelope, view, totalMs) { sample(envelope, totalMs, view.startMs, view.lengthMs, DETAIL_BARS) }
    val currentWindow by rememberUpdatedState(window)
    val currentView by rememberUpdatedState(view)

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colors.surface, RoundedCornerShape(10.dp))
            .pointerInput(enabled, totalMs) {
                if (!enabled || totalMs <= 0) return@pointerInput
                detectTapGestures { offset ->
                    val v = currentView
                    val ms = v.startMs + (offset.x / size.width * v.lengthMs).toLong()
                    val w = currentWindow
                    // A tap outside the clip jumps it there; a tap inside does nothing.
                    if (ms < w.startMs || ms > w.endMs) apply(w.centeredAt(ms, totalMs))
                }
            }
            .pointerInput(enabled, totalMs) {
                if (!enabled || totalMs <= 0) return@pointerInput
                val slop = 32.dp.toPx()
                var mode = Drag.Move
                var from = ClipWindow(0, 0)
                var frozen = ClipView(0, 1)
                var dx = 0f
                detectDragGestures(
                    onDragStart = { offset ->
                        from = currentWindow
                        frozen = currentView
                        dx = 0f
                        val leftX = (from.startMs - frozen.startMs).toFloat() / frozen.lengthMs * size.width
                        val rightX = (from.endMs - frozen.startMs).toFloat() / frozen.lengthMs * size.width
                        val toLeft = abs(offset.x - leftX)
                        val toRight = abs(offset.x - rightX)
                        mode = when {
                            toLeft < slop && toLeft <= toRight -> Drag.Start
                            toRight < slop -> Drag.End
                            else -> Drag.Move
                        }
                        dragWindow = from
                    },
                    onDragEnd = { dragWindow = null },
                    onDragCancel = { dragWindow = null },
                ) { change, amount ->
                    change.consume()
                    // Measured from the gesture's start: no drift from rounding each step.
                    dx += amount.x
                    val deltaMs = (dx / size.width * frozen.lengthMs).toLong()
                    val next = when (mode) {
                        Drag.Start -> from.withStart(from.startMs + deltaMs, totalMs)
                        Drag.End -> from.withEnd(from.endMs + deltaMs, totalMs)
                        Drag.Move -> from.movedBy(deltaMs, totalMs)
                    }
                    dragWindow = next
                    apply(next)
                }
            },
    ) {
        if (totalMs <= 0 || view.lengthMs <= 0) return@Canvas
        fun x(ms: Long) = (ms - view.startMs).toFloat() / view.lengthMs * size.width
        val left = x(shown.startMs)
        val right = x(shown.endMs)
        drawBars(bars, left, right, heightFraction = 0.6f, active = colors.accentStrong, idle = colors.textMuted.copy(alpha = 0.45f))
        drawRoundRect(
            color = colors.accentStrong.copy(alpha = 0.14f),
            topLeft = Offset(left, 0f),
            size = Size(right - left, size.height),
            cornerRadius = CornerRadius(8.dp.toPx()),
        )
        drawRoundRect(
            color = colors.accentStrong,
            topLeft = Offset(left, 1f),
            size = Size(right - left, size.height - 2f),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawHandle(left, colors.accentStrong, colors.onGold)
        drawHandle(right, colors.accentStrong, colors.onGold)
        playhead?.let { if (it in view.startMs..view.endMs) drawPlayhead(x(it), colors.textPrimary) }
    }
}

private enum class Drag { Start, End, Move }

/** A grab-able edge: a solid pill with two grip lines, centred on [x]. */
private fun DrawScope.drawHandle(x: Float, fill: Color, grip: Color) {
    val w = 12.dp.toPx()
    val h = size.height * 0.72f
    val top = (size.height - h) / 2
    drawRoundRect(
        color = fill,
        topLeft = Offset(x - w / 2, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(w / 2),
    )
    val gripH = h * 0.36f
    val gripTop = (size.height - gripH) / 2
    listOf(-2.dp.toPx(), 2.dp.toPx()).forEach { dx ->
        drawLine(grip, Offset(x + dx, gripTop), Offset(x + dx, gripTop + gripH), strokeWidth = 1.dp.toPx())
    }
}

private fun DrawScope.drawPlayhead(x: Float, color: Color) {
    drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
}

/** Bars across the canvas; the ones between [left] and [right] (the clip) in gold. */
private fun DrawScope.drawBars(bars: FloatArray, left: Float, right: Float, heightFraction: Float, active: Color, idle: Color) {
    val pitch = size.width / bars.size
    val barWidth = pitch * 0.55f
    bars.forEachIndexed { i, amplitude ->
        val cx = (i + 0.5f) * pitch
        val h = size.height * heightFraction * max(amplitude, 0.12f)
        drawRoundRect(
            color = if (cx in left..right) active else idle,
            topLeft = Offset(cx - barWidth / 2, (size.height - h) / 2),
            size = Size(barWidth, h),
            cornerRadius = CornerRadius(barWidth / 2),
        )
    }
}


/** [count] amplitudes for [fromMs]..+[lengthMs] of the whole-track [envelope]. */
private fun sample(envelope: FloatArray, totalMs: Long, fromMs: Long, lengthMs: Long, count: Int): FloatArray {
    if (envelope.isEmpty() || totalMs <= 0 || lengthMs <= 0) return FloatArray(count)
    return FloatArray(count) { i ->
        val t = (fromMs + lengthMs * (i + 0.5f) / count) / totalMs
        envelope[(t * (envelope.size - 1)).toInt().coerceIn(0, envelope.size - 1)].coerceIn(0f, 1f)
    }
}

@Composable
private fun ClipSelectorPreviewContent() {
    val envelope = FloatArray(600) { i -> (0.5f + 0.45f * kotlin.math.sin(i / 9f) * kotlin.math.sin(i / 3f)).coerceIn(0f, 1f) }
    ClipSelector(
        overviewEnvelope = envelope,
        totalMs = 3_500_000L,
        startMs = 1_260_000L,
        clipMs = 45_000L,
        playbackPositionMs = 12_000L,
        isPlaying = true,
        isBuffering = false,
        enabled = true,
        onRangeChanged = { _, _ -> },
        onPlayPause = {},
        modifier = Modifier.padding(16.dp),
    )
}

@androidx.compose.ui.tooling.preview.Preview(name = "Clip selector - light", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFFF4EEE5)
@Composable
private fun ClipSelectorLightPreview() {
    com.example.core.ui.theme.HassanAlHawaryTheme(darkTheme = false) { ClipSelectorPreviewContent() }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Clip selector - dark", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFF1A1512)
@Composable
private fun ClipSelectorDarkPreview() {
    com.example.core.ui.theme.HassanAlHawaryTheme(darkTheme = true) { ClipSelectorPreviewContent() }
}
