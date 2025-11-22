package com.example.data_firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.data_firebase.model.AudioDto
import com.example.domain.module.Audio
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject


class FirebaseAudioSource @Inject constructor(
    realTimeDb: FirebaseDatabase
) {



    private val audiosRef = realTimeDb.getReference("audios")

    /**
     * Fetches a single page of audios for pagination.
     * @param startAfterKey The key of the last item from the previous page. If null, fetches the first page.
     * @param limit The maximum number of items to fetch for the page.
     * @return A list of Audio domain models for the requested page.
     */
    suspend fun fetchAudioPage(startAfterKey: String?, limit: Int): List<Audio> {
        // Start building the query, ordering by key is essential for stable pagination.
        val query = audiosRef.orderByKey()

        Log.d(TAG, "fetchAudioPage: called from firebase")
        val finalQuery = if (startAfterKey == null) {
            // This is the first page load (or a refresh).
            // Fetch the first `limit` items.
            query.limitToFirst(limit)
        } else {
            // This is for subsequent pages (APPEND).
            // Fetch `limit` items starting after the last known key.
            // RealtimeDB's startAfter() includes the key, so we fetch limit + 1 and drop the first.
            query.startAfter(startAfterKey).limitToFirst(limit)
        }

        return try {
            val dataSnapshot = finalQuery.get().await()
            if (!dataSnapshot.exists()) {
                return emptyList()
            }

            // Map the snapshot children to Audio domain model.
            dataSnapshot.children.mapNotNull { snapshot ->
                snapshot.toAudioDomainModel()
            }
        } catch (e: Exception) {
            // For now, return an empty list to prevent crashes.
            Log.d(TAG, "fetchAudioPage: ${e.message}")
            emptyList()
        }
    }

    /**
     * Helper function to map a Firebase DataSnapshot to the app's Audio domain model.
     * This keeps the mapping logic clean and reusable.
     */
    private fun DataSnapshot.toAudioDomainModel(): Audio? {
        val firebaseDto = this.getValue(AudioDto::class.java)

        return firebaseDto?.let { dto ->
            Audio(
                id = this.key ?: return null, // The node's key is the unique ID.
                title = dto.title,
                audioUrl = dto.audioUrl,
                publishDate = Date(dto.publishDate),
                durationInMillis = dto.durationInMillis,

                // Default values for non-server fields (user state).
                // The RemoteMediator will later merge these with actual local data.
                isFavorite = false,
                isDownloaded = false,
                lastPlayedTimestamp = null,
                isPlaying = false,
                localFilePath = null
            )
        }
    }



    suspend fun getAllAudiosFromRealTimeDb(): List<Audio> {
        return emptyList()
    }
}