package com.example.admin.ui.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.admin.R
import com.example.admin.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.animation.LoadingScreen
import com.example.domain.module.Playlist


@Composable
fun PlaylistScreen(
    levelName: String,
    adminPlaylistViewModel: AdminPlaylistViewModel = hiltViewModel(),
    onAddPlaylist: () -> Unit,
    onEditPlaylist: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    val uiState by adminPlaylistViewModel.uiState.collectAsState()
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlaylist) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_playlist))
            }
        }
    ) { paddingValues ->
        when (val currentState = uiState) {
            is AdminPlaylistUiState.Error -> Text(text = currentState.message)
            AdminPlaylistUiState.Loading -> LoadingScreen()
            is AdminPlaylistUiState.Success -> {

                val playlists = currentState.playlists
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onEditClick = { onEditPlaylist(playlist.id) },
                            onDeleteClick = { playlistToDelete = playlist },
                            onClick = { onPlaylistClick(playlist.id) }
                        )
                    }
                }
            }
        }
    }

    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text(text = stringResource(R.string.delete)) },
            text = { Text(text = stringResource(R.string.delete_confirmation_msg, playlist.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        adminPlaylistViewModel.deletePlaylist(playlist.id)
                        playlistToDelete = null
                    }
                ) {
                    Text(text = stringResource(R.string.delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            AsyncImage(
                model = playlist.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(com.example.core.ui.R.drawable.dr_hassan_photo),
                error = painterResource(com.example.core.ui.R.drawable.dr_hassan_photo),
                contentScale = ContentScale.Crop
            )


            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {

                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))

            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_playlist)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistScreenPreview() {
    HassanAlHawaryTheme {
        Surface {
            PlaylistScreen("Level 1", onAddPlaylist = {}, onEditPlaylist = {}, onPlaylistClick = {})
        }
    }
}
