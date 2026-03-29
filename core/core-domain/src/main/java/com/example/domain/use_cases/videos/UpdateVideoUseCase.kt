package com.example.domain.use_cases.videos

import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateVideoUseCase @Inject constructor(
    private val repository: VideosRepository
) {
    operator fun invoke(id: String, title: String, youtubeUrl: String): Flow<UploadResult> {
        return repository.updateVideo(id, title, youtubeUrl)
    }
}
