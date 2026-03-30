package com.example.data_firebase.model

import com.google.firebase.firestore.PropertyName

data class LessonDto(
    val id: String = "",
    val playlistId: String = "",
    val order: Int = 0,
    val title: String = "",
    val audioUrl: String = "",
    val duration: Long = 0,
    val pdfUrl: String = "",
    val publishDate: Long = 0L,
    val updatedAt: Long = 0L,

    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    @get:JvmName("isDeletedField")
    var isDeleted: Boolean = false,

    )
