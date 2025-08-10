package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.model.ArticlesResult

interface ArticlesRepository {

    suspend fun getAllArticles(): ArticlesResult

    suspend fun getArticleById(articleId: String): ArticlesResult

    fun filterArticles(articles: List<Article>, query: String): List<Article>

    suspend fun getLatestArticles(): ArticlesResult

}