package com.example.study.presentation.detail

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.player.PlaybackService
import com.example.core.ui.R
import com.example.core.ui.animation.LoadingScreen
import com.example.domain.module.Lesson
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun LessonDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: LessonDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        viewModel.mediaControllerFuture = controllerFuture

        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }

    fun openPdf(url: String) {
        val intent = if (url.startsWith("http")) {
            // It's a remote URL, open in browser
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            // It's a local file path, use FileProvider
            val file = File(url)
            val authority = "${context.packageName}.provider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No application can handle this request.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    LessonDetailContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPlayPauseClick = viewModel::onPlayPauseClick,
        onSeekForward = viewModel::onSeekForward,
        onSeekBackward = viewModel::onSeekBackward,
        onSeekBarPositionChanged = viewModel::onSeekBarPositionChanged,
        onOpenPdfClick = { uiState.lesson?.pdfUrl?.let { openPdf(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailContent(
    modifier: Modifier = Modifier,
    uiState: PlayerUiState,
    onNavigateBack: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,
    onOpenPdfClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = uiState.lesson?.title ?: "", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPdfClick) {
                        Icon(Icons.Default.Description, "Open PDF Summary")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isLoading) {
                LoadingScreen()
            } else if (uiState.lesson != null) {
                // Player UI
                Image(
                    painter = painterResource(id = R.drawable.naqthm_lesson),
                    contentDescription = "Lesson Art",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = uiState.lesson.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                PlayerControls(
                    isPlaying = uiState.isPlaying,
                    isBuffering = uiState.isBuffering,
                    currentPosition = uiState.currentPosition,
                    totalDuration = uiState.totalDuration,
                    onPlayPauseClick = onPlayPauseClick,
                    onSeekForward = onSeekForward,
                    onSeekBackward = onSeekBackward,
                    onSeekBarPositionChanged = onSeekBarPositionChanged
                )
            }
        }
    }
}

@Composable
fun ThemedControlButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), // Elevated button look
    iconTint: Color = MaterialTheme.colorScheme.onSurface // Default tint
) {
    Box(
        modifier = modifier
            .size(iconSize + 16.dp) // Make touch target larger than icon
            .clip(CircleShape)
            .background(
                containerColor
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,
) {
    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(currentPosition.toFloat()) }

    // Sync with player ONLY when not dragging
    LaunchedEffect(currentPosition) {
        if (!isUserSeeking) {
            sliderPosition = currentPosition.toFloat()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Slider(
            value = sliderPosition,
            onValueChange = {
                isUserSeeking = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isUserSeeking = false
                onSeekBarPositionChanged(sliderPosition.toLong())
            },
            valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(0f)),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(sliderPosition.toLong()),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatDuration(totalDuration),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemedControlButton(
                onClick = onSeekBackward,
                painter = painterResource(id = R.drawable.round_backword_icon),
                contentDescription = "Forward 5 seconds",
                iconSize = 32.dp
            )

            if (isBuffering) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
            } else {
                Card(shape = CircleShape, elevation = CardDefaults.cardElevation(4.dp)) {
                    IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(64.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            ThemedControlButton(
                onClick = onSeekForward,
                painter = painterResource(id = R.drawable.round_forward_icon),
                contentDescription = "Forward 5 seconds",
                iconSize = 32.dp
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun LessonDetailScreenPreview() {
    MaterialTheme {
        LessonDetailContent(
            uiState = PlayerUiState(
                lesson = Lesson(
                    id = "1",
                    title = "Introduction to Islamic Beliefs",
                    audioUrl = "",
                    pdfUrl = "",
                    duration = "12:34"
                ),
                isPlaying = false,
                currentPosition = 120000L, // 2 minutes
                totalDuration = 754000L, // 12 minutes 34 seconds
                isLoading = false
            ),
            onNavigateBack = {},
            onPlayPauseClick = {},
            onSeekForward = {},
            onSeekBackward = {},
            onSeekBarPositionChanged = {},
            onOpenPdfClick = {}
        )
    }
}
