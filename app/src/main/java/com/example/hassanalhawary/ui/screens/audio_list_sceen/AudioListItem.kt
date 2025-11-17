package com.example.hassanalhawary.ui.screens.audio_list_sceen

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
import com.example.domain.module.Audio


@Composable
fun AudioListItem(
    audio: Audio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
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
                text = audio.duration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Preview ---
val sampleAudiosForListScreen = listOf(
    Audio("aud1", "Understanding Surah Al-Baqarah - Part 1", "45:30"),
    Audio("aud2", "The Seerah: Makkan Period", "1:12:15"),
    Audio("aud3", "Principles of Islamic Jurisprudence", "58:02"),
    Audio("aud4", "Tafsir of Juz Amma: An-Nas to An-Naba", "2:30:00"),
    Audio("aud5", "The Virtues of Charity in Islam", "33:45")
)

@Preview(showBackground = true)
@Composable
fun AudioListScreenPreview() {
    MaterialTheme { // Wrap with your app's theme or MaterialTheme for preview
        AudioListScreen(
            onNavigateToAudioDetail = { title, audioId -> println("Navigate to detail for audio ID: $audioId") }

        )
    }
}

@Preview(showBackground = true)
@Composable
fun AudioListScreenEmptyPreview() {
    MaterialTheme {
        AudioListScreen(
            onNavigateToAudioDetail = { title, audioUrl -> }
        )
    }
}