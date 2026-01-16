package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.AudioDto
import com.example.domain.module.Audio
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
    suspend fun fetchAudioPage(startAfterKey: String?, limit: Int): List<Audio> {
        Log.d(TAG, "fetchAudioPage: called from firestore")
        try {
            var query = audiosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)

            if (startAfterKey != null) {
                val lastVisibleDoc = audiosCollection.document(startAfterKey).get().await()
                query = query.startAfter(lastVisibleDoc)
            }

            val snapshot = query.limit(limit.toLong()).get().await()

            return snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<AudioDto>()
                dto?.let {
                    Audio(
                        id = document.id,
                        title = it.title,
                        audioUrl = it.audioUrl,
                        publishDate = java.util.Date(it.publishDate),
                        durationInMillis = it.durationInMillis,
                        // Local-only fields are defaulted
                        isFavorite = false,
                        isDownloaded = false,
                        lastPlayedTimestamp = null,
                        isPlaying = false,
                        localFilePath = null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAudioPage failed: ${e.message}")
            return emptyList()
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
                val audioDto = AudioDto(
                    title = title,
                    audioUrl = downloadUri.toString(),
                    publishDate = System.currentTimeMillis(),
                    durationInMillis = durationInMillis
                )
                audiosCollection.add(audioDto)
                    .addOnSuccessListener {
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
}