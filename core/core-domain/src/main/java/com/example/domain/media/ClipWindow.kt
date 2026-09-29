package com.example.domain.media

import kotlin.math.max
import kotlin.math.min

/**
 * The selected part of a recording, [startMs] until [endMs], for sharing as a clip. All
 * edits keep it inside 0..totalMs and at least [MIN_LENGTH_MS] long (or the whole track
 * when that's shorter). Pure maths for the share screen's selector, so it's unit-tested
 * and can never throw on an edge case (no `coerceIn` with min > max).
 */
data class ClipWindow(val startMs: Long, val endMs: Long) {

    val lengthMs: Long get() = endMs - startMs

    /** The whole window moved by [deltaMs], length kept, stopping at either end. */
    fun movedBy(deltaMs: Long, totalMs: Long): ClipWindow = startingAt(startMs + deltaMs, lengthMs, totalMs)

    /** Same length, centred on [centerMs] (the overview: "put the clip here"). */
    fun centeredAt(centerMs: Long, totalMs: Long): ClipWindow = startingAt(centerMs - lengthMs / 2, lengthMs, totalMs)

    /** A new start, the end fixed; never shorter than the minimum. */
    fun withStart(newStartMs: Long, totalMs: Long): ClipWindow {
        val minLength = minLength(totalMs)
        val start = newStartMs.clampTo(0L, max(0L, endMs - minLength))
        return ClipWindow(start, max(endMs, start + minLength).coerceAtMost(max(totalMs, minLength)))
    }

    /** A new end, the start fixed; never shorter than the minimum. */
    fun withEnd(newEndMs: Long, totalMs: Long): ClipWindow {
        val minLength = minLength(totalMs)
        val end = newEndMs.clampTo(min(startMs + minLength, totalMs), totalMs)
        return ClipWindow(min(startMs, max(0L, end - minLength)), end)
    }

    /** A new length, keeping the start unless the longer clip would run past the end. */
    fun withLength(lengthMs: Long, totalMs: Long): ClipWindow = startingAt(startMs, lengthMs, totalMs)

    /**
     * True when going from this window to [next] moves either edge across a [stepMs] mark
     * (0:05, 0:10, ...): the share screen ticks a haptic then while a finger drags.
     */
    fun crossesStep(next: ClipWindow, stepMs: Long = TICK_STEP_MS): Boolean =
        Math.floorDiv(startMs, stepMs) != Math.floorDiv(next.startMs, stepMs) ||
            Math.floorDiv(endMs, stepMs) != Math.floorDiv(next.endMs, stepMs)

    companion object {
        const val MIN_LENGTH_MS = 5_000L
        /** Haptic ticks while dragging: every 5 s an edge passes. */
        const val TICK_STEP_MS = 5_000L

        /** A window of [lengthMs] (clamped to 5 s..the track) starting as near [startMs] as fits. */
        fun startingAt(startMs: Long, lengthMs: Long, totalMs: Long): ClipWindow {
            val total = max(0L, totalMs)
            val length = lengthMs.clampTo(min(MIN_LENGTH_MS, total), total)
            val start = startMs.clampTo(0L, total - length)
            return ClipWindow(start, start + length)
        }

        private fun minLength(totalMs: Long) = min(MIN_LENGTH_MS, max(0L, totalMs))

        /** Like coerceIn, but a range with max < min collapses to min instead of throwing. */
        private fun Long.clampTo(low: Long, high: Long): Long = max(low, min(this, max(low, high)))
    }
}

/**
 * The zoomed "fine-tune" strip's visible range around a [ClipWindow]: the clip plus
 * context on both sides, so both edges can be dragged either way.
 */
data class ClipView(val startMs: Long, val lengthMs: Long) {
    val endMs: Long get() = startMs + lengthMs

    /**
     * True if [window] sits comfortably in this view: inside it with a margin on each side
     * (none needed where the view already touches the track's start or end), and neither
     * tiny nor filling it. When false, the strip re-centres ([around]) once the finger lifts.
     */
    fun fits(window: ClipWindow, totalMs: Long): Boolean {
        if (lengthMs <= 0) return false
        val margin = lengthMs / 10
        val startOk = if (startMs == 0L) window.startMs >= 0 else window.startMs >= startMs + margin
        val endOk = if (endMs >= totalMs) window.endMs <= totalMs else window.endMs <= endMs - margin
        val share = window.lengthMs.toDouble() / lengthMs
        val sizeOk = share <= 0.8 && (share >= 0.2 || lengthMs >= totalMs)
        return startOk && endOk && sizeOk
    }

    companion object {
        /** At least 40 s of context and never less than twice the clip, clamped to the track. */
        const val CONTEXT_MS = 40_000L

        fun around(window: ClipWindow, totalMs: Long): ClipView {
            val total = max(1L, totalMs)
            val length = max(window.lengthMs * 2, window.lengthMs + CONTEXT_MS).coerceAtMost(total).coerceAtLeast(1L)
            val start = (window.startMs + window.lengthMs / 2 - length / 2).coerceAtLeast(0L).coerceAtMost(total - length)
            return ClipView(start, length)
        }
    }
}
