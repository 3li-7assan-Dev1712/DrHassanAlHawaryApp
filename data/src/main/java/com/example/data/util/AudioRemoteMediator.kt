package com.example.data.util

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.FirebaseAudioSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.AudioEntity
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)

class AudioRemoteMediator @Inject  constructor(
    private val appDatabase: AppDatabase,
    private val firebaseAudioSource: FirebaseAudioSource
): RemoteMediator<Int, AudioEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AudioEntity>
    ): MediatorResult {
        return try {
            // 1. Determine the key of the last item to start fetching from.
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
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    lastItem.id
                }
            }

            // 2. Fetch a page of audios from Firebase Realtime Database.
            val audiosFromServer = firebaseAudioSource.fetchAudioPage(
                startAfterKey = lastItemKey,
                limit = state.config.pageSize
            )
            // 3. The critical "Read-Merge-Write" transaction to preserve user data.
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

            // 4. Return the result. The end of pagination is reached if the fetch returned fewer items than requested.
            MediatorResult.Success(
                endOfPaginationReached = audiosFromServer.size < state.config.pageSize
            )

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            // Catches other exceptions, including potential Firebase errors.
            MediatorResult.Error(e)
        }
    }
}