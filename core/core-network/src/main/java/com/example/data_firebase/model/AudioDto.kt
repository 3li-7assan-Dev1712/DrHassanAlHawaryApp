package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName


//    @get:JvmName("isDeleted")

@Keep 
data class AudioDto(
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    val id: String = "",
    val title: String = "",
    val audioUrl: String = "",
    val durationInMillis: Long = 0L,
    val publishDate: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val type: String = "",
    val categoryId: String? = null
)
