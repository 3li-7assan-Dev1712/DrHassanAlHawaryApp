package com.example.feature.article.presentation.detail

import com.example.domain.module.Article


sealed interface DetailArticleUiState {
    object Loading : DetailArticleUiState

    /**
     * [paragraphs] is the display-cleaned body (see ArticleTextCleaner.readerParagraphs);
     * [article] keeps the stored text untouched.
     */
    data class Success(
        val article: Article,
        val paragraphs: List<String> = emptyList(),
        val readingMinutes: Int = 1,
    ) : DetailArticleUiState

    data class Error(val message: String) : DetailArticleUiState
}
