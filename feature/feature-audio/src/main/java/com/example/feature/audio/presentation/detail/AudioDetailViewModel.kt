package com.example.feature.audio.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AudioDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    private val audioUrl: String = savedStateHandle.get<String>("audioUrl") ?: ""
    private val audioTitle: String = savedStateHandle.get<String>("title") ?: ""

    private val _uiState = MutableStateFlow(AudioDetailUiState())
    val uiState = _uiState.asStateFlow()

    var mediaControllerFuture: ListenableFuture<MediaController>? = null
        set(value) {
            field = value
            value?.let {
                listenToController(it)

            }
        }


    init {
        Log.d("Ali 1712", "audio url is $audioUrl: ")
        if (audioUrl.isNotBlank()) {
            _uiState.update { it.copy(audioUrl = audioUrl, title = audioTitle) }

        } else {
            _uiState.update { it.copy(playbackErrorMessage = "Audio ID missing.") }
        }
    }


    fun onPlayPauseToggle() {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch

            if (controller.isPlaying) {
                controller.pause()
                return@launch
            }

            if (controller.currentMediaItem != null) {
                controller.play()
                return@launch
            }

            val metadata = MediaMetadata.Builder()
                .setTitle(audioTitle)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaMetadata(metadata)
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    fun onSeek(positionFraction: Float) {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            val duration = controller.duration
            if (duration > 0) {
                controller.seekTo((duration * positionFraction).toLong())
            }
        }
    }

    fun onForward(seconds: Int) {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            val newPosition =
                (controller.currentPosition + seconds * 1000).coerceAtMost(controller.duration)
            controller.seekTo(newPosition)
        }
    }

    fun onRewind(seconds: Int) {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            val newPosition = (controller.currentPosition - seconds * 1000).coerceAtLeast(0L)
            controller.seekTo(newPosition)
        }
    }

    fun onChangeSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun onToggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun onDownloadClicked() {
        // Handle download logic
    }


    private fun listenToController(controllerFuture: ListenableFuture<MediaController>) {
        viewModelScope.launch {
            val controller = controllerFuture.await()


            if (controller.currentMediaItem?.mediaId != audioUrl) {
                Log.d("AudioVM", "Controller has wrong audio. Setting new media item: $audioUrl")
                val metadata = MediaMetadata.Builder()
                    .setTitle(audioTitle)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaId(audioUrl)
                    .setMediaMetadata(metadata)
                    .build()
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
            } else {
                Log.d("AudioVM", "Controller already has correct audio. Ensuring it plays.")
                controller.play()
            }




            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _uiState.update {
                        it.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            isPlaybackEnded = playbackState == Player.STATE_ENDED
                        )
                    }
                    if (playbackState == Player.STATE_READY) {
                        _uiState.update {
                            it.copy(
                                totalDurationMillis = controller.duration,
                                isLoadingDetails = false
                            )
                        }
                    }
                }
            })

            // Progress updater
            launch {
                while (isActive) {
                    if (controller.playbackState != Player.STATE_IDLE && controller.playbackState != Player.STATE_ENDED) {
                        _uiState.update {
                            it.copy(
                                currentPositionMillis = controller.currentPosition,
                            )
                        }
                    }
                    delay(300)
                }
            }
        }
    }


    override fun onCleared() {
//        audioPlayerController.release()
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onCleared()
    }
}