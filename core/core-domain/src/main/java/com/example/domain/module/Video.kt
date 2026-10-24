package com.example.domain.module

import java.util.Date

data class Video(
    val id: String,
    val title: String,
    val videoUrl: String,
    val publishDate: Date,
    val youtubeVideoId: String?,
    val type: String = "",
    val categoryId: String? = null,
    // series membership: null for a standalone item
    val seriesId: String? = null,
    /** 1-based position inside [seriesId]. */
    val episodeNumber: Long? = null,
)