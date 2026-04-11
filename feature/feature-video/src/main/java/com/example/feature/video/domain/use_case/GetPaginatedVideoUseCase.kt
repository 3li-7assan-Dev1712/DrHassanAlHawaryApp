package com.example.feature.video.domain.use_case

import androidx.paging.PagingData
import com.example.domain.module.Video
import com.example.domain.repository.VideosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedVideoUseCase @Inject constructor(
    private val videoRepository: VideosRepository
) {
    operator fun invoke(): Flow<PagingData<Video>> {
        return videoRepository.getPaginatedVideo()
    }
}