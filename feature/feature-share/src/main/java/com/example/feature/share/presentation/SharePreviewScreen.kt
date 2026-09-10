package com.example.feature.share.presentation

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.example.core.ui.R
import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.engine.ShareCardSpec
import com.example.feature.share.presentation.components.GenerationOverlay
import com.example.feature.share.presentation.components.ShareActionBar
import com.example.feature.share.presentation.components.ShareCardPreview
import com.example.feature.share.presentation.components.TrimTimeline
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun SharePreviewScreen(
    onNavigateUp: () -> Unit,
    viewModel: SharePreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.share_video_chooser)

    LaunchedEffect(viewModel) {
        viewModel.shareIntentEvent.collect { uri ->
            val title = uiState.content?.title.orEmpty()
            val appStoreLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "$title\n$appStoreLink")
                clipData = ClipData.newUri(context.contentResolver, title, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, chooserTitle)
            // §8: no app can handle the intent -> fall back to a plain-text share.
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            } else {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "$title\n$appStoreLink")
                }
                context.startActivity(Intent.createChooser(fallback, chooserTitle))
            }
        }
    }

    SharePreviewScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onPlayPauseToggle = viewModel::onPlayPauseToggle,
        onSeekWithinClip = viewModel::onSeekWithinClip,
        onRangeChanged = viewModel::onRangeChanged,
        onShareClick = viewModel::onShareClicked,
        onRetry = viewModel::onRetry,
        onShareLinkInstead = {
            val title = uiState.content?.title.orEmpty()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$title\n${uiState.audioUrl}")
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharePreviewScreen(
    uiState: SharePreviewUiState,
    onNavigateUp: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSeekWithinClip: (Long) -> Unit,
    onRangeChanged: (startMs: Long, endMs: Long) -> Unit,
    onShareClick: () -> Unit,
    onRetry: () -> Unit,
    onShareLinkInstead: () -> Unit,
) {
    val isGenerating = uiState.exportState is ShareExportState.Preparing ||
        uiState.exportState is ShareExportState.Encoding

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.share_preview_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.share_preview_close),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val content = uiState.content
                val spec = remember { ShareCardSpec.default() }
                if (content != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .clip(RoundedCornerShape(24.dp)),
                    ) {
                        ShareCardPreview(
                            content = content,
                            spec = spec,
                            envelope = uiState.clipEnvelope,
                            playbackFraction = uiState.playbackFraction,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Only ever appears while a background audio download this screen is
                // actively waiting on is in flight - not for the overview decode, and
                // never a full-screen block. Rare once the detail screen's silent
                // prefetch has had a head start.
                uiState.downloadProgressPercent?.let { percent ->
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                PlaybackScrubber(
                    positionMs = uiState.playbackPositionMs,
                    durationMs = uiState.clipDurationMs,
                    isPlaying = uiState.isPlaying,
                    isBuffering = uiState.isBuffering,
                    errorMessage = uiState.playbackErrorMessage,
                    onPlayPauseToggle = onPlayPauseToggle,
                    onSeek = onSeekWithinClip,
                )

                Text(
                    text = stringResource(
                        R.string.share_trim_range,
                        formatDuration(uiState.startMs),
                        formatDuration(uiState.startMs + uiState.clipDurationMs),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                TrimTimeline(
                    overviewEnvelope = uiState.overviewEnvelope,
                    totalDurationMs = uiState.totalTrackDurationMs,
                    startMs = uiState.startMs,
                    endMs = uiState.startMs + uiState.clipDurationMs,
                    onRangeChanged = onRangeChanged,
                    modifier = Modifier.padding(vertical = 16.dp),
                )

                if (!uiState.isTooShortToShare) {
                    val errorMessage = uiState.errorMessage
                    if (errorMessage != null) {
                        ErrorCard(
                            message = errorMessage,
                            onRetry = onRetry,
                            onShareLinkInstead = onShareLinkInstead,
                        )
                    } else {
                        ShareActionBar(
                            enabled = !isGenerating,
                            onShareClick = onShareClick,
                        )
                    }
                }
            }
        }

        if (isGenerating) {
            GenerationOverlay(
                progress = uiState.exportState.toProgressFraction() ?: 0f,
                titleText = stringResource(R.string.share_generating_video),
                indeterminate = uiState.exportState is ShareExportState.Preparing,
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onShareLinkInstead: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.share_retry))
            }
            TextButton(onClick = onShareLinkInstead) {
                Text(stringResource(R.string.share_link_instead))
            }
        }
    }
}

private fun ShareExportState.toProgressFraction(): Float? = when (this) {
    is ShareExportState.Encoding -> progress
    is ShareExportState.Ready -> 1f
    ShareExportState.Preparing -> 0f
    ShareExportState.Idle, is ShareExportState.Failed -> null
}

@Composable
private fun PlaybackScrubber(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    isBuffering: Boolean,
    errorMessage: String?,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    // The ViewModel's position updates every 200ms from the player; without this
    // local guard, that update fights the user's drag and the thumb never moves
    // (same pattern as AudioDetailScreen's ThemedPlayerControls).
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(positionMs.toFloat()) }

    LaunchedEffect(positionMs) {
        if (!isUserSeeking) seekPosition = positionMs.toFloat()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onPlayPauseToggle) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.share_pause else R.string.share_play
                    ),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isBuffering) 0.3f else 1f),
                )
            }
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Slider(
            value = seekPosition,
            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
            onValueChange = {
                isUserSeeking = true
                seekPosition = it
            },
            onValueChangeFinished = {
                isUserSeeking = false
                onSeek(seekPosition.toLong())
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(positionMs), style = MaterialTheme.typography.labelMedium)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0L))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
