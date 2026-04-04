package com.example.feature.audio.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.example.domain.module.Audio
import com.example.domain.use_cases.audios.DownloadAudioUseCase
import com.example.domain.use_cases.audios.DownloadResult
import com.example.domain.use_cases.audios.GetAudioByUrlUseCase
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
    private val getAudioByUrlUseCase: GetAudioByUrlUseCase,
    private val downloadAudioUseCase: DownloadAudioUseCase
) : ViewModel() {


    private val TAG = "AudioDetailViewModel"
    private val audioUrl: String = savedStateHandle.get<String>("audioUrl") ?: ""
    private val audioTitle: String = savedStateHandle.get<String>("title") ?: ""

    private val _uiState = MutableStateFlow(AudioDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var currentAudio: Audio? = null

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
            loadAudioDetails()
        } else {
            _uiState.update {
                it.copy(
                    playbackErrorMessage = "Audio ID missing.",
                    isLoadingDetails = false
                )
            }
        }
    }

    private fun loadAudioDetails() {
        viewModelScope.launch {
            getAudioByUrlUseCase(audioUrl).collect { audio ->
                Log.d(TAG, "loadAudioDetails: $audio")
                if (audio != null) {
                    Log.d(TAG, "loadAudioDetails: local file path ${audio.localFilePath}")
                    currentAudio = audio
                    _uiState.update {
                        it.copy(
                            isDownloaded = audio.isDownloaded,
                            isFavorite = audio.isFavorite,
                            isLoadingDetails = false
                        )
                    }
                }
            }
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

            // Use local file path if downloaded, else use remote URL
            val uriToPlay =
                if (currentAudio?.isDownloaded == true && currentAudio?.localFilePath != null) {
                    currentAudio!!.localFilePath!!

                } else {
                    audioUrl
                }

            Log.d("AudioDetailViewModel", "onPlayPauseToggle: uri to play : $uriToPlay")
            val mediaItem = MediaItem.Builder()
                .setUri(uriToPlay)
                .setMediaMetadata(metadata)
                .setMediaId(audioUrl) // keep original ID for reference
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    fun onSeek(newPosition: Long) {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            controller.seekTo(newPosition)
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
        val audioToDownload = currentAudio ?: return

        if (audioToDownload.isDownloaded) return

        viewModelScope.launch {
            downloadAudioUseCase(audioToDownload).collect { result ->
                when (result) {
                    is DownloadResult.Progress -> {
                        _uiState.update { it.copy(downloadProgress = result.percentage.toFloat()) }
                    }

                    is DownloadResult.Success -> {
                        _uiState.update { it.copy(isDownloaded = true, downloadProgress = 100f) }
                        loadAudioDetails()

                        // Switch the player to the newly downloaded file
                        switchToLocalPlayback(result.localPath)
                    }

                    is DownloadResult.Error -> {
                        Log.e("AudioDetailVM", "Download error: ${result.message}")
                        // Optionally set an error state here to show a toast
                    }
                }
            }
        }
    }


    private fun listenToController(controllerFuture: ListenableFuture<MediaController>) {
        viewModelScope.launch {
            val controller = controllerFuture.await()

            val uriToPlay =
                if (currentAudio?.isDownloaded == true && currentAudio?.localFilePath != null) {
                    currentAudio!!.localFilePath!!
                } else {
                    audioUrl
                }

            // 1. Handle Media Item and Force Restart
            if (controller.currentMediaItem?.mediaId != audioUrl) {
                Log.d("AudioVM", "Controller has wrong audio. Setting new media item.")
                val metadata = MediaMetadata.Builder().setTitle(audioTitle).build()
                val mediaItem = MediaItem.Builder()
                    .setUri(uriToPlay)
                    .setMediaId(audioUrl)
                    .setMediaMetadata(metadata)
                    .build()
                controller.setMediaItem(mediaItem)
                controller.prepare()
            } else {
                Log.d("AudioVM", "Same audio. Restarting from the beginning.")
                // Reset to the beginning as requested
                controller.seekTo(0L)

                // If the audio had previously finished, it needs to be prepared again
                if (controller.playbackState == Player.STATE_ENDED || controller.playbackState == Player.STATE_IDLE) {
                    controller.prepare()
                }
            }


            _uiState.update {
                it.copy(
                    isPlaying = controller.isPlaying,
                    currentPositionMillis = controller.currentPosition,
                    totalDurationMillis = if (controller.duration > 0) controller.duration else it.totalDurationMillis,
                    isBuffering = if (currentAudio?.isDownloaded == true) false
                    else controller.playbackState == Player.STATE_BUFFERING
                )
            }

            // 3. Add Listener for future changes
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _uiState.update {
                        it.copy(
                            isBuffering = if (currentAudio?.isDownloaded == true) false
                            else controller.playbackState == Player.STATE_BUFFERING,
                            isPlaybackEnded = playbackState == Player.STATE_ENDED
                        )
                    }
                    if (playbackState == Player.STATE_READY) {
                        _uiState.update {
                            it.copy(totalDurationMillis = controller.duration)
                        }
                    }
                }
            })

            // 4. Progress updater loop
            while (isActive) {
                val pos = controller.currentPosition
                if (_uiState.value.currentPositionMillis != pos) {
                    _uiState.update { it.copy(currentPositionMillis = pos) }
                }
                delay(300)
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

    private fun switchToLocalPlayback(localFilePath: String) {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch

            // 1. Save the current playback state and position
            val wasPlaying = controller.isPlaying
            val currentPosition = controller.currentPosition

            // 2. Create new MediaItem with the LOCAL file path, but KEEP the original ID
            val metadata = MediaMetadata.Builder()
                .setTitle(audioTitle)
                .build()

            val localMediaItem = MediaItem.Builder()
                .setUri(localFilePath)
                .setMediaId(audioUrl) // Must remain audioUrl so listenToController doesn't reset it
                .setMediaMetadata(metadata)
                .build()

            Log.d(TAG, "Switching to local playback: $localFilePath at position $currentPosition")

            // 3. Swap the item, seek to the exact same position, and prepare
            controller.setMediaItem(localMediaItem)
            controller.seekTo(currentPosition)
            controller.prepare()

            // 4. Resume playing if it was playing before the swap
            if (wasPlaying) {
                controller.play()
            }
        }
    }
}
