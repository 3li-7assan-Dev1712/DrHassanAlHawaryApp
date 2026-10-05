package com.example.feature.share.presentation

import android.content.ClipData
import android.content.Intent
import androidx.compose.runtime.remember
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.text.ArabicNumerals
import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.presentation.components.ClipSelector
import com.example.feature.share.presentation.components.ShareCardPreview

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
            val appStoreLink = context.getString(R.string.share_cta_url)
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

/**
 * Everything fits one screen, no scrolling: the 9:16 preview at ~40% of the height, the
 * clip selector, and the share button pinned at the bottom with the encoding progress
 * inside it.
 */
@Composable
private fun SharePreviewScreen(
    uiState: SharePreviewUiState,
    onNavigateUp: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onRangeChanged: (startMs: Long, endMs: Long) -> Unit,
    onShareClick: () -> Unit,
    onRetry: () -> Unit,
    onShareLinkInstead: () -> Unit,
) {
    val colors = Brand.colors
    val isGenerating = uiState.exportState is ShareExportState.Preparing ||
        uiState.exportState is ShareExportState.Encoding

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.share_preview_title), onBack = onNavigateUp) },
        containerColor = colors.background,
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val previewHeight = (maxHeight * 0.4f).coerceAtMost(maxHeight - 420.dp).coerceAtLeast(140.dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val content = uiState.content
                val playbackPositionMs = smoothPlaybackPosition(uiState.playbackPositionMs, uiState.isPlaying)
                if (content != null) {
                    ShareCardPreview(
                        content = content,
                        envelope = uiState.clipEnvelope,
                        playbackPositionMs = playbackPositionMs,
                        clipDurationMs = uiState.clipDurationMs,
                        modifier = Modifier
                            .height(previewHeight)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                }

                // Only while a background audio download this screen waits on is in flight.
                uiState.downloadProgressPercent?.let { percent ->
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(top = 6.dp),
                        color = colors.accentStrong,
                        trackColor = colors.divider,
                    )
                }
                uiState.playbackErrorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))
                ClipSelector(
                    overviewEnvelope = uiState.overviewEnvelope,
                    totalMs = uiState.totalTrackDurationMs,
                    startMs = uiState.startMs,
                    clipMs = uiState.clipDurationMs,
                    playbackPositionMs = playbackPositionMs,
                    isPlaying = uiState.isPlaying,
                    isBuffering = uiState.isBuffering,
                    enabled = !isGenerating,
                    onRangeChanged = onRangeChanged,
                    onPlayPause = onPlayPauseToggle,
                )

                Spacer(Modifier.weight(1f))

                if (!uiState.isTooShortToShare) {
                    val errorMessage = uiState.errorMessage
                    if (errorMessage != null) {
                        ErrorCard(message = errorMessage, onRetry = onRetry, onShareLinkInstead = onShareLinkInstead)
                    } else {
                        ShareButton(exportState = uiState.exportState, enabled = !isGenerating, onClick = onShareClick)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** "مشاركة الفيديو", or the encoding progress inside the same button while it's generated. */
@Composable
private fun ShareButton(exportState: ShareExportState, enabled: Boolean, onClick: () -> Unit) {
    val colors = Brand.colors
    val progress = exportState.toProgressFraction()
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accentStrong,
            contentColor = colors.onGold,
            disabledContainerColor = colors.accentContainer,
            disabledContentColor = colors.onAccentContainer,
        ),
    ) {
        if (!enabled) {
            CircularProgressIndicator(
                progress = { progress ?: 0f },
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = colors.onAccentContainer,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(
                    R.string.share_preparing_percent,
                    "${ArabicNumerals.digits(((progress ?: 0f) * 100).toInt())}٪",
                ),
            )
        } else {
            Icon(painterResource(TablerIcons.Share), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.share_video_button), style = MaterialTheme.typography.titleSmall)
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
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
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

/** How often the view model reads the player position (its POSITION_POLL_MS). */
private const val POSITION_POLL_MS = 200

/**
 * The preview position moves linearly between the view model's 200ms polls while playing,
 * so the waveform fill and the playhead glide instead of stepping 5 times a second.
 * Jumps (a new clip, a seek back, pause) are applied at once.
 */
@Composable
private fun smoothPlaybackPosition(targetMs: Long, isPlaying: Boolean): Long {
    val position = remember { Animatable(targetMs.toFloat()) }
    LaunchedEffect(targetMs, isPlaying) {
        val target = targetMs.toFloat()
        if (!isPlaying || target < position.value || target - position.value > POSITION_POLL_MS * 3) {
            position.snapTo(target)
        } else {
            position.animateTo(target, tween(POSITION_POLL_MS, easing = LinearEasing))
        }
    }
    return position.value.toLong()
}
