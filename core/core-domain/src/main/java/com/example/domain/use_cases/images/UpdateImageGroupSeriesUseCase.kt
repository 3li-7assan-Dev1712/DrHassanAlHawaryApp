package com.example.domain.use_cases.images

import com.example.domain.module.SeriesPlacement
import com.example.domain.repository.ImagesRepository
import javax.inject.Inject

/** Moves a design group into a series, or out of one (standalone). */
class UpdateImageGroupSeriesUseCase @Inject constructor(
    private val repository: ImagesRepository
) {
    suspend operator fun invoke(groupId: String, placement: SeriesPlacement): Result<Unit> =
        repository.updateImageGroupSeries(groupId, placement)
}
