package com.example.data_firebase

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.AudioDto
import com.example.domain.module.Audio
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject


class FirebaseAudioSource @Inject constructor(
    realTimeDb: FirebaseDatabase,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {


    private val audiosRef = realTimeDb.getReference("audios")
    private val storageRef = storage.getReference("audios")

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


    /**
     * Uploads an audio file to Firebase Storage and its metadata to the Realtime Database,
     * providing progress updates via a Flow. This is the best practice approach.
     *
     * @param title The title of the audio.
     * @param uriString The String representation of the audio file's content URI.
     * @param durationInMillis The duration of the audio in milliseconds.
     * @return A Flow that emits UploadResult states (Progress, Success, or Error).
     */
    fun uploadAudio(
        title: String,
        uriString: String,
        durationInMillis: Long
    ): Flow<UploadResult> = callbackFlow {
        val uri = uriString.toUri()
        val fileName = "audios/${System.currentTimeMillis()}"
        // Note: The storageRef was being initialized at the class level before,
        // which is not ideal for dynamic file names. This is better.
        val specificFileStorageRef = storage.reference.child(fileName)

        // Open an InputStream from the URI. This is memory-efficient.
        val uploadTask = specificFileStorageRef.putFile(uri)

        trySend(UploadResult.Progress(0))
        uploadTask.addOnProgressListener { taskSnapshot ->
            val bytesTransferred = taskSnapshot.bytesTransferred
            val totalByteCount = taskSnapshot.totalByteCount
            Log.d(TAG, "uploadAudio: bytes trans: $bytesTransferred")
            Log.d(TAG, "uploadAudio: total byte: $totalByteCount")
             if (totalByteCount > 0) {
                val progress = ((bytesTransferred.toDouble() * 100) / totalByteCount).toInt()
                Log.d(TAG, "Upload progress: $progress% ($bytesTransferred / $totalByteCount)")
                trySend(UploadResult.Progress(progress))
            }
        }
            .addOnSuccessListener {
                // After upload is successful, get the download URL
                it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Now that we have the URL, create the metadata object
                    val audioDto = AudioDto(
                        title = title,
                        audioUrl = downloadUri.toString(),
                        publishDate = System.currentTimeMillis(),
                        durationInMillis = durationInMillis
                    )

                    // Save the metadata to the Realtime Database
                    audiosRef.push().setValue(audioDto)
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully uploaded audio and metadata for: $title")
                            trySend(UploadResult.Success) // Emit final success
                            close() // Close the flow successfully
                        }
                        .addOnFailureListener { dbError ->
                            Log.e(TAG, "Failed to save metadata to Realtime DB", dbError)
                            trySend(UploadResult.Error("Failed to save metadata: ${dbError.message}"))
                            close()
                        }
                }.addOnFailureListener { urlError ->
                    Log.e(TAG, "Failed to get download URL after upload", urlError)
                    trySend(UploadResult.Error("Failed to get download URL: ${urlError.message}"))
                    close()
                }
            }
            .addOnFailureListener { uploadError ->
                Log.e(TAG, "Firebase Storage upload failed", uploadError)
                trySend(UploadResult.Error("Upload failed: ${uploadError.message}"))
                close()
            }

        // This ensures the upload is cancelled if the collecting coroutine is cancelled by the user.
        awaitClose {
            uploadTask.cancel()
        }
    }


    suspend fun getAllAudiosFromRealTimeDb(): List<Audio> {
        return emptyList()
    }
}