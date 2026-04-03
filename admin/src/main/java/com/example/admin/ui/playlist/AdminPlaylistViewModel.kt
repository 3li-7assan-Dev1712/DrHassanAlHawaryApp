package com.example.admin.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.DeletePlaylistUseCase
import com.example.domain.use_cases.study.GetRemotePlaylistsForLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPlaylistViewModel @Inject constructor(
    private val getPlaylistsForLevelUseCase: GetRemotePlaylistsForLevelUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "PlaylistViewModel"

    val levelId: String? = savedStateHandle["levelId"]

    private val _uiState = MutableStateFlow<AdminPlaylistUiState>(AdminPlaylistUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.value = AdminPlaylistUiState.Loading
            try {
                if (levelId != null) {
                    val playlists = getPlaylistsForLevelUseCase(levelId)
                    if (playlists.isNotEmpty()) _uiState.value =
                        AdminPlaylistUiState.Success(playlists)
                    else _uiState.value = AdminPlaylistUiState.Error("No playlists found")
                } else {
                    _uiState.value = AdminPlaylistUiState.Error("levelId is null")
                }
            } catch (e: Exception) {
                _uiState.value = AdminPlaylistUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            deletePlaylistUseCase(playlistId).onSuccess {
                loadPlaylists()
            }.onFailure {
                // Handle error
            }
        }
    }
}
