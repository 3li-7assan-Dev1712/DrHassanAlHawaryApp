package com.example.feature.share.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.BidiText
import kotlin.math.abs
import kotlin.math.max

/** Clip lengths offered as chips, in ms. */
private val CLIP_LENGTHS = listOf(15_000L, 30_000L, 60_000L)
private const val NUDGE_MS = 5_000L
private const val STRIP_BARS = 60
private const val MIN_CLIP_MS = 5_000L

/**
 * The one clip selector: duration chips, a thin overview of the whole lecture with the
 * selection marked, a zoomed waveform strip (twice the clip, centred on it) with a
 * draggable window and handles, ±5 s nudges with the range label, and a play button that
 * previews the selection. Timelines run left to right even in RTL.
 */
@Composable
fun ClipSelector(
    overviewEnvelope: FloatArray,
    totalMs: Long,
    startMs: Long,
    clipMs: Long,
    isPlaying: Boolean,
    isBuffering: Boolean,
    enabled: Boolean,
    onLengthSelected: (Long) -> Unit,
    onRangeChanged: (startMs: Long, endMs: Long) -> Unit,
    onNudge: (Long) -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CLIP_LENGTHS.filter { it <= max(totalMs, CLIP_LENGTHS.first()) }.forEach { length ->
                val selected = abs(clipMs - length) < 500
                Surface(
                    onClick = { onLengthSelected(length) },
                    enabled = enabled,
                    shape = RoundedCornerShape(50),
                    color = if (selected) colors.accentContainer else colors.surface,
                    border = BorderStroke(if (selected) 1.dp else 0.5.dp, if (selected) colors.accentStrong else colors.divider),
                ) {
                    Text(
                        text = "${ArabicNumerals.digits((length / 1000).toInt())} ث",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) colors.onAccentContainer else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column {
                OverviewBar(totalMs = totalMs, startMs = startMs, clipMs = clipMs)
                Spacer(Modifier.height(8.dp))
                ZoomedStrip(
                    envelope = overviewEnvelope,
                    totalMs = totalMs,
                    startMs = startMs,
                    clipMs = clipMs,
                    enabled = enabled,
                    onRangeChanged = onRangeChanged,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NudgeButton(stringResource(R.string.share_nudge_back), enabled) { onNudge(-NUDGE_MS) }
                    Text(
                        text = BidiText.ltr(
                            "${ArabicNumerals.formatMediaTime(startMs)} – ${ArabicNumerals.formatMediaTime(startMs + clipMs)}",
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    NudgeButton(stringResource(R.string.share_nudge_forward), enabled) { onNudge(NUDGE_MS) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onPlayPause,
                shape = CircleShape,
                color = colors.accentStrong,
                modifier = Modifier.size(44.dp),
            ) {
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

@Composable
private fun NudgeButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = Brand.colors
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/** The whole lecture as a thin track, the selection marked in accentStrong. */
@Composable
private fun OverviewBar(totalMs: Long, startMs: Long, clipMs: Long) {
    val colors = Brand.colors
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(6.dp),
    ) {
        val radius = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(colors.surfaceMuted, cornerRadius = radius)
        if (totalMs > 0) {
            val left = size.width * startMs / totalMs
            val width = max(size.width * clipMs / totalMs, size.height)
            drawRoundRect(colors.accentStrong, topLeft = Offset(left, 0f), size = Size(width, size.height), cornerRadius = radius)
        }
    }
}

/**
 * About twice the clip, centred on it: the bars, the selected window with a handle at
 * each end. Dragging inside the window moves it; dragging a handle resizes that side.
 */
@Composable
private fun ZoomedStrip(
    envelope: FloatArray,
    totalMs: Long,
    startMs: Long,
    clipMs: Long,
    enabled: Boolean,
    onRangeChanged: (Long, Long) -> Unit,
) {
    val colors = Brand.colors
    val visibleMs = (clipMs * 2).coerceAtMost(totalMs).coerceAtLeast(1L)
    val visibleStart = (startMs + clipMs / 2 - visibleMs / 2).coerceIn(0L, (totalMs - visibleMs).coerceAtLeast(0L))
    val bars = remember(envelope, visibleStart, visibleMs, totalMs) { sample(envelope, totalMs, visibleStart, visibleMs) }

    val currentStart by rememberUpdatedState(startMs)
    val currentClip by rememberUpdatedState(clipMs)
    val currentVisibleStart by rememberUpdatedState(visibleStart)
    val currentVisibleMs by rememberUpdatedState(visibleMs)

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colors.surface, RoundedCornerShape(10.dp))
            .pointerInput(enabled, totalMs) {
                if (!enabled || totalMs <= 0) return@pointerInput
                val handleSlop = 20.dp.toPx()
                var mode = 0 // 0 move, 1 start handle, 2 end handle
                // Frozen for the whole gesture, so the strip doesn't re-centre under the finger.
                var frozenVisibleStart = 0L
                var frozenVisibleMs = 1L
                var dragStart = 0L
                var dragEnd = 0L
                detectDragGestures(
                    onDragStart = { offset ->
                        frozenVisibleStart = currentVisibleStart
                        frozenVisibleMs = currentVisibleMs
                        dragStart = currentStart
                        dragEnd = currentStart + currentClip
                        val leftX = (dragStart - frozenVisibleStart).toFloat() / frozenVisibleMs * size.width
                        val rightX = (dragEnd - frozenVisibleStart).toFloat() / frozenVisibleMs * size.width
                        mode = when {
                            abs(offset.x - leftX) < handleSlop -> 1
                            abs(offset.x - rightX) < handleSlop -> 2
                            else -> 0
                        }
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val deltaMs = (amount.x / size.width * frozenVisibleMs).toLong()
                        when (mode) {
                            1 -> dragStart = (dragStart + deltaMs).coerceIn(0L, dragEnd - MIN_CLIP_MS)
                            2 -> dragEnd = (dragEnd + deltaMs).coerceIn(dragStart + MIN_CLIP_MS, totalMs)
                            else -> {
                                val length = dragEnd - dragStart
                                dragStart = (dragStart + deltaMs).coerceIn(0L, totalMs - length)
                                dragEnd = dragStart + length
                            }
                        }
                        onRangeChanged(dragStart, dragEnd)
                    },
                )
            },
    ) {
        val pitch = size.width / STRIP_BARS
        val barWidth = pitch * 0.5f
        val windowLeft = (startMs - visibleStart).toFloat() / visibleMs * size.width
        val windowRight = (startMs + clipMs - visibleStart).toFloat() / visibleMs * size.width
        for (i in 0 until STRIP_BARS) {
            val cx = (i + 0.5f) * pitch
            val h = size.height * 0.75f * max(bars[i], 0.12f)
            val inside = cx in windowLeft..windowRight
            drawRoundRect(
                color = if (inside) colors.accentStrong else colors.textMuted.copy(alpha = 0.45f),
                topLeft = Offset(cx - barWidth / 2, (size.height - h) / 2),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
            )
        }
        // The window, and a handle at each end.
        drawRoundRect(
            color = colors.accentStrong,
            topLeft = Offset(windowLeft, 1f),
            size = Size(windowRight - windowLeft, size.height - 2f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 2.dp.toPx()),
        )
        val handleW = 6.dp.toPx()
        val handleH = size.height * 0.5f
        listOf(windowLeft, windowRight).forEach { x ->
            drawRoundRect(
                color = colors.accentStrong,
                topLeft = Offset(x - handleW / 2, (size.height - handleH) / 2),
                size = Size(handleW, handleH),
                cornerRadius = CornerRadius(handleW / 2, handleW / 2),
            )
        }
    }
}

/** [STRIP_BARS] amplitudes for [visibleStart]..+[visibleMs] out of the whole-track [envelope]. */
private fun sample(envelope: FloatArray, totalMs: Long, visibleStart: Long, visibleMs: Long): FloatArray {
    if (envelope.isEmpty() || totalMs <= 0) return FloatArray(STRIP_BARS)
    return FloatArray(STRIP_BARS) { i ->
        val t = (visibleStart + visibleMs * (i + 0.5f) / STRIP_BARS) / totalMs
        envelope[(t * (envelope.size - 1)).toInt().coerceIn(0, envelope.size - 1)].coerceIn(0f, 1f)
    }
}
