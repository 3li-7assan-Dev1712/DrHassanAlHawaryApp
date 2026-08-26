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
    val playbackPositionMs: Long = 0L,
    val isExtracting: Boolean = true,
    val isTooShortToShare: Boolean = false,
    val exportState: ShareExportState = ShareExportState.Idle,
    val errorMessage: String? = null,
) {
    val playbackFraction: Float
        get() = if (clipDurationMs <= 0L) 0f else (playbackPositionMs.toFloat() / clipDurationMs).coerceIn(0f, 1f)
}
