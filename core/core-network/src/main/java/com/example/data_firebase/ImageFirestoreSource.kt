package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.ImageDto
import com.example.data_firebase.model.ImageGroupDto
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup
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
 * Handles all interactions with Firestore and Cloud Storage for the Image feature.
 */
class ImageFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseFunctions: FirebaseFunctions
) {
    private val TAG = "ImageFirestoreSource"
    private val imagesGroupCollection = firestore.collection("image_groups")

    private fun DocumentSnapshot.toImageGroupDtoSafe(): ImageGroupDto? {
        return try {
            this.toObject<ImageGroupDto>()?.copy(id = this.id)
        } catch (e: Exception) {
            Log.w(TAG, "Standard deserialization failed for image group ${this.id}, trying manual mapping", e)
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

                ImageGroupDto(
                    isDeleted = this.getBoolean("isDeleted") ?: this.getBoolean("deleted") ?: false,
                    id = this.id,
                    title = this.getString("title") ?: "",
                    previewImageUrl = this.getString("previewImageUrl") ?: "",
                    publishDate = publishDate,
                    updatedAt = updatedAt,
                    type = this.getString("type") ?: ""
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Manual mapping failed for image group ${this.id}", ex)
                null
            }
        }
    }

    suspend fun fetchLatestImageGroup(): ImageGroup? {
        try {
            val snapshot = imagesGroupCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d(TAG, "fetchLatestImageGroup: No image groups found.")
                return null
            }

            val document = snapshot.documents.first()
            val dto = document.toImageGroupDtoSafe()
            return dto?.let {
                ImageGroup(
                    id = document.id,
                    title = it.title,
                    publishDate = it.publishDate?.toDate() ?: Date(),
                    previewImageUrl = it.previewImageUrl,
                    isDeleted = it.isDeleted,
                    type = it.type
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchLatestImageGroup failed: ${e.message}")
            return null
        }
    }

    fun uploadImageGroup(title: String, imageUris: List<String>): Flow<UploadResult> =
        callbackFlow {
            if (imageUris.isEmpty()) {
                trySend(UploadResult.Error("No images to upload."))
                close(); return@callbackFlow
            }
            trySend(UploadResult.Progress(0))

            val uploadedImageUrls = mutableListOf<String>()
            try {
                imageUris.forEachIndexed { index, uriString ->
                    val uri = uriString.toUri()
                    val imageRef =
                        storage.reference.child("images/${System.currentTimeMillis()}_${index}.jpg")
                    val downloadUrl =
                        imageRef.putFile(uri).await().storage.downloadUrl.await().toString()
                    uploadedImageUrls.add(downloadUrl)
                    val progress =
                        (((index + 1).toFloat() / imageUris.size) * 90).toInt() // 90% for uploads
                    trySend(UploadResult.Progress(progress))
                }
            } catch (e: Exception) {
                trySend(UploadResult.Error("Failed to upload image: ${e.message}"))
                close(); return@callbackFlow
            }

            launch {
                try {
                    val publishDateIso = Date().toIsoString()
                    val contentMap = hashMapOf(
                        "title" to title,
                        "publishDate" to publishDateIso,
                        "previewImageUrl" to uploadedImageUrls.first(),
                        "images" to uploadedImageUrls.mapIndexed { index, url ->
                            mapOf("imageUrl" to url, "orderIndex" to index)
                        }
                    )

                    val payload = hashMapOf(
                        "collectionName" to "image_groups",
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
                    trySend(UploadResult.Error("Failed to save metadata via Cloud Function: ${e.message}"))
                    close()
                }
            }

            awaitClose { }
        }


    /**
     * Fetches the list of individual images for a specific group from Firestore.
     * This queries the 'images' subcollection of a specific ImageGroup document.
     *
     * @param groupId The unique ID of the parent image group document.
     * @return A list of Image domain models, ordered by their index.
     */
    suspend fun fetchImagesForGroup(groupId: String): List<Image> {
        val query = imagesGroupCollection
            .document(groupId)
            .collection("images")
            .orderBy(
                "orderIndex",
                Query.Direction.ASCENDING
            ) // Ensure images are in the correct order

        return try {
            val snapshot = query.get().await()
            if (snapshot.isEmpty) {
                return emptyList()
            }

            // Map the documents in the subcollection to the Image domain model
            snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<ImageDto>()
                dto?.let {
                    Image(
                        id = document.id, // The subcollection document has its own ID
                        imageUrl = it.imageUrl,
                        orderIndex = it.orderIndex
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch images for group $groupId", e)
            emptyList()
        }
    }


    suspend fun fetchImageGroupsPage(
        startAfterPublishDate: Long?,
        limit: Int
    ): Pair<List<ImageGroup>, Boolean> {
        return try {
            var query = imagesGroupCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (startAfterPublishDate != null) {
                query = query.startAfter(Timestamp(Date(startAfterPublishDate)))
            }

            val snapshot = query.get().await()

            val imageGroups = snapshot.documents.mapNotNull { document ->
                val dto = document.toImageGroupDtoSafe()
                dto?.let {
                    ImageGroup(
                        id = document.id,
                        title = it.title,
                        publishDate = it.publishDate?.toDate() ?: Date(),
                        previewImageUrl = it.previewImageUrl,
                        isDeleted = it.isDeleted,
                        type = it.type
                    )
                }
            }
            val endOfPaginationReached = imageGroups.size < limit
            Pair(imageGroups, endOfPaginationReached)
        } catch (e: Exception) {
            Log.e(TAG, "fetchImageGroupsPage failed: ${e.message}")
            Pair(emptyList(), true) // On error, return empty list and signal end of page
        }
    }

    suspend fun getUpdatedImageGroups(lastSyncTime: Long): List<ImageGroupDto> {
        return try {
            val snapshot = imagesGroupCollection
                .whereGreaterThan("updatedAt", Timestamp(Date(lastSyncTime)))
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toImageGroupDtoSafe() }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdatedImageGroups failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteImageGroup(groupId: String): Result<Unit> {
        return try {
            val payload = hashMapOf(
                "collectionName" to "image_groups",
                "documentId" to groupId
            )

            firebaseFunctions
                .getHttpsCallable("deleteContent")
                .call(payload)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteImageGroup failed", e)
            Result.failure(e)
        }
    }

    fun syncImageGroupsDbWithServer(): Flow<List<ImageGroupDto>> {
        return callbackFlow {
            val listenerRegistration = imagesGroupCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen failed: ${error.message}", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        trySend(snapshot.documents.mapNotNull { it.toImageGroupDtoSafe() })
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }
}
