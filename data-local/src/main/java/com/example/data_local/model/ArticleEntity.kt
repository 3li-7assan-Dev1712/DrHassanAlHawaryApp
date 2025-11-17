package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.module.Article
import java.util.Date


@Entity(tableName = "articles")
data class ArticleEntity (
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val content: String,
    val publishDate: Long
)

/**
 * Convert ArticleAudio to Article domain model
 */
fun ArticleEntity.toDomainModel(): Article =
    Article(
        id = this.id,
        title = this.title,
        content = this.content,
        publishDate = Date(this.publishDate)

    )



