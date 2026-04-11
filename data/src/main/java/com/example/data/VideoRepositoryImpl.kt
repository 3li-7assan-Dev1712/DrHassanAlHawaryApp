package com.example.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.mappers.toDomainModel
import com.example.data.util.VideoRemoteMediator
import com.example.data_firebase.VideoFirestoreSource
import com.example.data_local.VideoDao
import com.example.domain.module.Video
import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoFirestoreSource: VideoFirestoreSource,
    private val videoRemoteMediator: VideoRemoteMediator,
    private val videoDao: VideoDao
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

    @OptIn(ExperimentalPagingApi::class)
    override fun getPaginatedVideo(): Flow<PagingData<Video>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            remoteMediator = videoRemoteMediator,
            pagingSourceFactory = {
                videoDao.pagingSource()
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                Video(
                    id = entity.id,
                    title = entity.title,
                    videoUrl = entity.videoUrl,
                    publishDate = Date(entity.publishDate),
                    youtubeVideoId = entity.youtubeVideoId,
                    type = entity.type
                )
            }
        }
    }


}
