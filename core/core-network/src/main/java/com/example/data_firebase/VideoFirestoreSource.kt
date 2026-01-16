package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.VideoDto
import com.example.domain.module.Video
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Handles all interactions with Firestore for the Video feature.
 */
class VideoFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "VideoFirestoreSource"
    private val videosCollection = firestore.collection("videos")


    suspend fun fetchVideoPage(startAfterKey: String?, limit: Int): List<Video> {
        try {
            var query = videosCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)

            if (startAfterKey != null) {
                val lastVisibleDoc = videosCollection.document(startAfterKey).get().await()
                query = query.startAfter(lastVisibleDoc)
            }

            val snapshot = query.limit(limit.toLong()).get().await()

            return snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<VideoDto>()
                dto?.let {
                    Video(
                        id = document.id,
                        title = it.title,
                        videoUrl = it.videoUrl,
                        publishDate = Date(it.publishDate),
                        youtubeVideoId = it.videoYoutubeId
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchVideoPage failed: ${e.message}")
            return emptyList()
        }
    }

    fun uploadVideo(
        title: String,
        videoUrl: String,
        publishDate: Long = System.currentTimeMillis()
    ): Flow<UploadResult> = callbackFlow {
        trySend(UploadResult.Progress(0))

        val youtubeId = getYoutubeVideoId(videoUrl)
        if (youtubeId == null) {
            trySend(UploadResult.Error("Invalid YouTube URL."))
            close(); return@callbackFlow
        }

        trySend(UploadResult.Progress(50)) // Indicate processing
        val videoDto = VideoDto(
            title = title,
            videoUrl = videoUrl,
            videoYoutubeId = youtubeId,
            publishDate = publishDate
        )

        videosCollection.add(videoDto)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded video metadata: $title")
                trySend(UploadResult.Progress(100))
                trySend(UploadResult.Success)
                close()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to upload video metadata", e)
                trySend(UploadResult.Error("Database Error: ${e.message}"))
                close()
            }
        awaitClose { }
    }

    private fun getYoutubeVideoId(url: String): String? {
        val pattern =
            "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val matcher = java.util.regex.Pattern.compile(pattern).matcher(url)
        return if (matcher.find()) matcher.group() else null
    }
}