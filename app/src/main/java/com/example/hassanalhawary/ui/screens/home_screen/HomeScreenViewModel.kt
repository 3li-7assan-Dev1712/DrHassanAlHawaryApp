package com.example.hassanalhawary.ui.screens.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.use_cases.GetAllAudiosUseCase
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
    private val getLatestAudiosUseCase: GetAllAudiosUseCase
) : ViewModel() {


    private val _homeScreenUiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState())
    val homeScreenUiState: StateFlow<HomeScreenUiState> = _homeScreenUiState.asStateFlow()

    init {
        loadLatestArticles()
        loadLatestAudios()


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

    private fun loadLatestAudios() {
        viewModelScope.launch {
            val audiosResult = getLatestAudiosUseCase()

            if (audiosResult.audios != null) {
                _homeScreenUiState.value = _homeScreenUiState.value.copy(
                    latestAudios = audiosResult.audios,
                    loadingLatestAudios = false
                )
            }

        }
    }
}