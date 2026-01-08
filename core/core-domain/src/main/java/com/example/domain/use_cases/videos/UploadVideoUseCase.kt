package com.example.domain.use_cases.videos


import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadVideoUseCase @Inject constructor(
    private val repository: VideosRepository
) {
    operator fun invoke(title: String, videoUrl: String): Flow<UploadResult> {
        return repository.uploadVideo(title, videoUrl)
    }
}