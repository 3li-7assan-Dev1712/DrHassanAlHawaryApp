package com.example.feature.image.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data_local.AppDatabase
import com.example.feature.image.domain.model.ImageGroup
import com.example.feature.image.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject


class ImageRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val imageRemoteMediator: ImageGroupRemoteMediator
) : ImageRepository {


    /**
     * Gets a Flow of PagingData for ImageGroups.
     * It uses a RemoteMediator to orchestrate fetching from Firebase and caching in Room.
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun getPaginatedImageGroups(): Flow<PagingData<ImageGroup>> {
        val imageDao = appDatabase.imageDao()
        val pagingSourceFactory = { imageDao.pagingSource() }

        return Pager(
            config = PagingConfig(
                pageSize = 20, // Define how many items to load at once
                enablePlaceholders = false
            ),
            remoteMediator = imageRemoteMediator,
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map {
                ImageGroup(
                    id = it.id,
                    title = it.title,
                    publishDate = Date(it.publishDate),
                    previewImageUrl = it.previewImageUrl
                )
            }

        }
    }


}