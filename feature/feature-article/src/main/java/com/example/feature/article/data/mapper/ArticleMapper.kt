package com.example.feature.article.data.mapper

import com.example.domain.text.ArticleTextCleaner
import com.example.data_local.model.ArticleEntity
import com.example.domain.module.Article
import java.sql.Date


private val textCleaner = ArticleTextCleaner()

/** Display mapping: the title is cleaned (no author suffix / Facebook timestamp) for every
 * article screen - list, detail header - while the stored entity keeps the original. */
fun ArticleEntity.toDomainModel(): Article = Article(
    id = id,
    title = textCleaner.cleanTitle(title),
    publishDate = Date(publishDate),
    content = content
)