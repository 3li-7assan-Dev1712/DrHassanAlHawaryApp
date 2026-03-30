package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "articles")
data class ArticleEntity (
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val content: String,
    val publishDate: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
