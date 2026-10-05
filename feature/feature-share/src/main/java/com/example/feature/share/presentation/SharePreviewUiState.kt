package com.example.feature.share.presentation

import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.domain.ShareExportState

@Suppress("ArrayInDataClass")
data class SharePreviewUiState(
    val audioUrl: String = "",
    val localFilePath: String? = null,
    /** The full source track's length - NOT the chosen clip window. */
    val totalTrackDurationMs: Long = 0L,
    val startMs: Long = 0L,
    /** The chosen clip window's length (60s default, 30s chip, or the whole track if shorter). */
    val clipDurationMs: Long = 0L,
    val content: ShareCardContent? = null,
    val clipEnvelope: FloatArray = FloatArray(0),
    val overviewEnvelope: FloatArray = FloatArray(0),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val playbackPositionMs: Long = 0L,
    /** True only while the real (non-placeholder) overview decode is still in
     * flight - never gates the UI, just lets it know the waveform may still sharpen. */
    val isExtracting: Boolean = true,
    val isTooShortToShare: Boolean = false,
    val exportState: ShareExportState = ShareExportState.Idle,
    val errorMessage: String? = null,
    /** Preview-playback error only (export failures use [errorMessage]) - kept
     * separate so a playback hiccup never hides the Share button. */
    val playbackErrorMessage: String? = null,
    /** Non-null only while a background audio download this screen is waiting on
     * is actively in progress - drives a thin, non-blocking progress affordance. */
    val downloadProgressPercent: Int? = null,
) {
    val playbackFraction: Float
        get() = if (clipDurationMs <= 0L) 0f else (playbackPositionMs.toFloat() / clipDurationMs).coerceIn(0f, 1f)
}
