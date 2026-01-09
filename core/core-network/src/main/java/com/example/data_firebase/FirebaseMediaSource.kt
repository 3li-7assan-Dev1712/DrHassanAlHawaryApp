package com.example.data_firebase

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.AudioDto
import com.example.domain.module.Audio
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup
import com.example.domain.module.Video
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject


/**
 * This class is help working with the remote database (firebase firestore) by providing
 * primary function like uploadAudioFile (used by admin app) or download audioFile (used by users app)
 *
 * @param realTimeDb the firebase real time database to store audios metadata like duration and file name
 * @param storage Firebase Storge to store media like audios, videos and images
 *
 */
class FirebaseMediaSource @Inject constructor(
    realTimeDb: FirebaseDatabase,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {


    private val TAG = "FirebaseMediaSource"
    private val audiosRef = realTimeDb.getReference("audios")
    val imagesRef = realTimeDb.getReference("images")
    private val videosRef = realTimeDb.getReference("videos")


    suspend fun fetchVideoPage(startAfterKey: String?, limit: Int): List<Video> {
        val query = videosRef.orderByKey()

        val finalQuery = if (startAfterKey == null) {
            query.limitToFirst(limit)
        } else {
            query.startAfter(startAfterKey).limitToFirst(limit)
        }

        return try {
            val dataSnapshot = finalQuery.get().await()
            dataSnapshot.children.mapNotNull { snapshot ->
                val title = snapshot.child("title").getValue(String::class.java) ?: ""
                val url = snapshot.child("videoUrl").getValue(String::class.java) ?: ""
                val timestamp = snapshot.child("publishDate").getValue(Long::class.java) ?: 0L
                val videoId = snapshot.child("videoYoutubeId").getValue(String::class.java) ?: ""


                Video(
                    id = snapshot.key ?: "",
                    title = title,
                    videoUrl = url,
                    publishDate = (Date(timestamp)),
                    youtubeVideoId = videoId
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getYoutubeVideoId(url: String): String? {
        val pattern =
            "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = java.util.regex.Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group() else null
    }


    fun uploadVideo(
        title: String,
        videoUrl: String,
        publishDate: Long = System.currentTimeMillis()
    ): Flow<UploadResult> = callbackFlow {

        trySend(UploadResult.Progress(0))

        val youtubeId = getYoutubeVideoId(videoUrl)
        if (youtubeId == null) {
            trySend(UploadResult.Error("Invalid YouTube URL. Please check the link."))
            close()
            return@callbackFlow
        }

        // Create the data map to send to Firebase
        val videoData = hashMapOf(
            "title" to title,
            "videoUrl" to videoUrl,
            "videoYoutubeId" to youtubeId,
            "publishDate" to publishDate
        )

        // Push to "videos" node
        videosRef.push().setValue(videoData)
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

        awaitClose { /* No task to cancel as this is a simple DB write */ }
    }


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


    /**
     * Uploads a new design group to Firebase, providing progress updates.
     * 1. Uploads multiple images to Firebase Storage.
     * 2. Saves metadata to Realtime Database upon completion.
     * @return A Flow that emits UploadResult states (Progress, Success, or Error).
     */
    fun uploadImageGroup(title: String, imageUris: List<String>): Flow<UploadResult> =
        callbackFlow {
            // --- 1. Preparation ---
            val newImageRef = imagesRef.push()
            val categoryId = newImageRef.key
            if (categoryId == null) {
                trySend(UploadResult.Error("Failed to generate a key from Realtime Database."))
                close()
                return@callbackFlow
            }

            // --- 2. Create and Monitor Upload Tasks ---
            val uploadTasks = imageUris.mapIndexed { index, uriString ->
                val uri = uriString.toUri()
                val imageRef = storage.reference.child("images/$categoryId/image_$index.jpg")
                imageRef.putFile(uri)
            }

            var totalBytes = 0L
            var bytesTransferred = 0L

            // Attach listeners to each task to aggregate progress
            uploadTasks.forEach { task ->
                task.addOnProgressListener { snapshot ->
                    // This listener gets called for each individual file's progress
                    // To get total progress, you need to know the total size of all files first.
                    // For simplicity and immediate feedback, we can average the progress.
                    // A more accurate way would be to sum total bytes of all files.
                }
            }

            // A simpler way to show overall progress: update after each file completes.
            val uploadedImageUrls = mutableListOf<String>()
            trySend(UploadResult.Progress(0))

            uploadTasks.forEachIndexed { index, task ->
                try {
                    // Wait for the current file to finish uploading
                    val snapshot = task.await()
                    val downloadUrl = snapshot.storage.downloadUrl.await().toString()
                    uploadedImageUrls.add(downloadUrl)

                    // Calculate and emit progress based on how many files are done
                    val progress = (((index + 1).toFloat() / imageUris.size) * 100).toInt()
                    trySend(UploadResult.Progress(progress))

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload image #${index + 1}", e)
                    trySend(UploadResult.Error("Failed to upload image #${index + 1}: ${e.message}"))
                    close() // Abort on the first failure
                    return@callbackFlow
                }
            }

            // --- 3. Save Metadata after all uploads are successful ---
            try {
                val designGroup = hashMapOf(
                    "id" to categoryId,
                    "title" to title,
                    "publishDate" to System.currentTimeMillis(),
                    "previewImageUrl" to uploadedImageUrls.firstOrNull()
                )
                newImageRef.setValue(designGroup).await()

                val imagesMap = uploadedImageUrls.mapIndexed { index, url ->
                    mapOf(
                        "imageUrl" to url,
                        "orderIndex" to index
                    )
                }
                newImageRef.child("images").setValue(imagesMap).await()

                trySend(UploadResult.Success)
                close() // Successfully completed and closed the flow

            } catch (e: Exception) {
                Log.e(TAG, "Failed to save metadata to Realtime DB after upload", e)
                trySend(UploadResult.Error("Failed to save metadata: ${e.message}"))
                close()
            }

            awaitClose {
                // Cancel any ongoing uploads if the collector cancels the flow
                uploadTasks.filter { it.isInProgress }.forEach { it.cancel() }
            }
        }


    /**
     * Fetches a single page of image groups from Firebase Realtime Database.
     *
     * @param startAfterKey The key of the last item from the previous page. Null for the first page.
     * @param limit The maximum number of items to fetch for this page.
     * @return A list of ImageGroup objects for the requested page.
     */
    suspend fun fetchImageGroupsPage(startAfterKey: String?, limit: Int): List<ImageGroup> {

        // Order by key is essential for reliable pagination
        val query = imagesRef.orderByKey()

        val finalQuery = if (startAfterKey == null) {
            // First page: Start from the beginning and get the first 'limit' items
            query.limitToFirst(limit)
        } else {
            // Subsequent pages: Start after the last key and get the next 'limit' items
            query.startAfter(startAfterKey).limitToFirst(limit)
        }

        return try {
            val dataSnapshot = finalQuery.get().await()
            if (!dataSnapshot.exists()) {
                return emptyList()
            }

            dataSnapshot.children.mapNotNull { snapshot ->
                // Use your existing logic to map the snapshot to an ImageGroup
                val groupData = snapshot.getValue<Map<String, Any>>()
                if (groupData != null) {
                    ImageGroup(
                        id = groupData["id"] as String,
                        title = groupData["title"] as String,
                        publishDate = Date(groupData["publishDate"] as Long),
                        previewImageUrl = groupData["previewImageUrl"] as String
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseMediaSource", "Failed to fetch image groups page", e)
            emptyList()
        }
    }


    /**
     * Fetches all design groups from the Realtime Database.
     * @return A list of ImageGroup domain models.
     */
    suspend fun fetchImageGroups(): List<ImageGroup> {
        return try {
            // Get all data under the "designs" node.
            val dataSnapshot = imagesRef.get().await()
            if (!dataSnapshot.exists()) {
                return emptyList()
            }

            // Map each child node to an ImageGroup domain model.
            dataSnapshot.children.mapNotNull { snapshot ->
                val groupData = snapshot.getValue<Map<String, Any>>()
                if (groupData != null) {
                    ImageGroup(
                        id = groupData["id"] as? String ?: snapshot.key ?: "",
                        title = groupData["title"] as? String ?: "No Title",
                        previewImageUrl = groupData["previewImageUrl"] as? String ?: "",
                        publishDate = Date(groupData["publishDate"] as? Long ?: 0L)
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch image groups from Realtime DB", e)
            emptyList() // Return an empty list on failure to prevent crashes.
        }
    }


    /**
     * Fetches the list of individual images for a specific group from the Realtime Database.
     * This is called on-demand from the detail screen's logic.
     *
     * @param groupId The unique key of the parent image group.
     * @return A list of Image domain models.
     */
    suspend fun fetchImagesForGroup(groupId: String): List<Image> {
        val query = imagesRef.child(groupId).child("images")

        return try {
            val dataSnapshot = query.get().await()
            if (!dataSnapshot.exists()) {
                return emptyList()
            }

            val imageList = dataSnapshot.getValue<List<Map<String, Any>>>()

            imageList?.mapNotNull { imageMap ->
                // Manually map the fields from the Map to your Image domain object.
                val imageUrl = imageMap["imageUrl"] as? String ?: return@mapNotNull null
                val orderIndex = (imageMap["orderIndex"] as? Long)?.toInt() ?: 0

                Image(
                    id = "",
                    imageUrl = imageUrl,
                    orderIndex = orderIndex,
                )
            } ?: emptyList()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch images for group $groupId", e)
            emptyList()
        }
    }

}