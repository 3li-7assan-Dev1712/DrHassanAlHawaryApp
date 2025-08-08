package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.ArticlesResult

interface ArticlesRepository {

    suspend fun getAllArticles(): ArticlesResult

    suspend fun getArticleById(articleId: String): ArticlesResult

}