package com.example.domain.repository

import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface VideosRepository {

    fun uploadVideo(
        title: String,
        youtubeUrl: String
    ): Flow<UploadResult>


}