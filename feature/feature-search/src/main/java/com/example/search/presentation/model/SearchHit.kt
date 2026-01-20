package com.example.search.presentation.model

import androidx.compose.ui.text.AnnotatedString

/**
 * A robust data class representing a parsed search hit.
 * It includes fields for common attributes and highlighted versions.
 */
data class SearchHit(
    val objectID: String,
    val type: String?,
    val title: String?,
    val content: String?,
    val url: String?,
    val youtubeVideoId: String?,
    val highlightedTitle: AnnotatedString?,
    val highlightedContent: AnnotatedString?
)