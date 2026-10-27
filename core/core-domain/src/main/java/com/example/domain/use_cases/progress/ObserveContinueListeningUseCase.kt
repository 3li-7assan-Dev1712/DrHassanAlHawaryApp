package com.example.domain.use_cases.progress

import com.example.domain.module.PlaybackProgress
import com.example.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** «متابعة الاستماع»: the audios left part way, the latest first, [limit] at most. */
class ObserveContinueListeningUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<PlaybackProgress>> =
        repository.observeAll().map { all ->
            all.filter { it.isInProgress }
                .sortedByDescending { it.updatedAt }
                .take(limit)
        }

    companion object {
        const val DEFAULT_LIMIT = 10
    }
}
