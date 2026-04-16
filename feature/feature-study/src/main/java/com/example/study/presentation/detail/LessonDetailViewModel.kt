package com.example.study.presentation.detail

import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.example.domain.module.Lesson
import com.example.study.domain.use_case.EnsureLessonFilesDownloadedUseCase
import com.example.study.domain.use_case.GetLessonByIdUseCase
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val lesson: Lesson? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isBuffering: Boolean = false,
    val isLoading: Boolean = true
)

@OptIn(UnstableApi::class)
@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getLessonByIdUseCase: GetLessonByIdUseCase,
    private val ensureLessonFilesDownloadedUseCase: EnsureLessonFilesDownloadedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    var mediaControllerFuture: ListenableFuture<MediaController>? = null
        set(value) {
            field = value
            value?.let { listenToController(it) }
        }

    init {
        viewModelScope.launch {
            ensureLessonFilesDownloadedUseCase(lessonId)
            getLessonByIdUseCase(lessonId).collectLatest { lesson ->
                _uiState.update { it.copy(lesson = lesson, isLoading = false) }
                // If the controller is ready, update its media item
                mediaControllerFuture?.await()?.let { controller ->
                    if (lesson != null && controller.currentMediaItem?.mediaId != lesson.id) {
                        setupPlayerWithLesson(controller, lesson)
                    }
                }
            }
        }
    }

    private fun setupPlayerWithLesson(controller: MediaController, lesson: Lesson) {
        val metadata = MediaMetadata.Builder().setTitle(lesson.title).build()
        val mediaItem = MediaItem.Builder()
            .setUri(lesson.audioUrl) // This will now be a local or remote path
            .setMediaId(lesson.id)
            .setMediaMetadata(metadata)
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
    }

    private fun listenToController(controllerFuture: ListenableFuture<MediaController>) {
        viewModelScope.launch {
            val controller = controllerFuture.await()

            // Initial setup if lesson is already loaded
            _uiState.value.lesson?.let { lesson ->
                if (controller.currentMediaItem?.mediaId != lesson.id) {
                    setupPlayerWithLesson(controller, lesson)
                } else {
                    if (controller.playbackState == Player.STATE_ENDED || controller.playbackState == Player.STATE_IDLE) {
                        controller.prepare()
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isPlaying = controller.isPlaying,
                    currentPosition = controller.currentPosition,
                    totalDuration = if (controller.duration > 0) controller.duration else it.totalDuration,
                    isBuffering = controller.playbackState == Player.STATE_BUFFERING
                )
            }

            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _uiState.update {
                        it.copy(isBuffering = playbackState == Player.STATE_BUFFERING)
                    }
                    if (playbackState == Player.STATE_READY) {
                        _uiState.update { it.copy(totalDuration = controller.duration) }
                    }
                }
            })

            // Progress tracking
            while (isActive) {
                val pos = controller.currentPosition
                if (_uiState.value.currentPosition != pos) {
                    _uiState.update { it.copy(currentPosition = pos) }
                }
                delay(300)
            }
        }
    }

    fun onPlayPauseClick() {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun onSeekForward() {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            val newPosition = (controller.currentPosition + 5000).coerceAtMost(controller.duration)
            controller.seekTo(newPosition)
            _uiState.update {
                it.copy(
                    currentPosition = newPosition
                )
            }
        }
    }

    fun onSeekBackward() {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            val newPosition = (controller.currentPosition - 5000).coerceAtLeast(0L)
            controller.seekTo(newPosition)
            _uiState.update {
                it.copy(
                    currentPosition = newPosition
                )
            }
        }
    }

    fun onSeekBarPositionChanged(newPosition: Long) {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            controller.seekTo(newPosition)
        }
    }

    override fun onCleared() {
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
