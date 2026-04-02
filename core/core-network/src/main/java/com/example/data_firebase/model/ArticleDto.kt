package com.example.data_firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Data Transfer Object (DTO) for an article stored in Firestore.
 */
data class ArticleDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val publishDate: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val type: String= "",
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false
)
