package com.example.study.presentation.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.domain.module.Playlist

@Composable
fun PlaylistScreen(
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LessonsPlaylistContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPlaylistClick = onPlaylistClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsPlaylistContent(
    modifier: Modifier = Modifier,
    uiState: PlaylistUiState,
    onNavigateBack: () -> Unit,
    onPlaylistClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.lessons)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState) {
                is PlaylistUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is PlaylistUiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.playlists) { playlist ->
                            LessonPlaylistItem(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist.id) })
                        }
                    }
                }

                is PlaylistUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun LessonPlaylistItem(
    playlist: Playlist,
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
                model = playlist.thumbnailUrl,
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
                /*Text(
                    text = "${playlist.} lessons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )*/
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


@Preview(showBackground = true)
@Composable
private fun LessonsPlaylistScreenPreview() {
    val playlists = listOf(
        Playlist("p1", "أساسيات العقيدة الإسلامية", "12"),
        Playlist("p2", "شرح كتاب التوحيد", ""),
        Playlist("p3", "مقدمات في علوم الحديث", ""),
    )
    MaterialTheme {
        LessonsPlaylistContent(
            uiState = PlaylistUiState.Success(playlists),
            onNavigateBack = {},
            onPlaylistClick = {},
        )
    }
}
