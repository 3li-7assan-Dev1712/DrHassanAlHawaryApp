package com.example.data.mappers

import com.example.data_firebase.model.ArticleDto
import com.example.data_local.model.ArticleEntity
import com.example.domain.module.Article
import java.util.Date

fun Article.toEntity(): ArticleEntity =
    ArticleEntity(
        id = this.id,
        title = this.title,
        publishDate = this.publishDate.time,
        content = this.content,
        type = this.type,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted
    )

fun ArticleEntity.toDomainModel(): Article =
    Article(
        id = this.id,
        title = this.title,
        content = this.content,
        type = this.type,
        publishDate = Date(this.publishDate),
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted
    )

fun ArticleDto.toEntity(): ArticleEntity =
    ArticleEntity(
        id = id,
        title = title,
        content = content,
        type = type,
        publishDate = publishDate?.toDate()?.time ?: 0L,
        updatedAt = updatedAt?.toDate()?.time ?: 0L,
        isDeleted = isDeleted
    )

fun ArticleDto.toDomainModel(): Article =
    Article(
        id = id,
        title = title,
        content = content,
        type = type,
        publishDate = publishDate?.toDate() ?: Date(),
        updatedAt = updatedAt?.toDate()?.time ?: 0L,
        isDeleted = isDeleted
    )
