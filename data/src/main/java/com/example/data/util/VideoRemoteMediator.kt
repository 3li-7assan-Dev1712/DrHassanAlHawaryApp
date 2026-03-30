package com.example.data.util


import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.VideoFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.VideoEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class VideoRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val videoFirestoreSource: VideoFirestoreSource,
    private val networkStatusUseCase: GetCurrentNetworkStatusUseCase,
) : RemoteMediator<Int, VideoEntity>() {

    private val videoDao = appDatabase.videoDao()
    private val TAG = "VideoRemoteMediator"


    override suspend fun initialize(): InitializeAction {
        /*return if (videoDao.count() > 0) {
            Log.d(TAG, "DB has data. Skipping remote refresh on launch.")
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            Log.d(TAG, "DB is empty. Launching remote refresh on launch.")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }*/
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, VideoEntity>
    ): MediatorResult {
        return try {
            val lastItemPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // Logic to get the last item for pagination key
                    val lastLocalItem = videoDao.getLastVideo()

                    // Handle Offline/Flag Logic
                    if (networkStatusUseCase().first() == NetworkStatus.Unavailable) {
                        if (lastLocalItem == null) {
                            Log.d(TAG, "Offline/Flag True: Local data exhausted.")
                            return MediatorResult.Success(endOfPaginationReached = true)
                        } else {
                            Log.d(TAG, "Offline/Flag True: Letting Room continue paging.")
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }

                    // Online: return the publishDate of the last item to start fetching after it
                    lastLocalItem?.publishDate
                }
            }

            // Fetch from Firebase
            val videosFromServer = videoFirestoreSource.fetchVideoPage(
                startAfterPublishDate = lastItemPublishDate,
                limit = state.config.pageSize
            )

            val endOfPaginationReached = videosFromServer.size < state.config.pageSize

            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    videoDao.clearAll()
                }

                val entities = videosFromServer.map { it.toEntity() }

                videoDao.upsertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
