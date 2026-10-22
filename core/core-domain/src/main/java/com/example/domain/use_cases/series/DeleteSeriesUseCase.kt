package com.example.domain.use_cases.series

import com.example.domain.repository.SeriesRepository
import javax.inject.Inject

/** Soft-deletes a series; its items keep their seriesId. */
class DeleteSeriesUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    suspend operator fun invoke(seriesId: String): Result<Unit> = repository.deleteSeries(seriesId)
}
