package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.AudioDto
import com.example.domain.module.Audio
import com.example.domain.module.toIsoString
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Handles all interactions with Firestore and Cloud Storage for the Audio feature.
 */
class AudioFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseFunctions: FirebaseFunctions
) {

    private val TAG = "AudioFirestoreSource"
    private val audiosCollection = firestore.collection("audios")

    private fun DocumentSnapshot.toAudioDtoSafe(): AudioDto? {
        return try {
            this.toObject<AudioDto>()?.copy(id = this.id)
        } catch (e: Exception) {
            Log.w(TAG, "Standard deserialization failed for ${this.id}, trying manual mapping", e)
            try {
                val publishDate = try {
                    this.getTimestamp("publishDate")
                } catch (ex: Exception) {
                    this.getLong("publishDate")?.let { Timestamp(Date(it)) }
                }
                
                val updatedAt = try {
                    this.getTimestamp("updatedAt")
                } catch (ex: Exception) {
                    this.getLong("updatedAt")?.let { Timestamp(Date(it)) }
                }

                AudioDto(
                    isDeleted = this.getBoolean("isDeleted") ?: this.getBoolean("deleted") ?: false,
                    id = this.id,
                    title = this.getString("title") ?: "",
                    audioUrl = this.getString("audioUrl") ?: "",
                    durationInMillis = this.getLong("durationInMillis") ?: 0L,
                    publishDate = publishDate,
                    updatedAt = updatedAt,
                    type = this.getString("type") ?: ""
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Manual mapping failed for ${this.id}", ex)
                null
            }
        }
    }

    /**
     * Fetches a paginated list of audios from Firestore.
     */
    suspend fun fetchAudioPage(startAfterPublishDate: Long?, limit: Int): List<AudioDto> {
        try {
            var query = audiosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)

            if (startAfterPublishDate != null) {
                query = query.startAfter(Timestamp(Date(startAfterPublishDate)))
            }

            val snapshot = query.limit(limit.toLong()).get().await()

            return snapshot.documents.mapNotNull { it.toAudioDtoSafe() }
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
            snapshot.documents.mapNotNull { it.toAudioDtoSafe() }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdatedAudios failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAudioById(audioId: String): Audio? {
        return try {
            val doc = audiosCollection.document(audioId).get().await()
            val dto = doc.toAudioDtoSafe()
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
                    localFilePath = null,
                    type = it.type
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
        durationInMillis: Long,
        type: String = ""
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
        }.addOnSuccessListener { taskSnapshot ->
            launch {
                try {
                    val downloadUri = taskSnapshot.storage.downloadUrl.await()
                    val publishDateIso = Date().toIsoString()

                    val contentMap = hashMapOf(
                        "title" to title,
                        "audioUrl" to downloadUri.toString(),
                        "durationInMillis" to durationInMillis,
                        "publishDate" to publishDateIso,
                    )

                    val payload = hashMapOf(
                        "collectionName" to "audios",
                        "contentData" to contentMap
                    )

                    firebaseFunctions
                        .getHttpsCallable("uploadContent")
                        .call(payload)
                        .await()

                    Log.d(TAG, "Successfully uploaded audio and metadata for: $title")
                    trySend(UploadResult.Success)
                    close()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save metadata via Cloud Function", e)
                    trySend(UploadResult.Error("Failed to save metadata: ${e.message}"))
                    close()
                }
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
        durationInMillis: Long,
        type: String? = null
    ): Flow<UploadResult> = callbackFlow {
        if (newUriString == null) {
            // Only update metadata
            launch {
                try {
                    val updates = hashMapOf<String, Any>(
                        "title" to title,
                        "durationInMillis" to durationInMillis
                    )
                    type?.let { updates["type"] = it }

                    val payload = hashMapOf(
                        "collectionName" to "audios",
                        "documentId" to id,
                        "updates" to updates
                    )

                    firebaseFunctions
                        .getHttpsCallable("updateContent")
                        .call(payload)
                        .await()

                    trySend(UploadResult.Success)
                    close()
                } catch (e: Exception) {
                    trySend(UploadResult.Error(e.message ?: "Update failed"))
                    close()
                }
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
            }.addOnSuccessListener { taskSnapshot ->
                launch {
                    try {
                        val downloadUri = taskSnapshot.storage.downloadUrl.await()
                        val updates = hashMapOf<String, Any>(
                            "title" to title,
                            "audioUrl" to downloadUri.toString(),
                            "durationInMillis" to durationInMillis
                        )
                        type?.let { updates["type"] = it }

                        val payload = hashMapOf(
                            "collectionName" to "audios",
                            "documentId" to id,
                            "updates" to updates
                        )

                        firebaseFunctions
                            .getHttpsCallable("updateContent")
                            .call(payload)
                            .await()

                        // Try to delete old file
                        try {
                            storage.getReferenceFromUrl(existingUrl).delete().await()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to delete old audio file", e)
                        }
                        trySend(UploadResult.Success)
                        close()
                    } catch (e: Exception) {
                        trySend(UploadResult.Error(e.message ?: "Update failed"))
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
            val payload = hashMapOf(
                "collectionName" to "audios",
                "documentId" to audioId
            )

            firebaseFunctions
                .getHttpsCallable("deleteContent")
                .call(payload)
                .await()
            
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
                        trySend(snapshot.documents.mapNotNull { it.toAudioDtoSafe() })
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }
}
