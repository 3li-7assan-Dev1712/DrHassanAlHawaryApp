package com.example.admin.ui.upload_images_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.admin.R
import kotlinx.coroutines.launch


@Composable
fun UploadImagesScreen(
    viewModel: UploadImagesViewModel = hiltViewModel()
) {
    // --- State Collection ---
    val uiState by viewModel.uiState.collectAsState()
    val groupTitle by viewModel.groupTitle.collectAsState()
    val selectedImageUris by viewModel.selectedImageUris.collectAsState()

    // --- UI Infrastructure ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                viewModel.onImagesSelected(uris)
            }
        }
    )

    val successMsg = stringResource(R.string.upload_success)
    val errorPrefix = stringResource(R.string.error_loading)

    // --- Side Effects ---
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UploadImagesUiState.Success -> {
                snackbarHostState.showSnackbar(successMsg)
            }
            is UploadImagesUiState.Error -> {
                snackbarHostState.showSnackbar("$errorPrefix: ${state.message}")
            }
            else -> { /* No side effect needed for Idle or Loading */ }
        }
    }

    // --- UI Layout ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Crossfade(targetState = uiState is UploadImagesUiState.Loading, label = "UploadStateCrossfade") { isLoading ->
            if (isLoading) {
                // --- Loading State ---
                val progress = (uiState as? UploadImagesUiState.Loading)?.progress ?: 0
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(progress = { progress / 100f })
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.uploading_progress, progress),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                // --- Idle, Success, or Error State (The Form) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.upload_new_design_group), style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = groupTitle,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text(stringResource(R.string.group_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState is UploadImagesUiState.Idle
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState is UploadImagesUiState.Idle
                    ) {
                        Text(stringResource(R.string.select_images_button, selectedImageUris.size))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid to preview selected images
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImageUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.uploadDesignGroup()
                            }
                        },
                        enabled = groupTitle.isNotBlank() && selectedImageUris.isNotEmpty() && uiState is UploadImagesUiState.Idle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.upload_to_firebase))
                    }
                }
            }
        }
    }
}
