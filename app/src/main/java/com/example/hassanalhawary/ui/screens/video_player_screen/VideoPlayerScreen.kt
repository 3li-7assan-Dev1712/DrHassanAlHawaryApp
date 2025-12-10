package com.example.hassanalhawary.ui.screens.video_player_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

//    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    // Handle the back button press to navigate up and unlock orientation
    BackHandler {
        onNavigateBack()
    }

    // Create and remember the ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Set the media item to be played
            setMediaItem(MediaItem.fromUri(videoUrl))
            // Prepare the player
            // Start playing automatically when ready
            playWhenReady = true
        }
    }

    // Use DisposableEffect for lifecycle management
    // This is crucial to release the player when the screen is closed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        // Use AndroidView to embed the ExoPlayer's PlayerView in Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                }
            }
        )
    }
}
