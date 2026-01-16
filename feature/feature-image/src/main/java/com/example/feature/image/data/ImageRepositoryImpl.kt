package com.example.feature.image.data

import android.util.Log
import android.util.Log.e
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ImageEntity
import com.example.data_local.model.ImageGroupWithImages
import com.example.feature.image.domain.model.ImageGroup
import com.example.feature.image.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject


class ImageRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val imageRemoteMediator: ImageGroupRemoteMediator,
    private val imageFirestoreSource: ImageFirestoreSource
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


    override fun getImageGroupWithImages(groupId: String): Flow<ImageGroupWithImages?> {


        return flow {

            val localData = appDatabase.imageDao().getImageGroupWithImages(groupId)

            Log.d(
                "ImageRepositoryImpl",
                "getImageGroupWithImages: count: ${localData?.images?.size}"
            )
            if (localData != null && localData.images.isEmpty()) {
                try {
//                    val remoteImages = firebaseMediaSource.fetchImagesForGroup(groupId)
                    val remoteImages = imageFirestoreSource.fetchImagesForGroup(groupId)

                    Log.d(
                        "ImageRepositoryImpl",
                        "getImageGroupWithImages: count rem: ${remoteImages.size}"
                    )
                    if (remoteImages.isNotEmpty()) {
                        val imageEntities = remoteImages.map {
                            ImageEntity(
                                id = it.id.ifBlank { it.imageUrl },
                                groupId = groupId,
                                orderIndex = it.orderIndex,
                                imageUrl = it.imageUrl
                            )
                        }
                        appDatabase.imageDao().upsertImages(imageEntities)
                    }
                } catch (e: Exception) {
                    e(
                        "ImageRepository",
                        "Failed to fetch images for group $groupId",
                        e
                    )
                }
            }
            emitAll(appDatabase.imageDao().getObservableGroupWithImages(groupId))
        }.flowOn(Dispatchers.IO)


    }
}