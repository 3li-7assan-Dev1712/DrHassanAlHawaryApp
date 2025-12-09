package com.example.hassanalhawary.ui.screens.home_screen.components

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
import com.example.domain.module.Audio
import com.example.hassanalhawary.core.util.formatDate
import com.example.hassanalhawary.ui.theme.CairoTypography
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme
import java.util.Date

@Composable
fun AudioCard(
    audio: Audio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier

) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                text = formatDate(
                    audio.publishDate
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
        Audio(
            "aud1",
            "Understanding the Essence of Patience in Islam",
            durationInMillis = 2722000, // Example: 45 minutes 22 seconds
            audioUrl = "https://example.com/audio.mp3",
            publishDate = Date(), // Use current date for preview
            isFavorite = false,
            isDownloaded = false,
            lastPlayedTimestamp = 0,
            isPlaying = false,
            localFilePath = null,
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