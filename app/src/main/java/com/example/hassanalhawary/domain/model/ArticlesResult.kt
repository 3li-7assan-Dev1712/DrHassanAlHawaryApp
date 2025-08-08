package com.example.hassanalhawary.domain.model

data class ArticlesResult(
    val articles: List<Article>? = null,
    val article: Article? = null,
    val errorMessage: String? = null

)
