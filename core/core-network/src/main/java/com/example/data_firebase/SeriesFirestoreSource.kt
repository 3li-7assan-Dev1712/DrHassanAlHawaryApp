package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.SeriesDto
import com.example.domain.diagnostics.ErrorReporter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Handles all interactions with Firestore for series (the `series` collection).
 */
class SeriesFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseFunctions: FirebaseFunctions
) {
    private val TAG = "SeriesFirestoreSource"
    private val seriesCollection = firestore.collection("series")

    private fun DocumentSnapshot.toSeriesDtoSafe(): SeriesDto? {
        return try {
            this.toObject<SeriesDto>()?.copy(id = this.id)
        } catch (e: Exception) {
            Log.e(TAG, "Deserialization failed for series ${this.id}", e)
            ErrorReporter.record("series mapping ${this.id}", e)
            null
        }
    }

    /** Every series changed after [lastSyncTime], soft-deleted ones included (delta sync). */
    suspend fun getUpdatedSeries(lastSyncTime: Long): List<SeriesDto> {
        return try {
            val snapshot = seriesCollection
                .whereGreaterThan("updatedAt", Timestamp(Date(lastSyncTime)))
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toSeriesDtoSafe() }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdatedSeries failed: ${e.message}")
            emptyList()
        }
    }

    // Admin writes go through the content callables, as for the other collections. The
    // backend sets updatedAt and isDeleted and keeps itemCount / totalDurationMs.

    /** A new series in [collection] ("audios", ...); [categoryId] only for audios and videos. */
    suspend fun createSeries(
        title: String,
        collection: String,
        categoryId: String?,
        hashtag: String,
        order: Long,
    ): Result<Unit> = callContent(
        "uploadContent",
        hashMapOf(
            "collectionName" to SERIES,
            "contentData" to hashMapOf(
                "title" to title,
                "collection" to collection,
                "categoryId" to categoryId,
                "hashtag" to hashtag,
                "order" to order,
            ),
        ),
    )

    /** Rename, re-tag, move to another category or reorder: [updates] by field name. */
    suspend fun updateSeries(seriesId: String, updates: Map<String, Any?>): Result<Unit> = callContent(
        "updateContent",
        hashMapOf("collectionName" to SERIES, "documentId" to seriesId, "updates" to updates),
    )

    /** Soft-deletes the series (deleteContent sets isDeleted); its items keep their seriesId. */
    suspend fun deleteSeries(seriesId: String): Result<Unit> = callContent(
        "deleteContent",
        hashMapOf("collectionName" to SERIES, "documentId" to seriesId),
    )

    private suspend fun callContent(function: String, payload: Map<String, Any?>): Result<Unit> {
        return try {
            firebaseFunctions.getHttpsCallable(function).call(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "$function on series failed: ${e.message}")
            Result.failure(e)
        }
    }

    private companion object {
        const val SERIES = "series"
    }
}
