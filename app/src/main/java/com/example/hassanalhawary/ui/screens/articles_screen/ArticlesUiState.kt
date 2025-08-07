package com.example.hassanalhawary.ui.screens.articles_screen

import com.example.hassanalhawary.domain.model.Article

sealed interface ArticlesUiState {

    data class Success(val articles: List<Article>) : ArticlesUiState
    data class Error(val message: String? = null) : ArticlesUiState
    object Loading : ArticlesUiState
}
