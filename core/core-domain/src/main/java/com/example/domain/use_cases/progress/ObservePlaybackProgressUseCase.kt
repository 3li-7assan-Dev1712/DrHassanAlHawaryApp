package com.example.domain.use_cases.progress

import com.example.domain.module.PlaybackProgress
import com.example.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Every saved position by audio URL, for the audio rows' progress. */
class ObservePlaybackProgressUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    operator fun invoke(): Flow<Map<String, PlaybackProgress>> =
        repository.observeAll().map { all -> all.associateBy { it.audioUrl } }
}
