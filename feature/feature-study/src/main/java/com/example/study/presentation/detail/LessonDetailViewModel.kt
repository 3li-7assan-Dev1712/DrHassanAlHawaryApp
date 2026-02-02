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
import com.example.study.domain.use_case.GetLessonByIdUseCase
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
    private val getLessonByIdUseCase: GetLessonByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    var mediaControllerFuture: ListenableFuture<MediaController>? = null
        set(value) {
            field = value
            value?.let { listenToController(it) }
        }

    init {
        val lessonId = savedStateHandle.get<String>("lessonId")
        if (lessonId != null) {
            viewModelScope.launch {
                getLessonByIdUseCase(lessonId).collect { lesson ->

                    _uiState.value = _uiState.value.copy(lesson = lesson, isLoading = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }

    }

    // This is a placeholder. Fetch the real lesson from your repository.
    private fun getLessonById(id: String): Lesson {
        return Lesson(
            id = id,
            title = "Understanding the Fundamentals of Iman",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
            duration = "04:32"
        )
    }

    private fun listenToController(controllerFuture: ListenableFuture<MediaController>) {
        viewModelScope.launch {
            val controller = controllerFuture.await()

            // Set the media item if it's not already the correct one
            val lesson = _uiState.value.lesson
            if (lesson != null && controller.currentMediaItem?.mediaId != lesson.id) {
                val metadata = MediaMetadata.Builder().setTitle(lesson.title).build()
                val mediaItem = MediaItem.Builder()
                    .setUri(lesson.audioUrl)
                    .setMediaId(lesson.id)
                    .setMediaMetadata(metadata)
                    .build()
                controller.setMediaItem(mediaItem)
                controller.prepare()
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
                _uiState.update { it.copy(currentPosition = controller.currentPosition) }
                delay(1000)
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
        }
    }

    fun onSeekBackward() {
        viewModelScope.launch {
            val controller = mediaControllerFuture?.await() ?: return@launch
            val newPosition = (controller.currentPosition - 5000).coerceAtLeast(0L)
            controller.seekTo(newPosition)
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
