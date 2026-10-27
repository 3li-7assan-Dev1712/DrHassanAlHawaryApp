package com.example.core.player

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.domain.diagnostics.ErrorReporter
import com.example.domain.use_cases.progress.SavePlaybackProgressUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Keeps where each resumable audio ([ResumableMedia]) was left, so it opens there again, also
 * after the app was closed. It listens to the service's [player], whichever screen (or none)
 * controls it, and tells [PlaybackProgressRecorder] what happens; the recorder picks what
 * [saveProgress] writes. Runs on the player's (main) thread. [release] before the player is
 * released: it saves the position the audio was left at.
 */
internal class PlaybackProgressTracker(
    private val player: Player,
    private val saveProgress: SavePlaybackProgressUseCase,
) : Player.Listener {

    // Never cancelled: the save made by release() still completes after the service is gone.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val recorder = PlaybackProgressRecorder(::persist)
    private var ticker: Job? = null

    init {
        player.addListener(this)
        recorder.onItemChanged(read())
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        recorder.onItemChanged(read())
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            recorder.onPlaying(read())
            startTicker()
        } else {
            stopTicker()
            recorder.onSettled(read())
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) recorder.onSettled(read())
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) recorder.onEnded(read())
    }

    fun release() {
        stopTicker()
        recorder.onSettled(read())
        player.removeListener(this)
    }

    /** The player's item, when it is a resumable audio, and where it is. */
    private fun read(): AudioPosition? {
        val item = player.currentMediaItem?.takeIf(ResumableMedia::isResumable) ?: return null
        return AudioPosition(
            audioUrl = item.mediaId,
            title = item.mediaMetadata.title?.toString().orEmpty(),
            positionMs = player.currentPosition,
            durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L,
        )
    }

    private fun startTicker() {
        stopTicker()
        ticker = scope.launch {
            while (isActive) {
                delay(TICK_MS)
                recorder.onTick(read(), TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun persist(audio: AudioPosition) {
        scope.launch {
            try {
                saveProgress(audio.audioUrl, audio.title, audio.positionMs, audio.durationMs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Playback goes on; only this save is lost.
                Log.w(TAG, "Could not save the position of ${audio.audioUrl}", e)
                ErrorReporter.record("playback progress save", e)
            }
        }
    }

    private companion object {
        const val TAG = "PlaybackProgress"
        const val TICK_MS = 1_000L
    }
}
