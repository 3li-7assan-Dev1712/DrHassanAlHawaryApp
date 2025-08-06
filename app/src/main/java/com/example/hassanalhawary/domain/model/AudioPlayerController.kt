package com.example.hassanalhawary.domain.model

import android.content.Context
import androidx.media3.common.MediaItem.DEFAULT_MEDIA_ID
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AudioPlayerController @Inject constructor(
    @param:ApplicationContext private val context: Context // Application context
) {
    private var player: ExoPlayer? = null // Using ExoPlayer

    private val _playerStateFlow = MutableStateFlow(PlayerState())
    val playerStateFlow: StateFlow<PlayerState> = _playerStateFlow.asStateFlow()

    private var positionUpdateJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job()) // Use Main for UI updates

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                addListener(object : androidx.media3.common.Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerStateFlow.update { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) {
                            startPositionUpdates()
                        } else {
                            stopPositionUpdates()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playerStateFlow.update {
                            it.copy(
                                isBuffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING,
                                // Update duration when ready or media item changes
                                totalDuration = if (duration > 0) duration else it.totalDuration
                            )
                        }
                        if (playbackState == androidx.media3.common.Player.STATE_READY && _playerStateFlow.value.totalDuration == 0L && duration > 0) {
                            _playerStateFlow.update { it.copy(totalDuration = duration) }
                        }
                        if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                            // Handle playback completion
                            _playerStateFlow.update { it.copy(currentPosition = 0L) } // Reset position

                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        _playerStateFlow.update {
                            it.copy(
                                error = error.message ?: "Unknown player error"
                            )
                        }
                    }

                    override fun onMediaItemTransition(
                        mediaItem: androidx.media3.common.MediaItem?,
                        reason: Int
                    ) {
                        super.onMediaItemTransition(mediaItem, reason)
                        // Update duration and media ID when media item changes
                        val newDuration = player?.duration ?: 0L
                        _playerStateFlow.update {
                            it.copy(
                                currentMediaId = mediaItem?.mediaId,
                                totalDuration = if (newDuration > 0) newDuration else 0L,
                                currentPosition = 0L // Reset position for new item
                            )
                        }
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                        _playerStateFlow.update { it.copy(currentSpeed = playbackParameters.speed) }
                    }
                })
            }
        }
    }

    fun prepareAndPlay(audioUrl: String, mediaId: String = DEFAULT_MEDIA_ID) {
        if (player == null) initializePlayer()
        player?.let { exoPlayer ->
            val mediaItem = androidx.media3.common.MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaId(mediaId)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true // Autoplay after preparation
            _playerStateFlow.update { it.copy(currentMediaId = mediaId, error = null) }
        }
    }

    fun playPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                if (it.playbackState == androidx.media3.common.Player.STATE_ENDED) { // Re-play if ended
                    it.seekTo(0)
                    it.playWhenReady = true
                } else {
                    it.play()
                }
            }
        }
    }

    fun seekTo(positionMillis: Long) {
        player?.seekTo(positionMillis.coerceAtLeast(0L))
        _playerStateFlow.update { it.copy(currentPosition = positionMillis) } // Optimistic update
    }

    fun forward(seconds: Int) {
        player?.let {
            val newPosition =
                (it.currentPosition + seconds * 1000).coerceAtMost(it.duration.coerceAtLeast(0L))
            seekTo(newPosition)
        }
    }

    fun rewind(seconds: Int) {
        player?.let {
            val newPosition = (it.currentPosition - seconds * 1000).coerceAtLeast(0L)
            seekTo(newPosition)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player?.let {
            val currentPitch = it.playbackParameters.pitch
            it.setPlaybackParameters(androidx.media3.common.PlaybackParameters(speed, currentPitch))
        }
    }


    private fun startPositionUpdates() {
        stopPositionUpdates() // Ensure only one job is running
        positionUpdateJob = coroutineScope.launch {
            while (isActive && player?.isPlaying == true) {
                _playerStateFlow.update {
                    it.copy(
                        currentPosition = player?.currentPosition ?: it.currentPosition,
                        bufferedPosition = player?.bufferedPosition ?: it.bufferedPosition
                    )
                }
                delay(500L) // Update interval (adjust as needed)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun release() {
        stopPositionUpdates()
        player?.release()
        player = null
        _playerStateFlow.value = PlayerState() // Reset state
        // Cancel the coroutineScope if this controller is being destroyed
    }

    fun onAppBackgrounded() {
        // for now I will jut stop the player then next I will integrate media session to play on the background
         player?.pause()
    }

    fun onAppForegrounded() {
        // Potentially resume or re-initialize if needed
    }
}