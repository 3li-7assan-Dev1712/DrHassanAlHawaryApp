package com.example.hassanalhawary.ui.screens.audio_list_sceen

import com.example.hassanalhawary.domain.model.Audio

sealed interface AudioListUiState {
    data object Loading : AudioListUiState
    data class Success(val audios: List<Audio>) : AudioListUiState
    data class Error(val message: String) : AudioListUiState
}