package com.example.feature.audio.presentation.detail

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.player.PlaybackService
import com.example.core.ui.R
import com.example.feature.audio.presentation.components.formatDuration
import com.google.common.util.concurrent.ListenableFuture


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: AudioDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    val context = LocalContext.current
    val sessionToken = remember {
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
    }

    val controllerFuture: ListenableFuture<MediaController> = remember {
        MediaController.Builder(context, sessionToken).buildAsync()
    }

    LaunchedEffect(controllerFuture) {
        viewModel.mediaControllerFuture = controllerFuture
        Log.d("TAG", "AudioDetailRoute: created media controller and start listener")
    }

    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, PlaybackService::class.java)
        context.startService(serviceIntent)
        Log.d("TAG", "AudioDetailRoute: Start intent")
        /* onDepose {
              context.stopService(serviceIntent)
         }*/
    }

    AudioDetailScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onPlayPauseToggle = viewModel::onPlayPauseToggle,
        onSeek = viewModel::onSeek,
        onRewind = { viewModel.onRewind(10) },
        onForward = { viewModel.onForward(10) },
        onChangeSpeed = viewModel::onChangeSpeed,
        onToggleFavorite = viewModel::onToggleFavorite,
        onDownload = viewModel::onDownloadClicked,
        onShare = {
            // share the audio functionality will be added later
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDetailScreen(
    uiState: AudioDetailUiState,
    onNavigateUp: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent // Make TopAppBar transparent
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface // Main background
    ) { paddingValues ->
        if (uiState.isLoadingDetails) {
            LoadingSection(paddingValues)
        } else {
            AudioDetailContent(
                paddingValues,
                uiState,
                onToggleFavorite,
                onPlayPauseToggle,
                onSeek,
                onRewind,
                onForward,
                onChangeSpeed,
                onDownload
            )
        }
    }
}

@Composable
private fun AudioDetailContent(
    paddingValues: PaddingValues,
    uiState: AudioDetailUiState,
    onToggleFavorite: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background( // Adding a subtle gradient background
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {

        AudioTitleSection(uiState, onToggleFavorite)


        Spacer(modifier = Modifier.height(8.dp)) // Reduced spacer

        AudioDescriptionSection(onDownload, uiState)


        Spacer(modifier = Modifier.weight(1f))

        // --- Player Controls Section ---
        ThemedPlayerControls(
            uiState = uiState, // Pass full uiState for theming potential
            onPlayPauseToggle = onPlayPauseToggle,
            onSeek = onSeek,
            onRewind = onRewind,
            onForward = onForward,
            onChangeSpeed = onChangeSpeed,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
        )


    }
}

@Composable
private fun AudioDescriptionSection(
    onDownload: () -> Unit,
    uiState: AudioDetailUiState
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {

        if (uiState.description != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.audio_detail_description_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uiState.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@Composable
private fun AudioTitleSection(
    uiState: AudioDetailUiState,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f) // Adjust aspect ratio for a pleasing look
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(20.dp)) // Softer corners
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    3.dp
                )
            ), // Elevated look
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.dr_hassan_img),
            contentDescription = uiState.title,
            contentScale = ContentScale.Crop, // Crop to fill bounds
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        ),
                        startY = 0.6f * 220.dp.value // Adjust gradient start
                    )
                )
        )
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        FloatingActionButton(
            onClick = onToggleFavorite,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-40).dp) // Adjust offset
        ) {
            Icon(
                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite"
            )
        }
    }
}

@Composable
private fun LoadingSection(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}


@Composable
fun ThemedPlayerControls(
    modifier: Modifier = Modifier,
    uiState: AudioDetailUiState, // Use full uiState for potential theming
    onPlayPauseToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onChangeSpeed: (Float) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Seek Bar ---
        Box(contentAlignment = Alignment.Center) {
            Slider(
                value = if (uiState.totalDurationMillis > 0) uiState.currentPositionMillis.toFloat() / uiState.totalDurationMillis else 0f,
                onValueChange = onSeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                enabled = uiState.totalDurationMillis > 0 && !uiState.isBuffering,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            if (uiState.isBuffering && uiState.totalDurationMillis > 0) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatDuration(uiState.currentPositionMillis),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatDuration(uiState.totalDurationMillis),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Main Player Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemedControlButton(
                onClick = onRewind,
                enabled = !uiState.isBuffering,
                // ** THEMED ICON EXAMPLE **
                painter = painterResource(id = R.drawable.round_backword_icon),
                contentDescription = "Rewind 5 seconds",
                iconSize = 32.dp
            )

            // Play/Pause with animated content
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                AnimatedContent(
                    targetState = uiState.isPlaying,
                    label = "PlayPauseAnimation"
                ) { playing ->
                    ThemedControlButton(
                        onClick = onPlayPauseToggle,
                        enabled = !uiState.isBuffering && uiState.totalDurationMillis > 0,
                        painter = painterResource(id = if (playing) R.drawable.round_pause_icon else R.drawable.round_play_icon),
                        contentDescription = if (playing) "Pause" else "Play",
                        iconSize = 68.dp, // Larger for main action
                        containerColor = Color.Transparent, // No background for the main button if icon is rich
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.isBuffering && uiState.totalDurationMillis == 0L) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(68.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            ThemedControlButton(
                onClick = onForward,
                enabled = !uiState.isBuffering,
                painter = painterResource(id = R.drawable.round_forward_icon),
                contentDescription = "Forward 5 seconds",
                iconSize = 32.dp
            )
        }

    }
}

@Composable
fun ThemedControlButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), // Elevated button look
    iconTint: Color = MaterialTheme.colorScheme.onSurface // Default tint
) {
    Box(
        modifier = modifier
            .size(iconSize + 16.dp) // Make touch target larger than icon
            .clip(CircleShape)
            .background(
                if (enabled) containerColor else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.05f
                )
            )
            .clickable(onClick = onClick, enabled = enabled),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
        )
    }
}


// --- Previews ---
@Preview(showBackground = true, name = "Audio Detail Screen (Themed)")
@Composable
fun AudioDetailScreenThemedPreview() {
    MaterialTheme { // Apply your app's theme
        val sampleUiState = AudioDetailUiState(
            title = "The Art of Patience in Trials",
            description = "A deep dive into the concept of Sabr (patience) in Islam...",
            totalDurationMillis = 3600000L,
            currentPositionMillis = 1200000L,
            isPlaying = false,
            isFavorite = true,
            isLoadingDetails = false,
        )
        AudioDetailScreen(
            uiState = sampleUiState,
            onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {},
            onForward = {}, onChangeSpeed = {}, onToggleFavorite = {}, onDownload = {}, onShare = {}
        )
    }
}

@Preview(showBackground = true, name = "Audio Detail Screen - Playing (Themed)")
@Composable
fun AudioDetailScreenPlayingThemedPreview() {
    MaterialTheme {
        val sampleUiState = AudioDetailUiState(
            title = "Understanding Sincerity (Ikhlas)",
            totalDurationMillis = 1800000L,
            currentPositionMillis = 600000L,
            isPlaying = true,
            isLoadingDetails = false
        )
        AudioDetailScreen(
            uiState = sampleUiState,
            onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {},
            onForward = {}, onChangeSpeed = {}, onToggleFavorite = {}, onDownload = {}, onShare = {}
        )
    }
}
