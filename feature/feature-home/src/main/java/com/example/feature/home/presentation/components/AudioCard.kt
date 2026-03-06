package com.example.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.CairoTypography
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.feature.home.domain.model.AudioFeed
import java.util.Locale

@Composable
fun AudioCard(
    audio: AudioFeed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier

) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Audio Icon",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = audio.title,
                style = CairoTypography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = formatDuration(
                    audio.duration
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 500)
@Composable
fun PreviewAudioCard() {
    val sampleAudio =
        AudioFeed(
            id = "aud1",
            title = "Understanding the Essence of Patience in Islam",
            duration = 2722000, // Example: 45 minutes 22 seconds
            audioUrl = "https://example.com/audio.mp3",

            )
    HassanAlHawaryTheme {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .width(180.dp)
                .height(160.dp) // Adjusted height for better preview
        ) {
            AudioCard(audio = sampleAudio, onClick = {})
        }
    }
}

fun formatDuration(durationInMillis: Long): String {
    val totalSeconds = durationInMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        // Format as H:MM:SS
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        // Format as M:SS
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}