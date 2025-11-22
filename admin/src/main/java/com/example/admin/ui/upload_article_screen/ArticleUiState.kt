package com.example.admin.ui.upload_article_screen


data class ArticleUiState(
    val title: String = "",
    val content: String = "",
    val publishDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isArticleUploaded: Boolean = false,
    val userMessage: String? = null
)