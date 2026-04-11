package com.example.domain.repository

import androidx.paging.PagingData
import com.example.domain.module.Video
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface VideosRepository {

    fun uploadVideo(
        title: String,
        youtubeUrl: String,
    ): Flow<UploadResult>

    fun updateVideo(
        id: String,
        title: String,
        youtubeUrl: String,
    ): Flow<UploadResult>

    suspend fun deleteVideo(videoId: String): Result<Unit>

    suspend fun getVideoById(videoId: String): Video?

    suspend fun getAllRemoteVideos(): List<Video>

    fun getPaginatedVideo(): Flow<PagingData<Video>>

}
