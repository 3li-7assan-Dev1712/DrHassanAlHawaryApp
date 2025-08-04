package com.example.hassanalhawary.ui.screens.detail_article_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.model.getFakeArticles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailArticleViewModel @Inject constructor(

) : ViewModel() {
//    private val articleId: String? = savedStateHandle["articleId"]

    private val _uiState = MutableStateFlow<DetailArticleUiState>(DetailArticleUiState.Loading)
    val uiState: StateFlow<DetailArticleUiState> = _uiState.asStateFlow()

    init {
        fetchArticleDetails()
    }

    private fun fetchArticleDetails() {
        viewModelScope.launch {

            try {
                // --- FAKE DATA FETCH ---
                // Replace this with actual data fetching from your repository
                delay(1000) // Simulate network delay
                val fetchedArticle = getFakeArticles().firstOrNull()
                if (fetchedArticle != null) {
                    _uiState.value = DetailArticleUiState.Success(
                        article = fetchedArticle
                    )
                } else {
                    _uiState.value = DetailArticleUiState.Error(
                        message = "Article not found"
                    )
                }

            } catch (e: Exception) {
                _uiState.value =
                    DetailArticleUiState.Error(
                        message = e.message ?: "Unknown error"
                    )
            }
        }
    }
}