package com.example.feature.audio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feature.audio.domain.model.Audio
import com.example.feature.audio.presentation.list.AudioListScreen
import java.util.Locale
import java.util.concurrent.TimeUnit


@Composable
fun AudioListItem(
    audio: Audio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = audio.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                overflow = Ellipsis,
                maxLines = 2
            )
            Text(
                text = formatDuration(audio.durationInMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AudioListScreenPreview() {
    MaterialTheme {
        AudioListScreen(
            onNavigateToAudioDetail = { title, audioId ->
                println("Navigate to detail for audio ID: $audioId")
            },
            onNavigateBack = {}

        )
    }
}

@Preview(showBackground = true)
@Composable
fun AudioListScreenEmptyPreview() {
    MaterialTheme {
        AudioListScreen(
            onNavigateToAudioDetail = { title, audioUrl -> },
            onNavigateBack = {}
        )
    }
}

/**
 * Formats a duration from milliseconds into a HH:mm:ss or mm:ss string.
 * @param millis The duration in milliseconds.
 * @return A formatted string like "01:39:21" or "39:21".
 */
fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)

    return if (hours > 0) {
        // Include hours if the duration is an hour or longer
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        // Otherwise, just show minutes and seconds
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}