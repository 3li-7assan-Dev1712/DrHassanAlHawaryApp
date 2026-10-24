package com.example.domain.use_cases.series

import com.example.domain.repository.SeriesRepository
import javax.inject.Inject

/** Pulls the series changed since the last sync into the local cache. */
class SyncSeriesUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    suspend operator fun invoke() = repository.syncSeries()
}
