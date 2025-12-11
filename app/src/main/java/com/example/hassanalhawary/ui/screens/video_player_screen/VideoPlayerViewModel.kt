package com.example.hassanalhawary.ui.screens.video_player_screen

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    val player: Player
) : ViewModel() {

    // Keep the ExoPlayer instance in the ViewModel

    /**
     * Prepares the player with a specific video URL.
     */
    fun playVideo(videoUrl: String) {
        // Set the media item to be played
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        // Prepare the player
        player.prepare() // THIS is the correct method, not Looper.prepare()
        // Start playing automatically when ready
        player.playWhenReady = true
    }

    // onCleared is called when the ViewModel is no longer used and will be destroyed.
    // This is the perfect place to release the player's resources.
    override fun onCleared() {
        player.stop()
        player.release()
        super.onCleared()
    }
}