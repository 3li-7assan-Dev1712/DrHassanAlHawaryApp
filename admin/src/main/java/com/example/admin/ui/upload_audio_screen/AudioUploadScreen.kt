package com.example.admin.ui.upload_audio_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioUploadScreen(
    viewModel: AudioUploadViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val audioTitle by viewModel.audioTitle.collectAsState()
    val selectedAudioUri by viewModel.selectedAudioUri.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.upload_new_audio)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animate between the different states
            AnimatedContent(
                targetState = uiState,
                label = "State Animation"
            ) { state ->
                when (state) {
                    is AudioUploadUiState.Idle
                        -> {
                        UploadForm(
                            title = audioTitle,
                            onTitleChange = viewModel::onTitleChange,
                            selectedUri = selectedAudioUri,
                            onAudioSelected = viewModel::onAudioSelected,
                            onUploadClick = viewModel::uploadAudio
                        )
                    }

                    is AudioUploadUiState.Loading -> {
                        LoadingIndicator(progress = state.progress)
                    }

                    is AudioUploadUiState.Success -> {
                        StatusIndicator(
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF4CAF50),
                            message = stringResource(R.string.upload_success)
                        )
                    }

                    is AudioUploadUiState.Error -> {
                        StatusIndicator(
                            icon = Icons.Default.Warning,
                            iconColor = MaterialTheme.colorScheme.error,
                            message = state.message,
                            isError = true,
                            onRetry = viewModel::resetState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadForm(
    title: String,
    onTitleChange: (String) -> Unit,
    selectedUri: Uri?,
    onAudioSelected: (Uri) -> Unit,
    onUploadClick: () -> Unit
) {
    // This launcher handles opening the file picker for audio files.
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAudioSelected(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.audio_title_label)) },
            singleLine = true
        )

        FilePicker(
            selectedUri = selectedUri,
            onClick = {
                // Launch the file picker
                audioPickerLauncher.launch("audio/*")
            }
        )

        Button(
            onClick = onUploadClick,
            enabled = title.isNotBlank() && selectedUri != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(R.string.upload_audio_button), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilePicker(
    selectedUri: Uri?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedUri == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.select_file))
                Text(stringResource(R.string.select_audio_file))
            }
        } else {
            // Shows the last part of the file path as the name
            Text(
                text = stringResource(R.string.selected_file_label, selectedUri.path?.substringAfterLast('/') ?: ""),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun LoadingIndicator(progress: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.uploading), style = MaterialTheme.typography.titleLarge)
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
        Text("$progress%", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun StatusIndicator(
    icon: ImageVector,
    iconColor: Color,
    message: String,
    isError: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = iconColor
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isError && onRetry != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.try_again))
            }
        }
    }
}
