package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.VideoDto
import com.example.domain.module.Video
import com.example.domain.module.toIsoString
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Handles all interactions with Firestore for the Video feature.
 */
class VideoFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseFunctions: FirebaseFunctions
) {
    private val TAG = "VideoFirestoreSource"
    private val videosCollection = firestore.collection("videos")

    private fun DocumentSnapshot.toVideoDtoSafe(): VideoDto? {
        return try {
            this.toObject<VideoDto>()?.copy(id = this.id)
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

                VideoDto(
                    isDeleted = this.getBoolean("isDeleted") ?: this.getBoolean("deleted") ?: false,
                    id = this.id,
                    title = this.getString("title") ?: "",
                    videoUrl = this.getString("videoUrl") ?: "",
                    videoYoutubeId = this.getString("videoYoutubeId") ?: "",
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

    suspend fun fetchVideoPage(startAfterPublishDate: Long?, limit: Int): List<VideoDto> {
        try {
            var query = videosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)

            if (startAfterPublishDate != null) {
                query = query.startAfter(Timestamp(Date(startAfterPublishDate)))
            }

            val snapshot = query.limit(limit.toLong()).get().await()

            return snapshot.documents.mapNotNull { it.toVideoDtoSafe() }
        } catch (e: Exception) {
            Log.e(TAG, "fetchVideoPage failed: ${e.message}")
            return emptyList()
        }
    }

    suspend fun getUpdatedVideos(lastSyncTime: Long): List<VideoDto> {
        return try {
            val snapshot = videosCollection
                .whereGreaterThan("updatedAt", Timestamp(Date(lastSyncTime)))
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toVideoDtoSafe() }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdatedVideos failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getVideoById(videoId: String): Video? {
        return try {
            val doc = videosCollection.document(videoId).get().await()
            val dto = doc.toVideoDtoSafe()
            dto?.let {
                Video(
                    id = doc.id,
                    title = it.title,
                    videoUrl = it.videoUrl,
                    publishDate = it.publishDate?.toDate() ?: Date(),
                    youtubeVideoId = it.videoYoutubeId,
                    type = it.type
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getVideoById failed", e)
            null
        }
    }

    fun uploadVideo(
        title: String,
        videoUrl: String,
        publishDate: Long = System.currentTimeMillis(),
    ): Flow<UploadResult> = callbackFlow {
        trySend(UploadResult.Progress(0))

        val youtubeId = getYoutubeVideoId(videoUrl)
        if (youtubeId == null) {
            trySend(UploadResult.Error("Invalid YouTube URL."))
            close(); return@callbackFlow
        }

        launch {
            try {
                val publishDateIso = Date(publishDate).toIsoString()
                val contentMap = hashMapOf(
                    "title" to title,
                    "videoUrl" to videoUrl,
                    "videoYoutubeId" to youtubeId,
                    "publishDate" to publishDateIso,
                )

                val payload = hashMapOf(
                    "collectionName" to "videos",
                    "contentData" to contentMap
                )

                firebaseFunctions
                    .getHttpsCallable("uploadContent")
                    .call(payload)
                    .await()

                trySend(UploadResult.Progress(100))
                trySend(UploadResult.Success)
                close()
            } catch (e: Exception) {
                trySend(UploadResult.Error("Database Error: ${e.message}"))
                close()
            }
        }
        awaitClose { }
    }

    fun updateVideo(
        id: String,
        title: String,
        videoUrl: String,
    ): Flow<UploadResult> = callbackFlow {
        trySend(UploadResult.Progress(0))

        val youtubeId = getYoutubeVideoId(videoUrl)
        if (youtubeId == null) {
            trySend(UploadResult.Error("Invalid YouTube URL."))
            close(); return@callbackFlow
        }

        launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "title" to title,
                    "videoUrl" to videoUrl,
                    "videoYoutubeId" to youtubeId
                )

                val payload = hashMapOf(
                    "collectionName" to "videos",
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
        awaitClose { }
    }

    suspend fun deleteVideo(videoId: String): Result<Unit> {
        return try {
            val payload = hashMapOf(
                "collectionName" to "videos",
                "documentId" to videoId
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

    private fun getYoutubeVideoId(url: String): String? {
        val pattern =
            "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val matcher = java.util.regex.Pattern.compile(pattern).matcher(url)
        return if (matcher.find()) matcher.group() else null
    }

    fun syncVideosDbWithServer(): Flow<List<VideoDto>> {
        return callbackFlow {
            val listenerRegistration = videosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen failed: ${error.message}", error)
                        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            close()
                        } else {
                            trySend(emptyList())
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        trySend(snapshot.documents.mapNotNull { it.toVideoDtoSafe() })
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }
}
