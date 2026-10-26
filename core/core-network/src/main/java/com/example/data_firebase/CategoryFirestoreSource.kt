package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.CategoryDto
import com.example.domain.diagnostics.ErrorReporter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * The `categories` collection: a handful of documents, read live (Firestore's offline cache
 * keeps them for the next start). Writes go through the content callables, as for series.
 */
class CategoryFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseFunctions: FirebaseFunctions
) {
    private val TAG = "CategoryFirestoreSource"
    private val categories = firestore.collection(CATEGORIES)

    private fun DocumentSnapshot.toCategoryDtoSafe(): CategoryDto? = try {
        toObject<CategoryDto>()?.copy(id = id)
    } catch (e: Exception) {
        Log.e(TAG, "Deserialization failed for category $id", e)
        ErrorReporter.record("category mapping $id", e)
        null
    }

    /**
     * Every stored category, deleted ones included. A failed read (no access yet, offline
     * with nothing cached) emits an empty list: the apps then use the built-in categories.
     */
    fun observeCategories(): Flow<List<CategoryDto>> = callbackFlow {
        val registration = categories.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "categories listener failed: ${error.message}")
                ErrorReporter.record("categories listener", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { it.toCategoryDtoSafe() }.orEmpty())
        }
        awaitClose { registration.remove() }
    }

    /** The stored categories as the server has them now, for a write's decisions. */
    suspend fun fetchCategories(): Result<List<CategoryDto>> = try {
        Result.success(categories.get().await().documents.mapNotNull { it.toCategoryDtoSafe() })
    } catch (e: Exception) {
        Log.e(TAG, "fetchCategories failed: ${e.message}")
        Result.failure(e)
    }

    suspend fun createCategory(
        categoryId: String,
        collection: String,
        title: String,
        description: String?,
        order: Long,
    ): Result<Unit> = callContent(
        "uploadContent",
        hashMapOf(
            "collectionName" to CATEGORIES,
            "contentData" to hashMapOf(
                "categoryId" to categoryId,
                "collection" to collection,
                "title" to title,
                "description" to description,
                "order" to order,
            ),
        ),
    )

    /** Rename, re-describe or reorder: [updates] by field name. */
    suspend fun updateCategory(documentId: String, updates: Map<String, Any?>): Result<Unit> = callContent(
        "updateContent",
        hashMapOf("collectionName" to CATEGORIES, "documentId" to documentId, "updates" to updates),
    )

    /** Soft-deletes the category (deleteContent sets isDeleted). */
    suspend fun deleteCategory(documentId: String): Result<Unit> = callContent(
        "deleteContent",
        hashMapOf("collectionName" to CATEGORIES, "documentId" to documentId),
    )

    private suspend fun callContent(function: String, payload: Map<String, Any?>): Result<Unit> = try {
        firebaseFunctions.getHttpsCallable(function).call(payload).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "$function on categories failed: ${e.message}")
        Result.failure(e)
    }

    private companion object {
        const val CATEGORIES = "categories"
    }
}
