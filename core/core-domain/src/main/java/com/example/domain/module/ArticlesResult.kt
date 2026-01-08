package com.example.domain.module

data class ArticlesResult(
    val articles: List<Article>? = null,
    val article: Article? = null,
    val errorMessage: String? = null

)
