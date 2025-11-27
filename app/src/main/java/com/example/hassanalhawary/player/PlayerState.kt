package com.example.hassanalhawary.player

// Data class to represent the player's state
data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val currentSpeed: Float = 1.0f,
    val isBuffering: Boolean = false,
    val error: String? = null,
    val currentMediaId: String? = null
)