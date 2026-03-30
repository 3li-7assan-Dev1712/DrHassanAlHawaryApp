package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class PlaylistDto(
    val id: String = "",
    val title: String = "",
    val levelId: String = "",
    val order: Int = 0,
    val thumbnailUrl: String = "",
    val publishDate: Long = 0L,
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false
)
