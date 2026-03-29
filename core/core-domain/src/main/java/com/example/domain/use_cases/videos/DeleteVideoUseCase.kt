package com.example.domain.use_cases.videos

import com.example.domain.repository.VideosRepository
import javax.inject.Inject

class DeleteVideoUseCase @Inject constructor(
    private val repository: VideosRepository
) {
    suspend operator fun invoke(videoId: String): Result<Unit> {
        return repository.deleteVideo(videoId)
    }
}
