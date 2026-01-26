package com.example.study.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.study.domain.model.LessonPlaylist

@Composable
fun LessonsContent(modifier: Modifier = Modifier) {

    // fake data
    val playlists = remember {
        listOf(
            LessonPlaylist("p1", "أساسيات العقيدة الإسلامية", 12),
            LessonPlaylist("p2", "شرح كتاب التوحيد", 25),
            LessonPlaylist("p3", "مقدمات في علوم الحديث", 8),
            LessonPlaylist("p4", "فقه العبادات", 30),
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.lessons),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(playlists) { playlist ->
                LessonPlaylistItem(playlist = playlist, onClick = { /* TODO: Handle click */ })
            }
        }
    }
}

@Composable
fun LessonPlaylistItem(
    playlist: LessonPlaylist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = playlist.thumbnail,
                contentDescription = playlist.title,
                placeholder = painterResource(id = R.drawable.naqthm_lesson),
                error = painterResource(id = R.drawable.naqthm_lesson),
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Title and lesson count
            Column(modifier = Modifier.weight(1f)) {
                Text(text = playlist.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${playlist.lessonCount} lessons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play icon
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play playlist",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}