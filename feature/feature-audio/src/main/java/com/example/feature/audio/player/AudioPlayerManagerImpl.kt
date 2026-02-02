package com.example.feature.audio.player

import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.player.PlaybackService
import com.example.feature.audio.domain.model.Audio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@RequiresApi(Build.VERSION_CODES.P)
@Singleton
class AudioPlayerManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayerManager, Player.Listener {

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState = _playerState.asStateFlow()

    private var mediaController: MediaController? = null
    private var playlist: List<Audio> = emptyList()
    private var currentAudioIndex: Int = -1

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdaterJob: Job? = null

    init {
        connectToService()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val controllerFuture =
            MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                mediaController?.addListener(this)
                // Once connected, update the state with the controller's current state
                updateStateFromController()
            },
            context.mainExecutor
        )
    }

    override fun setPlaylist(audios: List<Audio>) {
        this.playlist = audios
    }

    override fun playAudio(audio: Audio) {
        currentAudioIndex = playlist.indexOf(audio)
        if (currentAudioIndex == -1) {
            playlist = listOf(audio)
            currentAudioIndex = 0
        }

        mediaController?.setMediaItem(MediaItem.fromUri(audio.audioUrl))
        mediaController?.prepare()
        mediaController?.play()
    }

    override fun resume() {
        mediaController?.play()
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    override fun playNext() {
    }

    override fun playPrevious() {
    }

    override fun stop() {
        mediaController?.stop()
    }

    // ... Implement resume, pause, seekTo, playNext, playPrevious, stop ...
    // These will call the corresponding methods on `mediaController`

    // Player.Listener callback
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _playerState.update { it.copy(isPlaying = isPlaying) }
        if (isPlaying) {
            startPositionUpdater()
        } else {
            stopPositionUpdater()
        }
    }

    // Player.Listener callback
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        updateStateFromController()
    }

    private fun updateStateFromController() {
        val currentMediaItem = mediaController?.currentMediaItem ?: return
        val audio = playlist.find { it.audioUrl == currentMediaItem.mediaId }

        _playerState.update {
            it.copy(
//                currentAudio = audio,
                isPlaying = mediaController?.isPlaying ?: false,
                totalDuration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L,
            )
        }
    }

    private fun startPositionUpdater() {
        stopPositionUpdater()
        positionUpdaterJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _playerState.update {
                        it.copy(currentPosition = controller.currentPosition.coerceAtLeast(0L))
                    }
                }
                delay(500) // Update position twice a second
            }
        }
    }

    private fun stopPositionUpdater() {
        positionUpdaterJob?.cancel()
        positionUpdaterJob = null
    }

}