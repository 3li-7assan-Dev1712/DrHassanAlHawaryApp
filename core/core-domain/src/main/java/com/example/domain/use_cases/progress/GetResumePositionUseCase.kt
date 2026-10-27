package com.example.domain.use_cases.progress

import com.example.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

/**
 * Where the player opens [audioUrl]: where it was left while it is in progress, else from the
 * start (never played, only glanced at, or finished).
 */
class GetResumePositionUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    suspend operator fun invoke(audioUrl: String): Long =
        repository.get(audioUrl)?.resumePositionMs ?: 0L
}
