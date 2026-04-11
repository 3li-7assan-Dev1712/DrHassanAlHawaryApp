package com.example.domain.module

import java.util.Date

data class Audio(


    // for server
    val id: String,
    val categoryId: String = "",
    val title: String,
    val audioUrl: String,
    val durationInMillis: Long,
    val publishDate: Date,
    val type: String = "",

    // for user and local cache
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val lastPlayedTimestamp: Long?,

    // for ui state play/pause
    val isPlaying: Boolean = false,

    val localFilePath: String? = null

)