package com.example.search.domain.model

data class SearchResult(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val videoUrl: String? = null

)
