package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class PlaylistDto(
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    @get:JvmName("isDeleted")
    var isDeleted: Boolean = false,


    val id: String = "",
    val title: String = "",
    val levelId: String = "",
    val order: Int = 0,
    val thumbnailUrl: String = "",
    val publishDate: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
