package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.AudioDto
import com.example.domain.module.Audio
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Handles all interactions with Firestore and Cloud Storage for the Audio feature.
 */
class AudioFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val TAG = "AudioFirestoreSource"
    private val audiosCollection = firestore.collection("audios")


    /**
     * Fetches a paginated list of audios from Firestore.
     */
    suspend fun fetchAudioPage(startAfterPublishDate: Long?, limit: Int): List<AudioDto> {
        try {
            // Removed isDeleted filter to allow client to sync deletions
            var query = audiosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)

            if (startAfterPublishDate != null) {
                query = query.startAfter(Timestamp(Date(startAfterPublishDate)))
            }

            val snapshot = query.limit(limit.toLong()).get().await()

            return snapshot.documents.mapNotNull { it.toObject<AudioDto>() }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAudioPage failed: ${e.message}")
            return emptyList()
        }
    }

    suspend fun getUpdatedAudios(lastSyncTime: Long): List<AudioDto> {
        return try {
            val snapshot = audiosCollection
                .whereGreaterThan("updatedAt", Timestamp(Date(lastSyncTime)))
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<AudioDto>() }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdatedAudios failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAudioById(audioId: String): Audio? {
        return try {
            val doc = audiosCollection.document(audioId).get().await()
            val dto = doc.toObject<AudioDto>()
            dto?.let {
                Audio(
                    id = doc.id,
                    title = it.title,
                    audioUrl = it.audioUrl,
                    publishDate = it.publishDate?.toDate() ?: Date(),
                    durationInMillis = it.durationInMillis,
                    isFavorite = false,
                    isDownloaded = false,
                    lastPlayedTimestamp = null,
                    isPlaying = false,
                    localFilePath = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAudioById failed", e)
            null
        }
    }

    /**
     * Uploads an audio file and its metadata to Firebase.
     */
    fun uploadAudio(
        title: String,
        uriString: String,
        durationInMillis: Long
    ): Flow<UploadResult> = callbackFlow {
        trySend(UploadResult.Progress(0))

        val uri = uriString.toUri()
        val fileName = "audios/${System.currentTimeMillis()}"
        val storageRef = storage.reference.child(fileName)
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress =
                ((100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
            trySend(UploadResult.Progress(progress))
        }.addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                val now = Timestamp.now()
                val audioDto = mapOf(
                    "id" to "", // Set later
                    "title" to title,
                    "audioUrl" to downloadUri.toString(),
                    "publishDate" to now,
                    "durationInMillis" to durationInMillis,
                    "updatedAt" to now,
                    "isDeleted" to false
                )
                audiosCollection.add(audioDto)
                    .addOnSuccessListener { docRef ->
                        docRef.update("id", docRef.id)
                        Log.d(TAG, "Successfully uploaded audio and metadata for: $title")
                        trySend(UploadResult.Success)
                        close()
                    }.addOnFailureListener { dbError ->
                        trySend(UploadResult.Error("Failed to save metadata: ${dbError.message}"))
                        close()
                    }
            }.addOnFailureListener { urlError ->
                trySend(UploadResult.Error("Failed to get download URL: ${urlError.message}"))
                close()
            }
        }.addOnFailureListener { uploadError ->
            trySend(UploadResult.Error("Upload failed: ${uploadError.message}"))
            close()
        }
        awaitClose { uploadTask.cancel() }
    }

    fun updateAudio(
        id: String,
        title: String,
        newUriString: String?,
        existingUrl: String,
        durationInMillis: Long
    ): Flow<UploadResult> = callbackFlow {
        val now = Timestamp.now()
        if (newUriString == null) {
            // Only update metadata
            try {
                audiosCollection.document(id).update(
                    mapOf(
                        "title" to title,
                        "durationInMillis" to durationInMillis,
                        "updatedAt" to now
                    )
                ).await()
                trySend(UploadResult.Success)
                close()
            } catch (e: Exception) {
                trySend(UploadResult.Error(e.message ?: "Update failed"))
                close()
            }
        } else {
            // Upload new file and update metadata
            trySend(UploadResult.Progress(0))
            val uri = newUriString.toUri()
            val fileName = "audios/${System.currentTimeMillis()}"
            val storageRef = storage.reference.child(fileName)
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = ((100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                trySend(UploadResult.Progress(progress))
            }.addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val updates = mapOf(
                        "title" to title,
                        "audioUrl" to downloadUri.toString(),
                        "durationInMillis" to durationInMillis,
                        "updatedAt" to now
                    )
                    audiosCollection.document(id).update(updates)
                        .addOnSuccessListener {
                            // Try to delete old file
                            try {
                                storage.getReferenceFromUrl(existingUrl).delete()
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to delete old audio file", e)
                            }
                            trySend(UploadResult.Success)
                            close()
                        }.addOnFailureListener { dbError ->
                            trySend(UploadResult.Error(dbError.message ?: "Update failed"))
                            close()
                        }
                }
            }.addOnFailureListener { e ->
                trySend(UploadResult.Error(e.message ?: "Upload failed"))
                close()
            }
            awaitClose { uploadTask.cancel() }
        }
    }

    suspend fun deleteAudio(audioId: String, audioUrl: String): Result<Unit> {
        return try {
            val now = Timestamp.now()
            audiosCollection.document(audioId).update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to now
                )
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun syncAudiosDbWithServer(): Flow<List<AudioDto>> {
        return callbackFlow {
            val listenerRegistration = audiosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen failed: ${error.message}", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        trySend(snapshot.toObjects(AudioDto::class.java))
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }
}
