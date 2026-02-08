package com.example.admin.ui.playlist

import com.example.domain.module.Playlist

sealed interface AdminPlaylistUiState {
    data class Success(val playlists: List<Playlist>) : AdminPlaylistUiState
    data class Error(val message: String) : AdminPlaylistUiState
    object Loading : AdminPlaylistUiState
}