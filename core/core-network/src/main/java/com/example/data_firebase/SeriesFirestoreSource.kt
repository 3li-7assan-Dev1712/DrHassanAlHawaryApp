package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.SeriesDto
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
}
