package com.example.hassanalhawary.ui.screens.home_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.core.util.NetworkStatus
import com.example.hassanalhawary.domain.use_cases.GetAllAudiosUseCase
import com.example.hassanalhawary.domain.use_cases.GetCurrentNetworkStatusUseCase
import com.example.hassanalhawary.domain.use_cases.GetLatestArticlesUseCase
import com.example.hassanalhawary.domain.use_cases.GetWisdomOfTheDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val getLatestArticlesUseCase: GetLatestArticlesUseCase,
    private val getLatestAudiosUseCase: GetAllAudiosUseCase,
    private val getWisdomOfTheDayUseCase: GetWisdomOfTheDayUseCase,
    private val getCurrentNetworkStatusUseCase: GetCurrentNetworkStatusUseCase
) : ViewModel() {


    private val _homeScreenUiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState())
    val homeScreenUiState: StateFlow<HomeScreenUiState> = _homeScreenUiState.asStateFlow()

    init {
        loadLatestArticles()
        loadLatestAudios()
        loadWisdomOfTheDay()
        checkCurrentNetworkStatus()

    }

    private fun checkCurrentNetworkStatus() {

        getCurrentNetworkStatusUseCase() // This should return Flow<NetworkStatus>
            .onEach { currentNetworkStatus -> // .onEach is like .collect but better for chaining in ViewModel
                _homeScreenUiState.update { currentState ->
                    currentState.copy(
                        isInOfflineMode = currentNetworkStatus == NetworkStatus.Unavailable

                    )
                }
                Log.d("TAG", "Network Status Updated: ${!_homeScreenUiState.value.isInOfflineMode}")
            }
            .launchIn(viewModelScope) // Collect the flow within viewModelScope


    }

    private fun loadWisdomOfTheDay() {
        viewModelScope.launch {
            val wisdomResult = getWisdomOfTheDayUseCase()
            _homeScreenUiState.value = _homeScreenUiState.value.copy(
                wotdResult = wisdomResult,
                loadingWotd = false
            )
        }
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