package com.example.search.presentation.mapper

import com.algolia.search.model.response.ResponseSearch
import com.example.search.presentation.model.SearchHit
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses the raw JSON hit from Algolia into our structured [SearchHit]. Algolia's own
 * `<em>` highlights are not used: they mark fragments inside words. The row highlights
 * whole words client-side (ArabicSearchText).
 */
fun parseHit(hit: ResponseSearch.Hit): SearchHit = SearchHit(
    objectID = hit["objectID"]?.jsonPrimitive?.content ?: "",
    type = hit["type"]?.jsonPrimitive?.content,
    title = hit["title"]?.jsonPrimitive?.content,
    content = hit["content"]?.jsonPrimitive?.content,
    previewImageUrl = hit["previewImageUrl"]?.jsonPrimitive?.content,
    videoUrl = hit["videoUrl"]?.jsonPrimitive?.content,
    audioUrl = hit["audioUrl"]?.jsonPrimitive?.content,
    youtubeVideoId = hit["videoYoutubeId"]?.jsonPrimitive?.content,
)
