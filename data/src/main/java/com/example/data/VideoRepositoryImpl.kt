package com.example.data

import com.example.domain.module.SeriesPlacement
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.mappers.toDomainModel
import com.example.data.util.VideoRemoteMediator
import com.example.data_firebase.VideoFirestoreSource
import com.example.data_local.VideoDao
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.module.Video
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoFirestoreSource: VideoFirestoreSource,
    private val videoRemoteMediator: VideoRemoteMediator,
    private val videoDao: VideoDao,
    private val categoryRepository: CategoryRepository,
) : VideosRepository {

    override fun uploadVideo(
        title: String,
        youtubeUrl: String,
        categoryId: String,
        placement: SeriesPlacement?,
    ): Flow<UploadResult> {
        return videoFirestoreSource.uploadVideo(title, youtubeUrl, categoryId = categoryId, placement = placement)
    }

    override fun updateVideo(
        id: String,
        title: String,
        youtubeUrl: String,
        categoryId: String,
        placement: SeriesPlacement?,
    ): Flow<UploadResult> {
        return videoFirestoreSource.updateVideo(id, title, youtubeUrl, categoryId = categoryId, placement = placement)
    }

    override suspend fun deleteVideo(videoId: String): Result<Unit> {
        return videoFirestoreSource.deleteVideo(videoId)
    }

    override suspend fun getVideoById(videoId: String): Video? {
        return videoFirestoreSource.getVideoById(videoId)
    }

    // Every document, not only the newest 100: the admin's list searches and filters all of them.
    override suspend fun getAllRemoteVideos(): List<Video> {
        return videoFirestoreSource.fetchAllVideos()
            .filter { !it.isDeleted }
            .map {
                it.toDomainModel()
            }
    }

    override fun getCategories(type: ContentType): Flow<List<ContentCategory>> {
        return categoryRepository.getCategories(type)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPaginatedVideo(categoryId: String?): Flow<PagingData<Video>> {
        videoRemoteMediator.categoryId = categoryId
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            remoteMediator = videoRemoteMediator,
            pagingSourceFactory = {
                videoDao.pagingSource(categoryId)
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toDomainModel()
            }
        }
    }


}
