package com.example.data.util


import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.FirebaseMediaSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ImageGroupEntity
import com.example.data_local.model.ImageGroupRemoteKeysEntity
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ImageGroupRemoteMediator @Inject constructor(
    private val firebaseMediaSource: FirebaseMediaSource,
    private val appDatabase: AppDatabase
) : RemoteMediator<Int, ImageGroupEntity>() {

    // Get both DAOs from the database instance
    private val imageDao = appDatabase.imageDao()
    private val imageGroupRemoteKeysDao = appDatabase.imageGroupRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ImageGroupEntity>
    ): MediatorResult {
        return try {
            // 1. Determine the key for the page to load
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null // Refresh starts from the beginning
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true) // We only page forward
                LoadType.APPEND -> {
                    // Get the last item loaded from the database
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    // Use its ID to find the remote key
                    val remoteKey = imageGroupRemoteKeysDao.getRemoteKeyByGroupId(lastItem.id)
                    remoteKey?.nextKey // This is the key for the next page
                }
            }

            // 2. Fetch the page of data from Firebase
            val fetchedImageGroupsPage = firebaseMediaSource.fetchImageGroupsPage(
                startAfterKey = loadKey,
                limit = state.config.pageSize
            )

            val endOfPaginationReached = fetchedImageGroupsPage.isEmpty()

            // 3. Save the new data and keys in a single database transaction
            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    imageDao.clearAll()
                    imageGroupRemoteKeysDao.clearRemoteKeys()
                }

                val nextKey = if (endOfPaginationReached) null else fetchedImageGroupsPage.last().id
                val keys = fetchedImageGroupsPage.map {
                    ImageGroupRemoteKeysEntity(groupId = it.id, nextKey = nextKey)
                }

                imageGroupRemoteKeysDao.insertAll(keys)
                imageDao.upsertImageGroups(fetchedImageGroupsPage.map { it.toEntity() })
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
