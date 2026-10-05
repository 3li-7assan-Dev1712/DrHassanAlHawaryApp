package com.example.search.presentation.model

/** One Algolia hit, as stored. Display cleaning happens in the row, never here. */
data class SearchHit(
    val objectID: String,
    val type: String?,
    val title: String?,
    val content: String?,
    val previewImageUrl: String?,
    val videoUrl: String?,
    val audioUrl: String?,
    val youtubeVideoId: String?,
)
