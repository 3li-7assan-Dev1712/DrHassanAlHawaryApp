package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * One audio or video category: a document in `categories`. Items point to it with their
 * `categoryId` field, which is [categoryId] here (not the document id), so the built-in ids
 * ("fatawah") keep working.
 */
@Keep
@IgnoreExtraProperties
data class CategoryDto(
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    val id: String = "",
    val categoryId: String = "",
    /** "audios" or "videos". */
    val collection: String = "",
    val title: String = "",
    val description: String? = null,
    val order: Long = 0L,
    val updatedAt: Timestamp? = null
)
