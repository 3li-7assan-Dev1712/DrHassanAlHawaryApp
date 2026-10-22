package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "articles")
data class ArticleEntity (
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val content: String,
    val type: String,
    val publishDate: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    /** The series this item belongs to (null: a standalone item). */
    val seriesId: String? = null,
    /** 1-based position inside [seriesId]. */
    val episodeNumber: Long? = null,
)
