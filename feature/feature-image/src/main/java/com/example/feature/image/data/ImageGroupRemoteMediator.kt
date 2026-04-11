package com.example.feature.image.data


import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ImageGroupEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

/**
 * ImageGroupRemoteMediator will load data from database when there is a cache data
 * when user scrolls down the Mediator will call the server (firebase firestore) to get new data,
 * store in room then update the UI accordingly.
 */
@OptIn(ExperimentalPagingApi::class)
class ImageGroupRemoteMediator @Inject constructor(
    private val imageFirestoreSource: ImageFirestoreSource,
    private val appDatabase: AppDatabase,
    private val networkRepositoryUseCase: GetCurrentNetworkStatusUseCase
) : RemoteMediator<Int, ImageGroupEntity>() {

    private val imageDao = appDatabase.imageDao()
    private val TAG = ImageGroupRemoteMediator::class.simpleName


    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ImageGroupEntity>
    ): MediatorResult {
        return try {
            val lastPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    if (networkRepositoryUseCase().first() == NetworkStatus.Unavailable) {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }
                    lastItem.publishDate
                }
            }

            val (fetchedImageGroupsPage, endOfPaginationReached) = imageFirestoreSource.fetchImageGroupsPage(
                startAfterPublishDate = lastPublishDate,
                limit = state.config.pageSize
            )

            appDatabase.withTransaction {

                val entities = fetchedImageGroupsPage.map {
                    ImageGroupEntity(
                        id = it.id,
                        title = it.title,
                        publishDate = it.publishDate.time,
                        previewImageUrl = it.previewImageUrl,
                        updatedAt = it.publishDate.time, // Default
                        isDeleted = it.isDeleted,
                        type = it.type
                    )
                }
                imageDao.upsertImageGroups(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred in RemoteMediator", e)
            return MediatorResult.Error(e)
        }
    }
}
