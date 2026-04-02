package com.example.search.presentation.mapper

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.HighlightResult
import com.example.search.presentation.model.SearchHit
import com.example.search.presentation.utils.algoliaHighlightToAnnotatedString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive


/**
 * Parses the raw JSON hit from Algolia into our structured [SearchHit] data class.
 * This function also handles extracting and converting highlighted text.
 */
fun parseHit(hit: ResponseSearch.Hit): SearchHit {
    // Extract standard attributes
    val objectID = hit["objectID"]?.jsonPrimitive?.content ?: ""
    val type = hit["type"]?.jsonPrimitive?.content
    val title = hit["title"]?.jsonPrimitive?.content
    val content = hit["content"]?.jsonPrimitive?.content
    val previewImageUrl = hit["previewImageUrl"]?.jsonPrimitive?.content
    val videoUrl = hit["videoUrl"]?.jsonPrimitive?.content
    val audioUrl = hit["audioUrl"]?.jsonPrimitive?.content

    val videoId = hit["videoYoutubeId"]?.jsonPrimitive?.content

    val highlightResult = hit["_highlightResult"] as? JsonObject

    val highlightedTitle: AnnotatedString? = highlightResult?.get("title")?.let { jsonElement ->
        // Get the raw HTML string to be used in the function
        val highlightValue = Json.decodeFromJsonElement<HighlightResult>(jsonElement).value
        algoliaHighlightToAnnotatedString(highlightValue)

    } ?: title?.let { buildAnnotatedString { append(it) } }

    val highlightedContent: AnnotatedString? = highlightResult?.get("content")?.let { jsonElement ->
        val highlightValue = Json.decodeFromJsonElement<HighlightResult>(jsonElement).value
        algoliaHighlightToAnnotatedString(highlightValue)

    } ?: content?.let { buildAnnotatedString { append(it) } } // Fallback
    return SearchHit(
        objectID = objectID,
        type = type,
        title = title,
        content = content,
        previewImageUrl = previewImageUrl,
        videoUrl = videoUrl,
        audioUrl = audioUrl,
        youtubeVideoId = videoId,
        highlightedTitle = highlightedTitle,
        highlightedContent = highlightedContent
    )
}
