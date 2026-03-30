package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.ImageDto
import com.example.data_firebase.model.ImageGroupDto
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup
import com.example.domain.use_cases.audios.UploadResult
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
 * Handles all interactions with Firestore and Cloud Storage for the Image feature.
 */
class ImageFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val TAG = "ImageFirestoreSource"
    private val imagesCollection = firestore.collection("image_groups")


    suspend fun fetchLatestImageGroup(): ImageGroup? {
        try {
            val snapshot = imagesCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d(TAG, "fetchLatestImageGroup: No image groups found.")
                return null
            }

            val document = snapshot.documents.first()
            val dto = document.toObject<ImageGroupDto>()
            return dto?.let {
                ImageGroup(
                    id = document.id,
                    title = it.title,
                    publishDate = Date(it.publishDate),
                    previewImageUrl = it.previewImageUrl
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

            val imageGroupDto = ImageGroupDto(
                title = title,
                publishDate = System.currentTimeMillis(),
                previewImageUrl = uploadedImageUrls.first()
            )

            // Add the main group doc
            val groupDocRef = firestore.collection("image_groups").document()
            groupDocRef.set(imageGroupDto).await()

            // Add the images to a subcollection
            val imagesSubCollection = groupDocRef.collection("images")
            uploadedImageUrls.forEachIndexed { index, url ->
                val imageDto = ImageDto(
                    imageUrl = url,
                    orderIndex = index
                )
                imagesSubCollection.add(imageDto).await()
            }
            trySend(UploadResult.Progress(100))
            trySend(UploadResult.Success)
            close()

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
        val query = imagesCollection
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
        startAfterKey: String?,
        limit: Int
    ): Pair<List<ImageGroup>, Boolean> {
        return try {
            var query = imagesCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (startAfterKey != null) {
                val lastVisibleDoc = imagesCollection.document(startAfterKey).get().await()
                query = query.startAfter(lastVisibleDoc)
            }

            val snapshot = query.get().await()

            val imageGroups = snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<ImageGroupDto>()
                dto?.let {
                    ImageGroup(
                        id = document.id,
                        title = it.title,
                        publishDate = Date(it.publishDate),
                        previewImageUrl = it.previewImageUrl
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

    suspend fun deleteImageGroup(groupId: String): Result<Unit> {
        return try {
            // 1. Get all images in the group to get their URLs for deletion in Storage
            val images = fetchImagesForGroup(groupId)
            
            // 2. Delete images from subcollection
            val subcollection = imagesCollection.document(groupId).collection("images")
            val subDocs = subcollection.get().await()
            subDocs.forEach { it.reference.delete().await() }
            
            // 3. Delete the main document
            imagesCollection.document(groupId).delete().await()
            
            // 4. Delete files from Storage
            images.forEach { image ->
                try {
                    storage.getReferenceFromUrl(image.imageUrl).delete().await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete image from storage: ${image.imageUrl}", e)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteImageGroup failed", e)
            Result.failure(e)
        }
    }


}
