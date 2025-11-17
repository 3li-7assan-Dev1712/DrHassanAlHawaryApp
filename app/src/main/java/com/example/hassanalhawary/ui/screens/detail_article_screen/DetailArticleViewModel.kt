package com.example.hassanalhawary.ui.screens.detail_article_screen

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.getFakeArticles
import com.example.domain.use_cases.GetArticleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailArticleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getArticleByIdUseCase: GetArticleByIdUseCase
) : ViewModel() {
//    private val articleId: String? = savedStateHandle["articleId"]

    private val _uiState = MutableStateFlow<DetailArticleUiState>(DetailArticleUiState.Loading)
    val uiState: StateFlow<DetailArticleUiState> = _uiState.asStateFlow()

    init {
//        fetchArticleDetails()
        val articleId = savedStateHandle.get<String>("articleId")
        Log.d(TAG, "articleId = : $articleId ")
        if (articleId != null) {
            fetchArticleDetailsById(articleId)
        }
    }

    private fun fetchArticleDetailsById(articleId: String) {
        viewModelScope.launch {

            try {
                val articleResult = getArticleByIdUseCase(articleId).article
                if (articleResult != null) {
                    Log.d(TAG, "fetchArticleDetailsById: article full content ${articleResult.content}")
                    _uiState.value = DetailArticleUiState.Success(
                        article = articleResult
                    )
                } else {
                    _uiState.value = DetailArticleUiState.Error(
                        message = "Article not found"
                    )
                }


            } catch (e: Exception) {
                _uiState.value = DetailArticleUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            }

        }

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