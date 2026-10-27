package com.example.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.PlaybackProgress
import com.example.domain.text.ArabicNumerals

/**
 * How much of an audio has been heard: filled from the left like the player's seek bar, as
 * Figma's LessonRow/InProgress (81:186) draws it in RTL too.
 */
@Composable
fun ListeningProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ProgressTrack(fraction, modifier)
    }
}

/**
 * How many lessons of a series are completed: filled from the start (Figma SeriesCard/Progress
 * 81:162 and the series header 84:1715).
 */
@Composable
fun SeriesProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    ProgressTrack(fraction, modifier)
}

/** Figma "progress 3dp": a divider track, [fraction] 0..1 of it accentStrong from the start. */
@Composable
private fun ProgressTrack(fraction: Float, modifier: Modifier) {
    val colors = Brand.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(ListeningProgressHeight)
            .clip(RoundedCornerShape(ListeningProgressHeight / 2))
            .background(colors.divider),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .background(colors.accentStrong),
        )
    }
}

/**
 * What follows an audio's duration on its row once it was played: "متبقٍ ١٢ د" while it is
 * part way, "تم الاستماع" once finished; null otherwise.
 */
@Composable
fun listeningStatus(progress: PlaybackProgress?): String? = when {
    progress == null -> null
    progress.isFinished -> stringResource(R.string.listening_finished)
    progress.isInProgress && progress.durationMs > 0L ->
        stringResource(R.string.listening_remaining, ArabicNumerals.formatRemaining(progress.remainingMs))
    else -> null
}

private val ListeningProgressHeight = 3.dp

@Composable
private fun ListeningProgressPreviewContent() {
    Column(Modifier.background(Brand.colors.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ListeningProgressBar(0.55f, Modifier.width(240.dp))
        SeriesProgressBar(0.2f, Modifier.width(240.dp))
    }
}

@Preview(name = "Listening progress - light", locale = "ar")
@Composable
private fun ListeningProgressLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { ListeningProgressPreviewContent() }
}

@Preview(name = "Listening progress - dark", locale = "ar")
@Composable
private fun ListeningProgressDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { ListeningProgressPreviewContent() }
}
