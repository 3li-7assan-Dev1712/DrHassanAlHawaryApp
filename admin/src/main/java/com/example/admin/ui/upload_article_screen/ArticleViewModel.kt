package com.example.admin.ui.upload_article_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Article
import com.example.domain.use_cases.articles.UploadArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val uploadArticleUseCase: UploadArticleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    fun onEvent(event: ArticleUserEvent) {
        when (event) {
            is ArticleUserEvent.OnTitleChanged -> {
                _uiState.update { it.copy(title = event.title) }
            }

            is ArticleUserEvent.OnContentChanged -> {
                _uiState.update { it.copy(content = event.content) }
            }

            is ArticleUserEvent.OnPublishDateChanged -> {
                _uiState.update { it.copy(publishDate = event.date) }
            }

            ArticleUserEvent.OnUploadClicked -> {
                uploadArticle()
            }

            ArticleUserEvent.OnUserMessageShown -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun uploadArticle() {
        val currentState = _uiState.value

        // Validate inputs before proceeding
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(userMessage = "Title cannot be empty.") }
            return
        }

        if (currentState.content.isBlank()) {
            _uiState.update { it.copy(userMessage = "Content cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val newArticle = Article(
                    title = currentState.title,
                    content = currentState.content,
                    publishDate = Date(currentState.publishDate),
                )
                uploadArticleUseCase(
                    newArticle
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isArticleUploaded = true,
                        userMessage = "Article uploaded successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Upload failed: ${e.message}"
                    )
                }
            }
        }
    }
}