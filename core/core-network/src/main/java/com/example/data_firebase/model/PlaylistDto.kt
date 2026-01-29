package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties


@Keep
@IgnoreExtraProperties
data class PlaylistDto(
    val id: String = "",
    val title: String = "",
    val level: Int = 0,
    val thumbnailUrl: String = "",
)