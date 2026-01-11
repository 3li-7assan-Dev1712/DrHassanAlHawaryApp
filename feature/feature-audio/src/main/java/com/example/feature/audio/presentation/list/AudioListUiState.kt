package com.example.feature.audio.presentation.list

import com.example.feature.audio.domain.model.Audio


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
