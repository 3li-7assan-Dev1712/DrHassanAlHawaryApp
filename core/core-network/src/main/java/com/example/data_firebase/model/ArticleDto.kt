package com.example.data_firebase.model

import java.util.Date

/**
 * Data Transfer Object (DTO) for an article stored in Firestore.
 * This class exactly matches the structure of the documents in the 'articles' collection.
 * It prevents crashes if the domain 'Article' model changes.
 */
data class ArticleDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val publishDate: Date = Date()
)