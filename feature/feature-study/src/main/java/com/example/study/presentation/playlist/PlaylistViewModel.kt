package com.example.study.presentation.playlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.use_case.GetPlaylistsForLevelUseCase
import com.example.study.domain.use_case.SyncPlaylistsForLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    getPlaylistsForLevelUseCase: GetPlaylistsForLevelUseCase,
    val syncPlaylistsUseCase: SyncPlaylistsForLevelUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "PlaylistViewModel"
//    private val level: Int = checkNotNull(savedStateHandle["level"])

    val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)

    val uiState = _uiState.asStateFlow()

    /*  val uiState: StateFlow<PlaylistUiState> =
          getPlaylistsForLevelUseCase(1)
              .map { playlists ->
                  PlaylistUiState.Success(playlists ?: emptyList()) as PlaylistUiState
              }
              .catch { e -> emit(PlaylistUiState.Error(e.message ?: "An unexpected error occurred")) }
              .stateIn(
                  scope = viewModelScope,
                  started = SharingStarted.WhileSubscribed(5000),
                  initialValue = PlaylistUiState.Loading
              )*/

    init {
        viewModelScope.launch {
            try {
                getPlaylistsForLevelUseCase(1).collect { playlists ->

                    if (!playlists.isNullOrEmpty())
                        _uiState.value = PlaylistUiState.Success(playlists)
                    else
                        _uiState.value = PlaylistUiState.Error("No playlists found")
                }

            } catch (e: Exception) {
                Log.d(TAG, "onRefresh: ${e.message}")
            }
        }
//        onRefresh()
    }

    fun onRefresh() {
        viewModelScope.launch {
            try {
                syncPlaylistsUseCase(1)
            } catch (e: Exception) {
                Log.d(TAG, "onRefresh: ${e.message}")
                // The UI will continue to show cached data.
                // You might want to show a transient error to the user (e.g., a Snackbar).
            }
        }
    }
}
