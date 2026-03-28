package com.example.admin.ui.upload_announcement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadAnnouncementScreen(
    viewModel: UploadAnnouncementViewModel = hiltViewModel(),
    onSendSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle terminal states
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UploadAnnouncementUiState.SendSuccess -> onSendSuccess()
            is UploadAnnouncementUiState.Stable -> {
                if (state.error != null) {
                    snackbarHostState.showSnackbar(state.error)
                }
            }
        }
    }

    when (val state = uiState) {
        is UploadAnnouncementUiState.Stable -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    FloatingActionButton(onClick = viewModel::onSendClick) {
                        if (state.isSending) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = stringResource(R.string.send_announcement))
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text(stringResource(R.string.title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.body,
                        onValueChange = viewModel::onBodyChange,
                        label = { Text(stringResource(R.string.body_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    // --- Topic Selector --- //
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.target_audience),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val topics = listOf(
                                "student_broadcasts" to R.string.topic_student_broadcasts,
                                "all_users" to R.string.topic_all_users
                            )
                            topics.forEach { (topicId, topicRes) ->
                                FilterChip(
                                    selected = state.topic == topicId,
                                    onClick = { viewModel.onTopicChange(topicId) },
                                    label = { Text(stringResource(topicRes)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        else -> { /* No-op */
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadAnnouncementScreenPreview() {
    HassanAlHawaryTheme {
        UploadAnnouncementScreen(onSendSuccess = {})
    }
}
