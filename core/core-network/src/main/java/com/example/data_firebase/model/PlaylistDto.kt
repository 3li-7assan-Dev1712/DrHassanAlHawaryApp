package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date


@Keep
@IgnoreExtraProperties
data class PlaylistDto(
    val id: String = "",
    val title: String = "",
    val levelId: String = "",
    val order: Int = 0,
    val thumbnailUrl: String = "",
    val updatedAt: Date = Date()
)