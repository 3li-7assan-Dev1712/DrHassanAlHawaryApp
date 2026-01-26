package com.example.study.domain.model

data class LessonPlaylist(
    val id: String,
    val title: String,
    val lessonCount: Int,
    val progress: Float = 0f,
    val thumbnail: String? = null
)
