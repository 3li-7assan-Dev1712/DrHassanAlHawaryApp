package com.example.hassanalhawary.ui.screens.audio_detail_screen

data class AudioDetailUiState(
    val audioUrl: String? = null,
    val title: String = "Loading...",
    val description: String? = null,

    val isPlaying: Boolean = false,
    val currentPositionMillis: Long = 0L,
    val bufferedPositionMillis: Long = 0L, // For showing buffered progress on seek bar
    val totalDurationMillis: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val availablePlaybackSpeeds: List<Float> = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f),

    val isBuffering: Boolean = false, // To show a specific loading state for player
    val isPlaybackEnded: Boolean = false,
    val playbackErrorMessage: String? = null,

    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,

    val isLoadingDetails: Boolean = true // Loading metadata for the screen itself
)
