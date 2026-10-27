package com.example.domain.module

/**
 * Where an audio was left: [positionMs] of [durationMs] (0 when the player never learned it),
 * saved by the playback service while it plays, [updatedAt] the time of that save. Keyed by
 * the audio's URL, the id the player and the audio routes know it by. The [title] is the
 * stored one, as the player was given it.
 */
data class PlaybackProgress(
    val audioUrl: String,
    val title: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
) {

    val remainingMs: Long get() = (durationMs - positionMs).coerceAtLeast(0L)

    /** Heard to the end, or so close to it that nothing is left to resume. */
    val isFinished: Boolean get() = durationMs > 0L && remainingMs <= finishedMarginMs(durationMs)

    /** Past its first seconds and not finished: worth resuming. */
    val isInProgress: Boolean get() = positionMs >= MIN_PROGRESS_MS && !isFinished

    /** Where the player opens it: here while it is in progress, else from the start. */
    val resumePositionMs: Long get() = if (isInProgress) positionMs else 0L

    /** The part heard, 0..1 (0 while the duration is unknown). */
    val fraction: Float get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    companion object {
        /** Less than this was a glance, not a start: the audio opens from the beginning again. */
        const val MIN_PROGRESS_MS = 5_000L

        private const val MAX_FINISHED_MARGIN_MS = 30_000L
        private const val FINISHED_MARGIN_FRACTION = 0.05

        /**
         * How close to the end counts as finished: 5% of the audio, at most 30 seconds (the
         * closing words of a long lecture, a few seconds of a short fatwa).
         */
        fun finishedMarginMs(durationMs: Long): Long =
            minOf(MAX_FINISHED_MARGIN_MS, (durationMs * FINISHED_MARGIN_FRACTION).toLong())
    }
}
