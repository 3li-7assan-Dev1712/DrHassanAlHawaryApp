package com.example.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.mappers.toDomainModel
import com.example.data.util.VideoRemoteMediator
import com.example.data_firebase.FirebaseMediaSource
import com.example.data_local.AppDatabase
import com.example.domain.module.Video
import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class VideosRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseMediaSource: FirebaseMediaSource,
    private val videoRemoteMediator: VideoRemoteMediator
) : VideosRepository {

    private val videoDao = appDatabase.videoDao()

    @OptIn(ExperimentalPagingApi::class)
    fun getVideos(): Flow<PagingData<Video>> {
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
                entity.toDomainModel()
            }
        }
    }

    override fun uploadVideo(
        title: String,
        youtubeUrl: String,
    ): Flow<UploadResult> {

        return firebaseMediaSource.uploadVideo(title, youtubeUrl)

    }
}