package com.example.feature.share.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Full-screen, semi-transparent progress overlay shown the instant Share is
 * tapped - the trim/preview screen stays visible behind it. [progress] is
 * expected to update synchronously with the tap so there's no visible gap
 * before this appears. Generic across every share flow (video export, quote
 * image render, ...) - the caller supplies [titleText] rather than this
 * component hardcoding wording for one specific flow.
 *
 * [indeterminate] covers a flow's pre-result phase (clip extraction + waveform
 * analysis for video, or the near-instant bitmap render for a quote image),
 * which has no meaningful percentage of its own - showing a frozen "0%" there
 * reads as hung, so a spinner runs instead until real progress starts arriving.
 */
@Composable
fun GenerationOverlay(
    progress: Float,
    titleText: String,
    indeterminate: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "generationProgress",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
        if (indeterminate) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 20.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.25f),
            )
        } else {
            Text(
                text = "${(animatedProgress * 100).roundToInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.25f),
            )
        }
    }
}
