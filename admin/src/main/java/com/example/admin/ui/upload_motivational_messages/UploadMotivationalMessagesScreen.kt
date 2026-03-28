package com.example.admin.ui.upload_motivational_messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.R
import com.example.admin.ui.theme.HassanAlHawaryTheme

@Composable
fun UploadMotivationalMessagesScreen(
    viewModel: UploadMotivationalMessagesViewModel = hiltViewModel(),
    onUploadSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.UploadSuccess -> onUploadSuccess()
            is UiState.Stable -> {
                if (state.error != null) {
                    snackbarHostState.showSnackbar(state.error)
                }
            }
        }
    }

    when (val state = uiState) {
        is UiState.Stable -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    FloatingActionButton(onClick = viewModel::onUploadClick) {
                        if (state.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = stringResource(R.string.upload_messages))
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.motivational_messages_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = state.messagesText,
                        onValueChange = viewModel::onMessagesTextChange,
                        label = { Text(stringResource(R.string.motivational_messages_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
        else -> { /* No-op */ }
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadMotivationalMessagesScreenPreview() {
    HassanAlHawaryTheme {
        UploadMotivationalMessagesScreen(onUploadSuccess = {})
    }
}
