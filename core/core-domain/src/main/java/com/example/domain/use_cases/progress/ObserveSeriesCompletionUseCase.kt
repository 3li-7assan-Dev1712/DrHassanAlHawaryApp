package com.example.domain.use_cases.progress

import com.example.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The lessons finished in each series the listener has started (a lesson finished or under
 * way), by series id: «أكملت ١٢ من ٥٩» on its card. Series not started are absent.
 */
class ObserveSeriesCompletionUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    operator fun invoke(): Flow<Map<String, Int>> =
        repository.observeSeriesLessons().map { lessons ->
            lessons.filter { it.progress.isInProgress || it.progress.isFinished }
                .groupBy { it.seriesId }
                .mapValues { (_, inSeries) -> inSeries.count { it.progress.isFinished } }
        }
}
