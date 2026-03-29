package com.example.domain.use_cases.videos

import com.example.domain.module.Video
import com.example.domain.repository.VideosRepository
import javax.inject.Inject

class GetVideoByIdUseCase @Inject constructor(
    private val repository: VideosRepository
) {
    suspend operator fun invoke(videoId: String): Video? {
        return repository.getVideoById(videoId)
    }
}
