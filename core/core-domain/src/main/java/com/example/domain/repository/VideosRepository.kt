package com.example.domain.repository

import com.example.domain.module.SeriesPlacement
import androidx.paging.PagingData
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.module.Video
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface VideosRepository {

    fun uploadVideo(
        title: String,
        youtubeUrl: String,
        categoryId: String,
        placement: SeriesPlacement? = null,
    ): Flow<UploadResult>

    fun updateVideo(
        id: String,
        title: String,
        youtubeUrl: String,
        categoryId: String,
        /** Null leaves the video's series as it is. */
        placement: SeriesPlacement? = null,
    ): Flow<UploadResult>

    suspend fun deleteVideo(videoId: String): Result<Unit>

    suspend fun getVideoById(videoId: String): Video?

    suspend fun getAllRemoteVideos(): List<Video>

    fun getPaginatedVideo(categoryId: String? = null): Flow<PagingData<Video>>

    fun getCategories(type: ContentType): Flow<List<ContentCategory>>

}
