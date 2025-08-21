package com.example.hassanalhawary.ui.screens.audio_detail_screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.hassanalhawary.domain.model.AudioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AudioDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val audioPlayerController: AudioPlayerController

) : ViewModel() {

    private var playerListener: Player.Listener? = null
    private var progressUpdateJob: Job? = null

    private var _player: ExoPlayer? = null
    val player: ExoPlayer? get() = _player

    private val audioUrl: String = savedStateHandle.get<String>("audioUrl") ?: ""
    private val audioTitle: String = savedStateHandle.get<String>("title") ?: ""

    private val _uiState = MutableStateFlow(AudioDetailUiState())
    val uiState = _uiState.asStateFlow()


    init {
        if (audioUrl.isNotBlank()) {
            _uiState.update { it.copy(audioUrl = audioUrl) }
            initializePlayer(audioUrl)
            addPlayerListener()
            loadAudioDetails()
             observePlayerState()
        } else {
            _uiState.update { it.copy(playbackErrorMessage = "Audio ID missing.") }
        }
    }

    private fun loadAudioDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetails = true) }

            Log.d("AudioDetailViewModel", "loadAudioDetails: the audio url is $audioUrl")
            // For now, using placeholder data after a delay:
            kotlinx.coroutines.delay(1000)
            _uiState.update {
                it.copy(
                    title = audioTitle,
                    description = "Audio Title with: \n" +
                            audioTitle,
                    totalDurationMillis = 2717000L, // approx 45:17
                    isLoadingDetails = false,
                    isFavorite = true // Sample
                )
            }
        }
    }

    private fun initializePlayer(url: String) {
        if (_player == null) {
            _player = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
//                Looper.prepare()
                prepare()
                playWhenReady = true

            // Asynchronously prepares the player
                // playWhenReady = true, Start playing as soon as prepared
            }
            addPlayerListener()
        } else {
            // Player already exists, perhaps update media item if URL could change
            // For now, we assume URL is fixed for the ViewModel lifecycle
        }
    }

    private fun addPlayerListener() {
        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressUpdater()
                } else {
                    stopProgressUpdater()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        isPlaybackEnded = playbackState == Player.STATE_ENDED
                    )
                }
                if (playbackState == Player.STATE_READY) {
                    _uiState.update { it.copy(totalDurationMillis = _player?.duration ?: 0L) }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _uiState.update {
                    it.copy(
                        playbackErrorMessage = error.message ?: "Unknown player error"
                    )
                }
            }
        }.also { _player?.addListener(it) }
    }

    private fun startProgressUpdater() {
        stopProgressUpdater() // Ensure only one updater is running
        progressUpdateJob = viewModelScope.launch {
            while (isActive && _player?.isPlaying == true) {
                _uiState.update {
                    it.copy(
                        currentPositionMillis = _player?.currentPosition ?: 0L,
                        bufferedPositionMillis = _player?.bufferedPosition ?: 0L
                    )
                }
                delay(300) // Update interval (e.g., every 300ms)
            }
        }
    }

    private fun stopProgressUpdater() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    // Placeholder for fetching actual audio metadata
    private fun loadAudioDetailsPlaceholder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetails = true) }
            kotlinx.coroutines.delay(1000) // Simulate network delay
            _uiState.update {
                it.copy(
                    // You'd fetch these details based on the audioUrl or an ID
                    title = "The Importance of Sincerity",
                    description = "An in-depth exploration of sincerity...",
                    // totalDurationMillis will be updated by the player once ready
                    isLoadingDetails = false,
                    isFavorite = false // Sample
                )
            }
        }
    }

    // --- Player Event Handlers ---
    fun onPlayPauseToggle() {
        // audioPlayerController.playPause()
        _player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                if (it.playbackState == Player.STATE_ENDED) {
                    it.seekTo(0) // Restart if ended
                }
                it.play()
            }
        }
        _uiState.update { it.copy(isPlaying = !it.isPlaying) } // Simplified
    }

    fun onSeek(positionFraction: Float) {
        val newPosition = (_uiState.value.totalDurationMillis * positionFraction).toLong()
        _player?.seekTo(newPosition.coerceIn(0, _player?.duration ?: 0L))
        // audioPlayerController.seekTo(newPosition)
        _uiState.update { it.copy(currentPositionMillis = newPosition) } // Simplified
    }

    fun onForward(seconds: Int) {
        val current = _uiState.value.currentPositionMillis
        val duration = _uiState.value.totalDurationMillis
        val newPosition = (current + seconds * 1000).coerceAtMost(duration)
        _player?.let {
            val newPosition =
                (it.currentPosition + seconds * 1000).coerceAtMost(it.duration.coerceAtLeast(0L))
            it.seekTo(newPosition)
        }
        _uiState.update { it.copy(currentPositionMillis = newPosition) }
    }

    fun onRewind(seconds: Int) {
        val current = _uiState.value.currentPositionMillis
        val newPosition = (current - seconds * 1000).coerceAtLeast(0L)
        _player?.let {
            val newPosition = (it.currentPosition - seconds * 1000).coerceAtLeast(0L)
            it.seekTo(newPosition)
        }
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