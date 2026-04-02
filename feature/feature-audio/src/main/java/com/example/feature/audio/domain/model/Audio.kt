package com.example.feature.audio.domain.model

import java.util.Date

data class Audio(



    val id: String,
    val categoryId: String = "",
    val title: String,
    val audioUrl: String,
    val durationInMillis: Long,
    val publishDate: Date,
    val type: String = "",

    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val lastPlayedTimestamp: Long?,

    val isPlaying: Boolean = false,

    val localFilePath: String? = null,

    )