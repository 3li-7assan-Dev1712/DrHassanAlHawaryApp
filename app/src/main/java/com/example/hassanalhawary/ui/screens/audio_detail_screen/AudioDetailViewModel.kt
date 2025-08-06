package com.example.hassanalhawary.ui.screens.audio_detail_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.model.AudioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AudioDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val audioPlayerController: AudioPlayerController,

) : ViewModel() {

    private val audioId: String = savedStateHandle.get<String>("audioId") ?: ""

    private val _uiState = MutableStateFlow(AudioDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (audioId.isNotBlank()) {
            _uiState.update { it.copy(audioId = audioId) }
            loadLessonDetails()
            // Initialize player state observer from your audio player controller
            // observePlayerState()
        } else {
            _uiState.update { it.copy(playbackErrorMessage = "Audio ID missing.") }
        }
    }

    private fun loadLessonDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetails = true) }

            // For now, using placeholder data after a delay:
            kotlinx.coroutines.delay(1000)
            _uiState.update {
                it.copy(
                    title = "The Importance of Sincerity in Islam",
                    description = "An in-depth exploration of sincerity (Ikhlas) and its pivotal role in all acts of worship. Sheikh Hassan Al-Hawary discusses its meaning, virtues, and practical ways to cultivate it.",
                    totalDurationMillis = 2717000L, // approx 45:17
                    isLoadingDetails = false,
                    isFavorite = true // Sample
                )
            }
        }
    }

    // --- Player Event Handlers ---
    fun onPlayPauseToggle() {
        // audioPlayerController.playPause()
        _uiState.update { it.copy(isPlaying = !it.isPlaying) } // Simplified
    }

    fun onSeek(positionFraction: Float) {
        val newPosition = (_uiState.value.totalDurationMillis * positionFraction).toLong()
        // audioPlayerController.seekTo(newPosition)
        _uiState.update { it.copy(currentPositionMillis = newPosition) } // Simplified
    }

    fun onForward(seconds: Int) {
        val current = _uiState.value.currentPositionMillis
        val duration = _uiState.value.totalDurationMillis
        val newPosition = (current + seconds * 1000).coerceAtMost(duration)
        // audioPlayerController.seekTo(newPosition)
        _uiState.update { it.copy(currentPositionMillis = newPosition) }
    }

    fun onRewind(seconds: Int) {
        val current = _uiState.value.currentPositionMillis
        val newPosition = (current - seconds * 1000).coerceAtLeast(0L)
        // audioPlayerController.seekTo(newPosition)
        _uiState.update { it.copy(currentPositionMillis = newPosition) }
    }

    fun onChangeSpeed(speed: Float) {
        // audioPlayerController.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun onToggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        // Call repository to save favorite state
    }

    fun onDownloadClicked() {
        // Handle download logic
    }

//     --- Observe Player State (Example) ---
     private fun observePlayerState() {
         viewModelScope.launch {
             audioPlayerController.playerStateFlow.collect { playerState ->
                 _uiState.update {
                     it.copy(
                         isPlaying = playerState.isPlaying,
                         currentPositionMillis = playerState.currentPosition,
                         bufferedPositionMillis = playerState.bufferedPosition,
                         totalDurationMillis = playerState.totalDuration,
                         isBuffering = playerState.isBuffering,
                         playbackErrorMessage = playerState.error
                     )
                 }
             }
         }
     }

    override fun onCleared() {
         audioPlayerController.release()
        super.onCleared()
    }
}