package com.example.feature.video.domain.model

import java.util.Date

data class Video(
    val id: String,
    val title: String,
    val videoUrl: String,
    val publishDate: Date,
    val youtubeVideoId: String?
)