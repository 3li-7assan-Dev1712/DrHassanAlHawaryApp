package com.example.data

import com.example.data.mappers.toDomainModel
import com.example.data_firebase.VideoFirestoreSource
import com.example.domain.module.Video
import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoFirestoreSource: VideoFirestoreSource
) : VideosRepository {

    override fun uploadVideo(title: String, youtubeUrl: String): Flow<UploadResult> {
        return videoFirestoreSource.uploadVideo(title, youtubeUrl)
    }

    override fun updateVideo(id: String, title: String, youtubeUrl: String): Flow<UploadResult> {
        return videoFirestoreSource.updateVideo(id, title, youtubeUrl)
    }

    override suspend fun deleteVideo(videoId: String): Result<Unit> {
        return videoFirestoreSource.deleteVideo(videoId)
    }

    override suspend fun getVideoById(videoId: String): Video? {
        return videoFirestoreSource.getVideoById(videoId)
    }

    override suspend fun getAllRemoteVideos(): List<Video> {
        return videoFirestoreSource.fetchVideoPage(null, 100)
            .filter { !it.isDeleted }
            .map {
                it.toDomainModel()
            }
    }
}
