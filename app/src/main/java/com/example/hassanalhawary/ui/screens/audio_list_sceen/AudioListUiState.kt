package com.example.hassanalhawary.ui.screens.audio_list_sceen

import com.example.domain.module.Audio


sealed interface AudioListUiState {

    val searchQuery: String

    data class Loading(
        override val searchQuery: String = ""
    ) : AudioListUiState

    data class Success(
        val audios: List<Audio>,
        val displayedAudios: List<Audio> = audios,
        override val searchQuery: String
    ) : AudioListUiState

    data class Error(val message: String, override val searchQuery: String) : AudioListUiState


}
