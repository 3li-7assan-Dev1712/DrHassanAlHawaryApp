package com.example.domain.module

import java.util.Date

data class Video(
    val id: String,
    val title: String,
    val videoUrl: String,
    val publishDate: Date,
    val youtubeVideoId: String?,
    val type: String = ""
)