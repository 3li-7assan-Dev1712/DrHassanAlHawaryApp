package com.example.feature.video.data


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data_firebase.FirebaseMediaSource
import com.example.data_local.AppDatabase
import com.example.feature.video.domain.model.Video
import com.example.feature.video.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject


class VideoRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseMediaSource: FirebaseMediaSource,
    private val videoRemoteMediator: VideoRemoteMediator
) : VideoRepository {

    private val videoDao = appDatabase.videoDao()

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
                    youtubeVideoId = entity.youtubeVideoId
                )
            }
        }
    }

}