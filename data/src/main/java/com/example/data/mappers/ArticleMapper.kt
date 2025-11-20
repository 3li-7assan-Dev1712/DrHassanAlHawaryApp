package com.example.data.mappers

import com.example.data_local.model.ArticleEntity
import com.example.domain.module.Article
import java.util.Date

/**
 * Converts an Article from the domain layer to an ArticleEntity for the data (Room) layer.
 */
fun Article.toEntity(): ArticleEntity =
    ArticleEntity(
        id = this.id,
        title = this.title,
        publishDate = this.publishDate.time,
        content = this.content
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