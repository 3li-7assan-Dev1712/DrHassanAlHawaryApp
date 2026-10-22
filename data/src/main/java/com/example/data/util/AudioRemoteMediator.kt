package com.example.data.util

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.AudioEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import com.example.domain.use_cases.IsUserLoggedInUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

/** Largest page fetched while skipping past pages that add nothing to the list. */
private const val MAX_SKIP_PAGE_SIZE = 50

@OptIn(ExperimentalPagingApi::class)
class AudioRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val audioFirestoreSource: AudioFirestoreSource,
    private val networkStatusUseCase: GetCurrentNetworkStatusUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : RemoteMediator<Int, AudioEntity>() {

    private val audioDao = appDatabase.audioDao()
    private val TAG = "AudioRemoteMediator"

    var categoryId: String? = null

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AudioEntity>
    ): MediatorResult {
        if (!isUserLoggedInUseCase()) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            val initialPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastLocalItem = state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()

                    if (lastLocalItem == null) {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }

                    if (networkStatusUseCase().first() == NetworkStatus.Unavailable) {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }
                    lastLocalItem.publishDate
                }
            }

            var currentLastPublishDate = initialPublishDate
            var lastResultEndOfPaginationReached = false
            var limit = state.config.pageSize

            while (true) {
                val audiosFromServer = audioFirestoreSource.fetchAudioPage(
                    startAfterPublishDate = currentLastPublishDate,
                    limit = limit,
                    categoryId = categoryId
                )

                lastResultEndOfPaginationReached = audiosFromServer.size < limit

                if (audiosFromServer.isEmpty()) break

                val (deletedItems, activeItems) = audiosFromServer.partition { it.isDeleted }

                appDatabase.withTransaction {
                    deletedItems.forEach { 
                        audioDao.deleteById(it.id)
                    }

                    if (activeItems.isNotEmpty()) {
                        audioDao.upsertKeepingUserData(activeItems.map { it.toEntity() })
                    }
                }

                // The list shows standalone audios only (a series is one card above it), so a
                // page of series and deleted audios adds nothing to it and the next load's
                // cursor (the list's last item) would not move. Keep going from the last
                // document fetched, in bigger pages: a series can fill many pages in a row.
                if (activeItems.any { it.seriesId == null } || lastResultEndOfPaginationReached) {
                    break
                }
                currentLastPublishDate = audiosFromServer.last().publishDate?.toDate()?.time
                limit = (limit * 2).coerceAtMost(MAX_SKIP_PAGE_SIZE)
            }

            MediatorResult.Success(endOfPaginationReached = lastResultEndOfPaginationReached)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
