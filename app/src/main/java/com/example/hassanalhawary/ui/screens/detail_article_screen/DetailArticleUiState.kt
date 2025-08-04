package com.example.hassanalhawary.ui.screens.detail_article_screen

import com.example.hassanalhawary.domain.model.Article

sealed interface DetailArticleUiState {
    object Loading : DetailArticleUiState
    data class Success(val article: Article) : DetailArticleUiState
    data class Error(val message: String) : DetailArticleUiState
}