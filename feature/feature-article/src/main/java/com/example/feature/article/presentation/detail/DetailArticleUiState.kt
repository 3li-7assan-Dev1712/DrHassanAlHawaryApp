package com.example.feature.article.presentation.detail

import com.example.domain.module.Article


sealed interface DetailArticleUiState {
    object Loading : DetailArticleUiState
    data class Success(val article: Article) : DetailArticleUiState
    data class Error(val message: String) : DetailArticleUiState
}