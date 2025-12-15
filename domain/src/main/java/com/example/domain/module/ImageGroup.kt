package com.example.domain.module

import java.util.Date


data class ImageGroup(
    // Unique identifier for the design group to be used when fetching designs from Firestore
    val id: String,
    val title: String,
    val publishDate: Date = Date(),
    val previewImageUrl: String
)