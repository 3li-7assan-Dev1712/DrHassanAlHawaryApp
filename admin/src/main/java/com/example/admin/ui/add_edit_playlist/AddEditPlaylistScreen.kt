package com.example.admin.ui.add_edit_playlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.admin.ui.theme.HassanAlHawaryTheme

@Composable
fun AddEditPlaylistScreen(
    viewModel: AddEditPlaylistViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle terminal states
    LaunchedEffect(uiState) {
        when (uiState) {
            is AddEditPlaylistUiState.SaveSuccess -> onSaveSuccess()
            is AddEditPlaylistUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as AddEditPlaylistUiState.Error).message)
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (val state = uiState) {
                is AddEditPlaylistUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AddEditPlaylistUiState.Stable -> {
                    AddEditPlaylistContent(
                        state = state,
                        onTitleChange = viewModel::onTitleChange,
                        onOrderChange = viewModel::onOrderChange,
                        onLevelChange = viewModel::onLevelChange,
                        onImageSelected = viewModel::onImageSelected,
                        onSaveClick = viewModel::onSaveClick
                    )
                }

                else -> Unit // Handled by LaunchedEffect
            }
        }
    }
}


@Composable
private fun AddEditPlaylistContent(
    state: AddEditPlaylistUiState.Stable,
    onTitleChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onSaveClick: () -> Unit
) {

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> onImageSelected(uri) }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick/* modifier = if(state.isSaving) Modifier.clickable(enabled = false) else Modifier*/) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Save Playlist")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Image Selector --- //
            Text("Playlist Cover Image", style = MaterialTheme.typography.titleMedium)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val imageToShow = state.selectedImageUri ?: state.existingImageUrl
                    if (imageToShow != null) {
                        AsyncImage(
                            model = imageToShow,
                            contentDescription = "Playlist Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Click to select an image",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // --- Input Fields --- //
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Playlist Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null
            )

            OutlinedTextField(
                value = state.order,
                onValueChange = onOrderChange,
                label = { Text("Order") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.error != null
            )
            if (!state.playlistId.isNullOrEmpty()) {
                OutlinedTextField(
                    value = state.levelId,
                    onValueChange = onLevelChange,
                    label = { Text("Level ID") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.error != null
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlaylistPreview() {
    HassanAlHawaryTheme {
        AddEditPlaylistContent(
            state = AddEditPlaylistUiState.Stable(levelId = "", playlistId = null),
            onTitleChange = {}, onOrderChange = {}, onImageSelected = {}, onLevelChange = {}, onSaveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditPlaylistPreview() {
    HassanAlHawaryTheme {
        AddEditPlaylistContent(
            state = AddEditPlaylistUiState.Stable(
                levelId = "",
                playlistId = "123",
                title = "Existing Title",
                order = "1",
                existingImageUrl = "https://example.com/image.jpg"
            ),
            onTitleChange = {}, onOrderChange = {}, onImageSelected = {}, onLevelChange = {}, onSaveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavingPreview() {
    HassanAlHawaryTheme {
        AddEditPlaylistContent(
            state = AddEditPlaylistUiState.Stable(levelId = "", playlistId = null, isSaving = true),
            onTitleChange = {}, onOrderChange = {}, onImageSelected = {}, onLevelChange = {}, onSaveClick = {}
        )
    }
}
