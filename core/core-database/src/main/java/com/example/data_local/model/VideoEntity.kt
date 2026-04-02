package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String, // This is the Firebase Key
    val title: String,
    val videoUrl: String,
    val publishDate: Long, // Stored as Long (timestamp) for Room compatibility

    val youtubeVideoId: String?, // Extracted ID for showing thumbnails (e.g., "ogfYd705cRs")
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val type: String = ""
)
