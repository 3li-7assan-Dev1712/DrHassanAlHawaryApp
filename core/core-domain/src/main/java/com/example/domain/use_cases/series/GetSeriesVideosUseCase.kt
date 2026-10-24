package com.example.domain.use_cases.series

import com.example.domain.module.Video
import com.example.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** A series' items from the local cache, by episodeNumber (see [RefreshSeriesItemsUseCase]). */
class GetSeriesVideosUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    operator fun invoke(seriesId: String): Flow<List<Video>> = repository.getSeriesVideos(seriesId)
}
