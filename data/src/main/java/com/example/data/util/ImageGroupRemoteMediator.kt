package com.example.data.util


import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ImageGroupEntity
import com.example.data_local.model.ImageGroupRemoteKeysEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import com.example.domain.use_cases.IsUserLoggedInUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

/** Largest page fetched while skipping past pages that add nothing to the grid. */
private const val MAX_SKIP_PAGE_SIZE = 50

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
    private val imageFirestoreSource: ImageFirestoreSource,
    private val appDatabase: AppDatabase,
    private val networkRepositoryUseCase: GetCurrentNetworkStatusUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : RemoteMediator<Int, ImageGroupEntity>() {

    // Get both DAOs from the database instance
    private val imageDao = appDatabase.imageDao()
    private val imageGroupRemoteKeysDao = appDatabase.imageGroupRemoteKeysDao()

    private val TAG = ImageGroupRemoteMediator::class.simpleName


    override suspend fun initialize(): InitializeAction {
        // This is the key. On first launch, check if we have data.
        // If we do, don't launch a remote refresh. Show cache first.
        // If the database is empty, then launch a remote refresh.
        // Only standalone groups count: groups cached for a series screen aren't in the grid.
        return if (imageDao.countStandalone() > 0) {
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
        if (!isUserLoggedInUseCase()) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            val initialLoadKey = when (loadType) {
                LoadType.REFRESH -> null

                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    if (networkRepositoryUseCase().first() == NetworkStatus.Unavailable) {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }

                    lastItem.publishDate
                }
            }

            var currentLoadKey = initialLoadKey
            var lastResultEndOfPaginationReached = false
            var isFirstIteration = true
            var limit = state.config.pageSize

            // A page can come back entirely soft-deleted. The old code upserted every fetched
            // group with toEntity()'s default isDeleted=false, so deleted groups resurrected as
            // visible content instead of disappearing. Fixing that (by partitioning deleted vs
            // active, like Article/Audio/Video do) means an all-deleted page now upserts nothing
            // visible - which would leave the local "last item" cursor stuck forever re-fetching
            // the same dead page. So we also loop, advancing the cursor from the last document
            // actually fetched from Firestore, until we find active items or truly run out.
            // The same goes for a page of series groups: the grid shows standalone ones only
            // (a series is one card above it), and a series can fill many pages in a row, so
            // the pages grow while they add nothing.
            while (true) {
                val (fetchedImageGroupsPage, endOfPaginationReached) = imageFirestoreSource.fetchImageGroupsPage(
                    startAfterPublishDate = currentLoadKey,
                    limit = limit
                )

                lastResultEndOfPaginationReached = endOfPaginationReached

                if (fetchedImageGroupsPage.isEmpty()) break

                val (deletedItems, activeItems) = fetchedImageGroupsPage.partition { it.isDeleted }

                appDatabase.withTransaction {
                    if (loadType == LoadType.REFRESH && isFirstIteration) {
                        imageDao.clearAll()
                        imageGroupRemoteKeysDao.clearRemoteKeys()
                    }

                    deletedItems.forEach {
                        imageDao.deleteById(it.id)
                    }

                    if (activeItems.isNotEmpty()) {
                        val nextKey = if (lastResultEndOfPaginationReached) null else fetchedImageGroupsPage.last().id
                        val keys = activeItems.map { ImageGroupRemoteKeysEntity(groupId = it.id, nextKey = nextKey) }
                        imageGroupRemoteKeysDao.insertAll(keys)
                        imageDao.upsertImageGroups(activeItems.map { it.toEntity() })
                    }
                }

                isFirstIteration = false

                if (activeItems.any { it.seriesId == null } || lastResultEndOfPaginationReached) {
                    break
                }
                currentLoadKey = fetchedImageGroupsPage.last().publishDate.time
                limit = (limit * 2).coerceAtMost(MAX_SKIP_PAGE_SIZE)
            }

            MediatorResult.Success(endOfPaginationReached = lastResultEndOfPaginationReached)

        } catch (e: IOException) {
            // This is an expected error when offline during a REFRESH. Treat it as success.
            Log.w(TAG, "IOException, likely offline. Returning Success. Message: ${e.message}")
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred in RemoteMediator", e)
            MediatorResult.Error(e)
        }
    }
}
