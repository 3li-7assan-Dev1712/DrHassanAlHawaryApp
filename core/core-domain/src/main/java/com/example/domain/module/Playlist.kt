package com.example.domain.module

import java.util.Date

data class Playlist(

    val id: String,
    val title: String,
    val levelId: String = "",
    val order: Int = 0,
    val updatedAt: Date = Date(),
    val thumbnailUrl: String,
)