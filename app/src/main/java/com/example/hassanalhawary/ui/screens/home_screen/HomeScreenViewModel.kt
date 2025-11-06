package com.example.hassanalhawary.ui.screens.home_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.core.util.NetworkMessageEvent
import com.example.hassanalhawary.core.util.NetworkStatus
import com.example.hassanalhawary.domain.use_cases.GetAllAudiosUseCase
import com.example.hassanalhawary.domain.use_cases.GetCurrentNetworkStatusUseCase
import com.example.hassanalhawary.domain.use_cases.GetLatestArticlesUseCase
import com.example.hassanalhawary.domain.use_cases.GetWisdomOfTheDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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


    // Channel to show one-time events of network connectivity changes
    private val _networkMessageEventChannel = Channel<NetworkMessageEvent>()
    val networkMessageEventFlow = _networkMessageEventChannel.receiveAsFlow()


    // Keep track of whether the *first* network state has been processed
    private var isInitialNetworkStatusProcessed = false
    private var lastKnownNetworkStatus: NetworkStatus? = null // Store the last status processed

    init {
        loadLatestArticles()
        loadLatestAudios()
        loadWisdomOfTheDay()
        checkCurrentNetworkStatus()

    }

    private fun checkCurrentNetworkStatus() {

        getCurrentNetworkStatusUseCase() // This should return Flow<NetworkStatus>
            .distinctUntilChanged() // Only react if the status truly changes
            .onEach { currentStatus ->
                val isNowOffline = currentStatus == NetworkStatus.Unavailable

                // Update the persistent network status in UI state
                _homeScreenUiState.update { it.copy(isInOfflineMode = isNowOffline) }


                if (!isInitialNetworkStatusProcessed) {
                    // This is the first status update since ViewModel init or flow start
                    // We just record it and don't show any transition messages yet
                    isInitialNetworkStatusProcessed = true
                } else {
                    if (lastKnownNetworkStatus == NetworkStatus.Available && currentStatus == NetworkStatus.Unavailable) {
                        // Was online, now offline
                        _networkMessageEventChannel.send(NetworkMessageEvent.WentOffline)
                        Log.d("NetworkChange", "Went OFFLINE")
                    } else if (lastKnownNetworkStatus == NetworkStatus.Unavailable && currentStatus == NetworkStatus.Available) {
                        // Was offline, now online
                        _networkMessageEventChannel.send(NetworkMessageEvent.BackOnline)
                        Log.d("NetworkChange", "Went ONLINE (Back Online)")
                    }

                }
                lastKnownNetworkStatus = currentStatus // Update the last known status
                Log.d(
                    "NetworkChange",
                    "Current Status: $currentStatus, Last Known: $lastKnownNetworkStatus, Initial Processed: $isInitialNetworkStatusProcessed"
                )


            }
            .launchIn(viewModelScope)


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

            getLatestAudiosUseCase()
                .onEach { audioFromDb ->
                    _homeScreenUiState.update {
                        it.copy(
                            latestAudios = audioFromDb,
                            loadingLatestAudios = false,
                            audioErrorMessage = null
                        )
                    }
                }
                .launchIn(viewModelScope)
        }


    }

        /*if (audiosResult.audios != null) {
            _homeScreenUiState.value = _homeScreenUiState.value.copy(
                latestAudios = audiosResult.audios,
                loadingLatestAudios = false
            )
        } else if (audiosResult.errorMessage != null) {
            _homeScreenUiState.value = _homeScreenUiState.value.copy(
                audioErrorMessage = audiosResult.errorMessage,
                loadingLatestAudios = false
            )

            Log.d("HomeScreenViewModel", audiosResult.errorMessage)
        }

    }*/

}