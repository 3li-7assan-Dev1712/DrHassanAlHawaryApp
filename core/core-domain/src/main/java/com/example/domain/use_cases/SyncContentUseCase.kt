package com.example.domain.use_cases

import com.example.domain.repository.ContentSyncRepository
import com.example.domain.repository.SeriesRepository
import javax.inject.Inject

/** The app-start delta sync: the series, then what changed in the cached content. */
class SyncContentUseCase @Inject constructor(
    private val seriesRepository: SeriesRepository,
    private val contentSyncRepository: ContentSyncRepository
) {
    suspend operator fun invoke() {
        seriesRepository.syncSeries()
        contentSyncRepository.syncChangedContent()
    }
}
