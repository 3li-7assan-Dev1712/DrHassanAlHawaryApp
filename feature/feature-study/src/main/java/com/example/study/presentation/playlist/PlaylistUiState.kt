package com.example.study.presentation.playlist

import com.example.domain.module.Playlist

sealed interface PlaylistUiState {
    data class Success(val playlists: List<Playlist>) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
    object Loading : PlaylistUiState
}