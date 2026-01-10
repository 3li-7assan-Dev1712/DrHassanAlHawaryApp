package com.example.feature.video.domain.use_case

import androidx.paging.PagingData
import com.example.feature.video.domain.model.Video
import com.example.feature.video.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedVideoUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(): Flow<PagingData<Video>> {
        return videoRepository.getPaginatedVideo()
    }
}