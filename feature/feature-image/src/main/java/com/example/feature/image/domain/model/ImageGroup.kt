package com.example.feature.image.domain.model

import java.util.Date


data class ImageGroup(
    // Unique identifier for the image group to be used when fetching designs from Firestore
    val id: String,
    val title: String,
    val publishDate: Date = Date(),
    val previewImageUrl: String,
    val type: String = ""
)