package com.example.feature.article.data.mapper

import com.example.data_local.model.ArticleEntity
import com.example.domain.module.Article
import java.sql.Date


fun Article.toEntity(): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    publishDate = publishDate.time,
    content = content,
)

fun ArticleEntity.toDomainModel(): Article = Article(
    id = id,
    title = title,
    publishDate = Date(publishDate),
    content = content
)