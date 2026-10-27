package com.example.core.player

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.domain.use_cases.progress.SavePlaybackProgressUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var savePlaybackProgress: SavePlaybackProgressUseCase

    /** Keeps where each audio was left, whichever screen (or none) is playing it. */
    private var progressTracker: PlaybackProgressTracker? = null

    override fun onCreate() {
        super.onCreate()
        progressTracker = PlaybackProgressTracker(player, savePlaybackProgress)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        // Before the player goes: it still knows where the audio was left.
        progressTracker?.release()
        progressTracker = null
        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}
