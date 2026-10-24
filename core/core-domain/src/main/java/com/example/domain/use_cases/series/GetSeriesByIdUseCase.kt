package com.example.domain.use_cases.series

import com.example.domain.module.Series
import com.example.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSeriesByIdUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    operator fun invoke(seriesId: String): Flow<Series?> = repository.getSeriesById(seriesId)
}
