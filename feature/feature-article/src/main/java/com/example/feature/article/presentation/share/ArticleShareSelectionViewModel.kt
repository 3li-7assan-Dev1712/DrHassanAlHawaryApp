package com.example.feature.article.presentation.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.article.domain.use_case.GetArticleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleShareSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleShareSelectionUiState())
    val uiState: StateFlow<ArticleShareSelectionUiState> = _uiState.asStateFlow()

    init {
        val articleId = savedStateHandle.get<String>(ARG_ARTICLE_ID)
        if (articleId != null) {
            loadArticle(articleId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Article not found") }
        }
    }

    private fun loadArticle(articleId: String) {
        viewModelScope.launch {
            try {
                getArticleByIdUseCase(articleId).collect { article ->
                    if (article != null) {
                        val displayText = article.content
                            .split("\n")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .joinToString("\n\n")
                        _uiState.update {
                            it.copy(isLoading = false, articleTitle = article.title, displayText = displayText)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Article not found") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    fun onSelectionChanged(start: Int, end: Int) {
        _uiState.update { it.copy(selectionStart = start, selectionEnd = end) }
    }

    fun onClearSelection() {
        _uiState.update { it.copy(selectionStart = 0, selectionEnd = 0) }
    }

    companion object {
        const val ARG_ARTICLE_ID = "articleId"
    }
}
