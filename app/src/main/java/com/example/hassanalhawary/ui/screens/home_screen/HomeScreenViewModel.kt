package com.example.hassanalhawary.ui.screens.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.use_cases.GetLatestArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val getLatestArticlesUseCase: GetLatestArticlesUseCase,
) : ViewModel() {


    private val _homeScreenUiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState())
    val homeScreenUiState: StateFlow<HomeScreenUiState> = _homeScreenUiState.asStateFlow()

    init {
        loadLatestArticles()

    }


    private fun loadLatestArticles() {
        viewModelScope.launch {
            val articlesResult = getLatestArticlesUseCase()
            if (articlesResult.articles != null) {
                _homeScreenUiState.value = _homeScreenUiState.value.copy(
                    latestArticles = articlesResult.articles,
                    loadingLatestArticles = false
                )
            }
        }
    }
}