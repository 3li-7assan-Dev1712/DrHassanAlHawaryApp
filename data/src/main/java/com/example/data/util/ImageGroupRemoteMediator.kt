package com.example.data.util


import android.util.Log
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
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

/**
 * ImageGroupRemoteMediator will load data from database when there is a cache data
 * when user scrolls down the Mediator will call the server (fireaase firestore) to get new data,
 * store in room then update the UI accordingly.
 *
 * @param firebaseMediaSource a helper class to read data from firebase
 * @param appDatabase the room database containing the images data
 * @param networkRepositoryUseCase a use case to get the internet status (AVAILABLE OR NOT)
 */
@OptIn(ExperimentalPagingApi::class)
class ImageGroupRemoteMediator @Inject constructor(
    private val firebaseMediaSource: FirebaseMediaSource,
    private val appDatabase: AppDatabase,
    private val networkRepositoryUseCase: GetCurrentNetworkStatusUseCase
) : RemoteMediator<Int, ImageGroupEntity>() {

    // Get both DAOs from the database instance
    private val imageDao = appDatabase.imageDao()
    private val imageGroupRemoteKeysDao = appDatabase.imageGroupRemoteKeysDao()

    private val TAG = ImageGroupRemoteMediator::class.simpleName



    override suspend fun initialize(): InitializeAction {
        // This is the key. On first launch, check if we have data.
        // If we do, don't launch a remote refresh. Show cache first.
        // If the database is empty, then launch a remote refresh.
        return if (imageDao.count() > 0) {
            Log.d(TAG, "DB has data. Skipping remote refresh on launch.")
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            Log.d(TAG, "DB is empty. Launching remote refresh on launch.")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ImageGroupEntity>
    ): MediatorResult {
        return try {
            // 1. Determine the key for the page to load (the 'loadKey')
            val loadKey = when (loadType) {
                // REFRESH always starts from the beginning (null key)
                LoadType.REFRESH -> null

                // We don't support paging backwards
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

                // This is the main logic for scrolling down
                LoadType.APPEND -> {
                    // --- Start of Refactored APPEND Block ---
                    val lastItem = state.lastItemOrNull()


                    // Step 1: Handle the OFFLINE case first and exit.
                    if (networkRepositoryUseCase().first() == NetworkStatus.Unavailable) {
                        val lastItem = state.lastItemOrNull()
                        // If offline, pagination ends only if the local PagingSource (Room) is also empty.
                        if (lastItem == null) {
                            Log.d(TAG, "Offline and local data is fully loaded.")
                            return MediatorResult.Success(endOfPaginationReached = true)
                        } else {
                            Log.d(TAG, "Offline, allowing local paging to continue.")
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }

                    // Step 2: If we reach here, we are ONLINE. Get the key for the next network page.
                    Log.d(TAG, "Online, determining next key for network fetch.")
                    val lastRemoteKey = imageGroupRemoteKeysDao.getLastRemoteKey()

                    // If the last key is null or its nextKey is null, it might be because the cache is stale.
                    // This is NOT a reason to stop. It just means the next fetch should start from the last known item.
                    // We return the nextKey, which might be null, but the fetch logic will handle it.
                    if (lastRemoteKey?.nextKey == null) {
                        Log.d(TAG, "End of LOCAL keys reached. Will attempt network fetch to check for new data.")
                    }

                    // Return the key for the next page. If the key is null, the server will return the next page after the last known item.
                    lastRemoteKey?.nextKey
                }
            }

            // If loadKey is null here, it's because it was a REFRESH or the APPEND logic decided there's no more data.
            // The fetch logic handles a null key correctly (fetches the first page).
            Log.d(TAG, "Proceeding to fetch from network with key: $loadKey")

            // 2. Fetch the page of data from Firebase
            val fetchedImageGroupsPage = firebaseMediaSource.fetchImageGroupsPage(
                startAfterKey = loadKey,
                limit = state.config.pageSize
            )

            val endOfPaginationReached = fetchedImageGroupsPage.size < state.config.pageSize

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
            // This is an expected error when offline during a REFRESH. Treat it as success.
            Log.w(TAG, "IOException, likely offline. Returning Success. Message: ${e.message}")
            return MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred in RemoteMediator", e)
            return MediatorResult.Error(e)
        }
    }
}
