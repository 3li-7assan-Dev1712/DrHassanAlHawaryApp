package com.example.domain.use_cases.series

import com.example.domain.module.Series
import com.example.domain.repository.SeriesRepository
import javax.inject.Inject

/** Saves a series' title, categoryId, hashtag and order (rename, reorder). */
class UpdateSeriesUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    suspend operator fun invoke(series: Series): Result<Unit> = repository.updateSeries(series)
}
