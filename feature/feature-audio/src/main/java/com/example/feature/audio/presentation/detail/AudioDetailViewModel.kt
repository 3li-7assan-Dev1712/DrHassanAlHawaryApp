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
import com.example.domain.repository.DataStoreRepository
import com.example.domain.use_cases.audios.DownloadAudioUseCase
import com.example.domain.use_cases.audios.DownloadResult
import com.example.domain.use_cases.audios.GetAudioByUrlUseCase
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AudioDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAudioByUrlUseCase: GetAudioByUrlUseCase,
    private val downloadAudioUseCase: DownloadAudioUseCase,
    private val dataStoreRepository: DataStoreRepository,
) : ViewModel() {

    /** The running "save offline" download, so the inline card's (x) can cancel it. */
    private var downloadJob: Job? = null


    private val TAG = "AudioDetailViewModel"
    // The route's audio; the player pane beside the list on a tablet changes it (showAudio).
    private var audioUrl: String = savedStateHandle.get<String>("audioUrl") ?: ""
    private var audioTitle: String = savedStateHandle.get<String>("title") ?: ""

    private var detailsJob: Job? = null
    /** The listener added to the connected controller, removed before another is added. */
    private var playerListener: Pair<MediaController, Player.Listener>? = null

    private val _uiState = MutableStateFlow(AudioDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var currentAudio: Audio? = null

    /**
     * The "restart from the beginning" behavior below is only meant for a fresh visit to
     * this screen, not every controller reconnect. [mediaControllerFuture] gets reassigned
     * (and [listenToController] re-runs) on every config change too, because the Composable's
     * `remember { MediaController.Builder(...).buildAsync() }` doesn't survive Activity
     * recreation - only the ViewModel does. Gating on this flag stops rotation from
     * resetting in-progress playback to 0.
     */
    private var isFirstControllerConnection = true

    /** Cancelled and replaced on every reconnect, so a rotation doesn't leave the
     * previous connection's progress-polling loop running alongside the new one. */
    private var controllerJob: Job? = null

    var mediaControllerFuture: ListenableFuture<MediaController>? = null
        set(value) {
            field = value
            value?.let {
                listenToController(it)

            }
        }


    init {
        viewModelScope.launch {
            val saved = dataStoreRepository.playbackSpeed().first()
            _uiState.update { it.copy(playbackSpeed = saved.takeIf { s -> s in SPEEDS } ?: 1f) }
            mediaControllerFuture?.await()?.setPlaybackSpeed(_uiState.value.playbackSpeed)
        }
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

    /**
     * Shows another audio (nothing if it is already shown): the player pane beside the audio
     * list on a tablet follows the selection. Like opening the player screen for it, the new
     * item is loaded into the controller, not started.
     */
    fun showAudio(title: String, url: String) {
        if (url == audioUrl) return
        audioUrl = url
        audioTitle = title
        downloadJob?.cancel()
        downloadJob = null
        currentAudio = null
        _uiState.update { AudioDetailUiState(audioUrl = url, title = title, playbackSpeed = it.playbackSpeed) }
        loadAudioDetails()
        isFirstControllerConnection = true
        // Only a connected controller is listened to again. A connection still pending loads
        // this audio when it completes (listenToController reads it after await()), and
        // re-listening would cancel it: await() cancels the future when its coroutine is
        // cancelled, which releases the controller and leaves the player dead (the tablet's
        // player pane opens with its first selection while the controller is connecting).
        mediaControllerFuture?.takeIf { it.isDone }?.let(::listenToController)
    }

    private fun loadAudioDetails() {
        detailsJob?.cancel()
        detailsJob = viewModelScope.launch {
            getAudioByUrlUseCase(audioUrl).collect { audio ->
                Log.d(TAG, "loadAudioDetails: $audio")
                currentAudio = audio
                if (audio != null) {
                    Log.d(TAG, "loadAudioDetails: local file path ${audio.localFilePath}")
                    _uiState.update {
                        it.copy(
                            isDownloaded = audio.isDownloaded,
                            isFavorite = audio.isFavorite,
                            // The share card's chip is keyed off the category id ("khotab", ...); `type` is
                            // just "audio" for every item and used to leak onto the card as a label.
                            category = audio.categoryId?.takeIf { id -> id.isNotBlank() },
                            localFilePath = audio.localFilePath.takeIf { audio.isDownloaded },
                            isLoadingDetails = false
                        )
                    }
                } else {
                    // Not (yet) cached locally - e.g. opened straight from search results
                    // without ever browsing the audio list, so Room has no matching row.
                    // Streaming still works off audioUrl/title from nav args, so stop
                    // blocking the UI behind the loading spinner.
                    _uiState.update { it.copy(isLoadingDetails = false) }
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

    /** "السرعة": 1× → 1.25× → 1.5× → 2× → 0.75× → 1×, saved for later playback. */
    fun onCycleSpeed() {
        val current = SPEEDS.indexOf(_uiState.value.playbackSpeed).coerceAtLeast(0)
        val next = SPEEDS[(current + 1) % SPEEDS.size]
        _uiState.update { it.copy(playbackSpeed = next) }
        viewModelScope.launch {
            dataStoreRepository.setPlaybackSpeed(next)
            mediaControllerFuture?.await()?.setPlaybackSpeed(next)
        }
    }

    /** The inline download card's (x): stops the running download. Playback is unaffected. */
    fun onCancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _uiState.update { it.copy(isDownloading = false, downloadProgress = 0f) }
    }

    fun onToggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun onDownloadClicked() {
        val audioToDownload = currentAudio ?: return

        if (audioToDownload.isDownloaded || downloadJob?.isActive == true) return

        startDownload(audioToDownload)
    }

    private fun startDownload(audio: Audio) {
        _uiState.update { it.copy(isDownloading = true, downloadProgress = 0f) }
        downloadJob = viewModelScope.launch {
            downloadAudioUseCase(audio).collect { result ->
                when (result) {
                    is DownloadResult.Progress -> {
                        _uiState.update { it.copy(downloadProgress = result.percentage.toFloat()) }
                    }

                    is DownloadResult.Success -> {
                        _uiState.update { it.copy(isDownloaded = true, isDownloading = false, downloadProgress = 100f) }
                        // Room's getAudioByUrl flow (already collected in loadAudioDetails())
                        // picks up this upsert on its own - no need to re-subscribe here.

                        // Switch the player to the newly downloaded file
                        switchToLocalPlayback(result.localPath)
                    }

                    is DownloadResult.Error -> {
                        Log.e("AudioDetailVM", "Download error: ${result.message}")
                        _uiState.update { it.copy(isDownloading = false, downloadProgress = 0f) }
                        // Optionally set an error state here to show a toast
                    }
                }
            }
        }
    }


    private fun listenToController(controllerFuture: ListenableFuture<MediaController>) {
        controllerJob?.cancel()
        controllerJob = viewModelScope.launch {
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
            } else if (isFirstControllerConnection) {
                Log.d("AudioVM", "Same audio, fresh screen visit. Restarting from the beginning.")
                // Reset to the beginning as requested - but only for a genuinely fresh
                // visit to this screen, not a reconnect from a rotation (see
                // isFirstControllerConnection's doc).
                controller.seekTo(0L)

                // If the audio had previously finished, it needs to be prepared again
                if (controller.playbackState == Player.STATE_ENDED || controller.playbackState == Player.STATE_IDLE) {
                    controller.prepare()
                }
            } else {
                Log.d("AudioVM", "Same audio, reconnecting (e.g. rotation). Keeping playback position.")
            }
            isFirstControllerConnection = false
            controller.setPlaybackSpeed(_uiState.value.playbackSpeed)


            _uiState.update {
                it.copy(
                    isPlaying = controller.isPlaying,
                    currentPositionMillis = controller.currentPosition,
                    totalDurationMillis = if (controller.duration > 0) controller.duration else it.totalDurationMillis,
                    isBuffering = if (currentAudio?.isDownloaded == true) false
                    else controller.playbackState == Player.STATE_BUFFERING
                )
            }

            // 3. Add Listener for future changes (replacing the one a previous connection, or
            // the previous audio in the tablet's player pane, added)
            playerListener?.let { (previous, listener) -> previous.removeListener(listener) }
            val listener = object : Player.Listener {
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
            }
            controller.addListener(listener)
            playerListener = controller to listener

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

    companion object {
        /** The speed button's cycle, in order. */
        val SPEEDS = listOf(1f, 1.25f, 1.5f, 2f, 0.75f)
    }
}
