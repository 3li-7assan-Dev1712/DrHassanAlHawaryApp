package com.example.data_firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class VideoDto(
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    @get:JvmName("isDeleted")
    var isDeleted: Boolean = false,

    val id: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val videoYoutubeId: String = "",
    val publishDate: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
