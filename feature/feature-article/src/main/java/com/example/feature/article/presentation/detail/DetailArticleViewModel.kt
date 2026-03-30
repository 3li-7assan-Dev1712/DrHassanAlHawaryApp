package com.example.feature.article.presentation.detail

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.article.domain.use_case.GetArticleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailArticleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArticleByIdUseCase: GetArticleByIdUseCase
) : ViewModel() {
//    private val articleId: String? = savedStateHandle["articleId"]

    private val _uiState = MutableStateFlow<DetailArticleUiState>(DetailArticleUiState.Loading)
    val uiState: StateFlow<DetailArticleUiState> = _uiState.asStateFlow()
    val index: Int = savedStateHandle.get<Int>("paragraphIndex") ?: -1


    init {
        val articleId = savedStateHandle.get<String>("articleId")
        Log.d(TAG, "articleId = : $articleId ")
        if (articleId != null) {
            fetchArticleDetailsById(articleId)
        }
    }

    private fun fetchArticleDetailsById(articleId: String) {
        viewModelScope.launch {

            try {
                getArticleByIdUseCase(articleId).collect { art ->
                    if (art != null) {
                        _uiState.value = DetailArticleUiState.Success(
                            article = art
                        )
                    } else {
                        _uiState.value = DetailArticleUiState.Error(
                            message = "Article not found"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = DetailArticleUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            }

        }

    }

}
