package com.example.domain.use_cases.progress

import com.example.domain.module.PlaybackProgress
import com.example.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

/**
 * Saves where [audioUrl] is now: [positionMs] of [durationMs] (0 or less: not known yet). The
 * playback service calls it as the audio plays; an item without a URL is not saved.
 */
class SavePlaybackProgressUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    suspend operator fun invoke(
        audioUrl: String,
        title: String,
        positionMs: Long,
        durationMs: Long,
        savedAt: Long = System.currentTimeMillis(),
    ) {
        if (audioUrl.isBlank()) return
        val duration = durationMs.coerceAtLeast(0L)
        val position = positionMs.coerceAtLeast(0L).let { if (duration > 0L) it.coerceAtMost(duration) else it }
        repository.save(PlaybackProgress(audioUrl, title, position, duration, savedAt))
    }
}
