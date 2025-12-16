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
import com.example.data_local.model.AudioEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)

class AudioRemoteMediator @Inject  constructor(
    private val appDatabase: AppDatabase,
    private val firebaseMediaSource: FirebaseMediaSource,
    private val networkStatusUseCase: GetCurrentNetworkStatusUseCase
): RemoteMediator<Int, AudioEntity>() {

    private val audioDao = appDatabase.audioDao()

    private val TAG = "AudioRemoteMediator"

    override suspend fun initialize(): InitializeAction {
        //
        // This is the key. On first launch, check if we have data.
        // If we do, don't launch a remote refresh. Show cache first.
        // If the database is empty, then launch a remote refresh.

        return if (audioDao.count() > 0) {
            Log.d(TAG, "DB has data. Skipping remote refresh on launch.")
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            Log.d(TAG, "DB is empty. Launching remote refresh on launch.")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }




    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AudioEntity>
    ): MediatorResult {
        Log.d("Ali 1712", "load: load type: $loadType")
        return try {


            //  Determine the key of the last item to start fetching from.
            val lastItemKey = when (loadType) {
                LoadType.REFRESH -> {
                    // For a refresh, we start from the beginning. No key needed.
                    null
                }
                LoadType.PREPEND -> {
                    // We don't support loading data backward in this model.
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // For appending, get the last item loaded from the PagingState.
                    // Its ID (which is the Firebase key) will be our cursor.
                    if (networkStatusUseCase().first() == NetworkStatus.Unavailable) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    lastItem.id
                }
            }


            //  Fetch a page of audios from Firebase Realtime Database.
            val audiosFromServer = firebaseMediaSource.fetchAudioPage(
                startAfterKey = lastItemKey,
                limit = state.config.pageSize
            )

            val endOfPaginationReached = audiosFromServer.isEmpty()
            // The critical "Read-Merge-Write" transaction to preserve user data.
            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // On refresh, clear previous data.
                    appDatabase.audioDao().clearAll()
                }

                // Get the IDs of the audios we just fetched from the server.
                val serverAudioIds = audiosFromServer.map { it.id }
                // Read the existing local audios that match these IDs.
                val localAudiosMap = appDatabase.audioDao().getAudiosByIds(serverAudioIds).associateBy { it.id }

                // Merge server data with local user data.
                val mergedEntities = audiosFromServer.map { serverAudio ->
                    val localAudio = localAudiosMap[serverAudio.id]

                    val serverEntity = serverAudio.toEntity() // Convert server model to a base entity.

                    if (localAudio != null) {
                        // This audio already exists locally.
                        // Preserve user-specific data (`isFavorite`, `lastPlayedTimestamp`, etc.)
                        // by copying it over the fresh server data.
                        serverEntity.copy(
                            isFavorite = localAudio.isFavorite,
                            lastPlayedTimestamp = localAudio.lastPlayedTimestamp,
                            localFilePath = localAudio.localFilePath,

                        )
                    } else {
                        // This is a brand new audio not seen before.
                        // The server entity with its default user-state values is fine.
                        serverEntity
                    }
                }

                // Insert the final, merged list into the database.
                appDatabase.audioDao().upsertAll(mergedEntities)
            }

            // Return the result. The end of pagination is reached if the fetch returned fewer items than requested.
            MediatorResult.Success(
                endOfPaginationReached = endOfPaginationReached
            )

        } catch (e: IOException) {
            Log.d("TAG", "load: Ali 1712 ${e.message}")
            MediatorResult.Error(e)
        } catch (e: Exception) {
            Log.d("TAG", "load: Ali 1712 ${e.message}")
            // Catches other exceptions, including potential Firebase errors.
            MediatorResult.Error(e)
        }
    }
}