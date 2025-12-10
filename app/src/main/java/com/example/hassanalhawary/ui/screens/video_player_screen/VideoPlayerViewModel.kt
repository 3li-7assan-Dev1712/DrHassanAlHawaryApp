package com.example.hassanalhawary.ui.screens.video_player_screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer


class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    // Keep the ExoPlayer instance in the ViewModel
    val exoPlayer: ExoPlayer

    init {
        // Get the application context from the AndroidViewModel
        val context = getApplication<Application>().applicationContext

        // Build the ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            // This is where you would set the MediaItem.
            // We will do this via a method instead.
        }
    }

    /**
     * Prepares the player with a specific video URL.
     */
    fun playVideo(videoUrl: String) {
        // Set the media item to be played
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
        // Prepare the player
        exoPlayer.prepare() // THIS is the correct method, not Looper.prepare()
        // Start playing automatically when ready
        exoPlayer.playWhenReady = true
    }

    // onCleared is called when the ViewModel is no longer used and will be destroyed.
    // This is the perfect place to release the player's resources.
    override fun onCleared() {
        exoPlayer.stop()
        exoPlayer.release()
        super.onCleared()
    }
}