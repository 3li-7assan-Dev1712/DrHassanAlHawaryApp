package com.example.domain.use_cases.series

import com.example.domain.module.Series
import com.example.domain.repository.SeriesRepository
import javax.inject.Inject

/** Fetches all of a series' items into the local cache. False if the fetch failed. */
class RefreshSeriesItemsUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    suspend operator fun invoke(series: Series): Boolean = repository.refreshSeriesItems(series)
}
