package com.example.study.presentation.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.module.Lesson
import com.example.study.domain.use_case.GetLessonByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val lesson: Lesson? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    getLessonById: GetLessonByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private var player: ExoPlayer? = null
    private var progressTracker: Job? = null

    init {
        val lessonId = savedStateHandle.get<String>("lessonId")
        if (lessonId != null) {
            viewModelScope.launch {
                getLessonById(lessonId).collect { lesson ->

                    if (lesson == null)
                        throw NullPointerException("Lesson is null")
                    else {
                        _uiState.value = _uiState.value.copy(lesson = lesson, isLoading = false)
                        setupPlayer(lesson.audioUrl)
                    }
                }
            }
        } else {
            // Handle error case where lessonId is null
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun getLessonById(id: String): Lesson {
        // This is a placeholder. Fetch the real lesson from your repository.
        return Lesson(
            id = id,
            title = "Understanding the Fundamentals of Iman",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
            duration = "04:32"
        )
    }

    private fun setupPlayer(audioUrl: String) {
        player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _uiState.value = _uiState.value.copy(totalDuration = player?.duration ?: 0L)
                    }
                }
            })
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTracker = viewModelScope.launch {
            while (true) {
                _uiState.value =
                    _uiState.value.copy(currentPosition = player?.currentPosition ?: 0L)
                delay(1000) // Update every second
            }
        }
    }

    private fun stopProgressTracker() {
        progressTracker?.cancel()
    }

    fun onPlayPauseClick() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun onSeekForward() {
        val newPosition = (player?.currentPosition ?: 0L) + 5000
        player?.seekTo(newPosition.coerceAtMost(player?.duration ?: 0L))
    }

    fun onSeekBackward() {
        val newPosition = (player?.currentPosition ?: 0L) - 5000
        player?.seekTo(newPosition.coerceAtLeast(0L))
    }

    fun onSeekBarPositionChanged(newPosition: Long) {
        player?.seekTo(newPosition)
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
        stopProgressTracker()
    }
}
