package com.example.data_firebase

import android.util.Log
import com.example.domain.module.SeriesPlacement
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Partial admin edits of any content document ("audios", "videos", "articles",
 * "image_groups") through updateContent: only the fields given change.
 */
class ContentFieldsFirestoreSource @Inject constructor(
    private val firebaseFunctions: FirebaseFunctions
) {
    private val TAG = "ContentFieldsSource"

    suspend fun setCategory(collectionName: String, documentId: String, categoryId: String): Result<Unit> =
        update(collectionName, documentId, mapOf("categoryId" to categoryId))

    suspend fun setPlacement(collectionName: String, documentId: String, placement: SeriesPlacement): Result<Unit> =
        update(collectionName, documentId, placement.toFirestoreFields())

    private suspend fun update(collectionName: String, documentId: String, updates: Map<String, Any?>): Result<Unit> = try {
        firebaseFunctions.getHttpsCallable("updateContent").call(
            hashMapOf("collectionName" to collectionName, "documentId" to documentId, "updates" to updates)
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "updateContent on $collectionName/$documentId failed: ${e.message}")
        Result.failure(e)
    }
}
