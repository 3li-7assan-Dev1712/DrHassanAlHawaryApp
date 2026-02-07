package com.example.admin.ui.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AddEditPlaylistScreen(
    playlistId: String? // Null if adding, non-null if editing
) {
    val screenTitle = if (playlistId == null) "Add New Playlist" else "Edit Playlist $playlistId"
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = screenTitle, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlaylistPreview() {
    AddEditPlaylistScreen(playlistId = null)
}

@Preview(showBackground = true)
@Composable
private fun EditPlaylistPreview() {
    AddEditPlaylistScreen(playlistId = "123")
}
