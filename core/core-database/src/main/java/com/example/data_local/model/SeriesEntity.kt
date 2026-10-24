package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    /** The Firestore collection holding the items: "audios", "articles", "videos" or "image_groups". */
    val collection: String,
    val categoryId: String?,
    val hashtag: String,
    val order: Long,
    val itemCount: Long,
    val totalDurationMs: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
