package com.example.hassanalhawary.ui.screens.articles_screen

import com.example.hassanalhawary.domain.model.Article

sealed interface ArticlesUiState {

    val searchQuery: String

    data class Success(
        val allArticles: List<Article> = emptyList(),
        val displayedArticles: List<Article> = allArticles,
        override val searchQuery: String = ""

    ) : ArticlesUiState
    data class Error(val message: String? = null, override val searchQuery: String = "") : ArticlesUiState
    object Loading : ArticlesUiState {
        override val searchQuery: String
            get() = ""
    }
}
