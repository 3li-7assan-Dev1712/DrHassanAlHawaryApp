package com.example.data_firebase

import com.example.data_firebase.model.ContentCategoryDto
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CategoryFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getCategories(type: ContentType): Flow<List<ContentCategory>> = callbackFlow {
        val listener = firestore.collection("categories")
            .whereEqualTo("type", type.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ContentCategoryDto::class.java)?.let { dto ->
                        ContentCategory(
                            id = dto.id,
                            title = dto.title,
                            type = dto.type
                        /*    imageUrl = dto.imageUrl,
                            description = dto.description*/
                        )
                    }
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }
}
