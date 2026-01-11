package com.example.feature.audio.player

import com.example.feature.audio.domain.model.Audio
import kotlinx.coroutines.flow.StateFlow

// Represents the state of the player at any given time
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

// The interface defining the contract for our manager
interface AudioPlayerManager {
    val playerState: StateFlow<PlayerState>

    fun setPlaylist(audios: List<Audio>)
    fun playAudio(audio: Audio)
    fun resume()
    fun pause()
    fun seekTo(position: Long)
    fun playNext()
    fun playPrevious()
    fun stop()
}