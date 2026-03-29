package com.example.admin.ui.upload_article_screen

import java.util.Date

data class ArticleUiState(
    val articleId: String? = null,
    val title: String = "",
    val content: String = "",
    val publishDate: Long = Date().time,
    val isLoading: Boolean = false,
    val isArticleUploaded: Boolean = false,
    val userMessage: String? = null
)
