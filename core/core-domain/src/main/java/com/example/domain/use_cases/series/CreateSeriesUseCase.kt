package com.example.domain.use_cases.series

import com.example.domain.module.Series
import com.example.domain.repository.SeriesRepository
import javax.inject.Inject

/** A new series: its title, collection, categoryId, hashtag and order. */
class CreateSeriesUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    suspend operator fun invoke(series: Series): Result<Unit> = repository.createSeries(series)
}
