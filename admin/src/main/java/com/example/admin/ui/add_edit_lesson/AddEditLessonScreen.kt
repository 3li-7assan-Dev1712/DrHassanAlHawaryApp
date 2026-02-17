package com.example.admin.ui.add_edit_lesson

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.animation.LoadingScreen

@Composable
fun AddEditLessonScreen(
    viewModel: AddEditLessonViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle terminal states
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddEditLessonUiState.SaveSuccess -> onSaveSuccess()
            is AddEditLessonUiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (val state = uiState) {
                is AddEditLessonUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingScreen()
                    }
                }

                is AddEditLessonUiState.Stable -> {
                    AddEditLessonContent(
                        state = state,
                        onTitleChange = viewModel::onTitleChange,
                        onOrderChange = viewModel::onOrderChange,
                        onPdfSelected = viewModel::onPdfSelected,
                        onAudioSelected = viewModel::onAudioSelected,
                        onClearPdf = viewModel::onClearPdf,
                        onClearAudio = viewModel::onClearAudio,
                        onSaveClick = viewModel::onSaveClick
                    )
                }

                else -> Unit // Handled by LaunchedEffect
            }
        }
    }
}

@Composable
private fun AddEditLessonContent(
    state: AddEditLessonUiState.Stable,
    onTitleChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onPdfSelected: (Uri?) -> Unit,
    onAudioSelected: (Uri?) -> Unit,
    onClearPdf: () -> Unit,
    onClearAudio: () -> Unit,
    onSaveClick: () -> Unit
) {
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = onPdfSelected
    )
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = onAudioSelected
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                modifier = if (state.isSaving) Modifier.clickable(
                    enabled = false,
                    onClick = { onSaveClick() }) else Modifier
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Save Lesson")
                }
            }
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Lesson Title") },
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
                singleLine = true,
                isError = state.error != null
            )

            if (state.error != null) {
                Text(
                    state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            FileControl(
                label = "PDF Document",
                icon = Icons.Default.PictureAsPdf,
                remoteUrl = state.existingPdfUrl,
                selectedLocalUri = state.selectedPdfUri,
                onSelect = { pdfPickerLauncher.launch("application/pdf") },
                onClear = onClearPdf,
                isEditing = state.lessonId != null
            )

            FileControl(
                label = "Audio File",
                icon = Icons.Default.Audiotrack,
                remoteUrl = state.existingAudioUrl,
                selectedLocalUri = state.selectedAudioUri,
                onSelect = { audioPickerLauncher.launch("audio/*") },
                onClear = onClearAudio,
                isEditing = state.lessonId != null
            )
        }
    }
}

@Composable
private fun FileControl(
    label: String,
    icon: ImageVector,
    remoteUrl: String?,
    selectedLocalUri: Uri?,
    onSelect: () -> Unit,
    onClear: () -> Unit,
    isEditing: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))

                val hasLocalFile = selectedLocalUri != null
                Column(Modifier.weight(1f)) {
                    val hasRemoteFile = isEditing && remoteUrl != null

                    if (hasLocalFile) {
                        Text("New selection:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = selectedLocalUri?.lastPathSegment ?: "File name not available",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (hasRemoteFile) {
                        Text("Current file:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = remoteUrl ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text("No file selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (hasLocalFile) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear selection")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
            val buttonText = when {
                selectedLocalUri != null -> "Change File"
                isEditing && remoteUrl != null -> "Replace File"
                else -> "Select File"
            }
            Text(buttonText)
        }
    }
}

@Preview(showBackground = true, name = "Add Lesson Mode")
@Composable
private fun AddLessonPreview() {
    HassanAlHawaryTheme {
        Surface {
            AddEditLessonContent(
                state = AddEditLessonUiState.Stable(lessonId = null),
                onTitleChange = {},
                onOrderChange = {},
                onPdfSelected = {},
                onAudioSelected = {},
                onClearPdf = {},
                onClearAudio = {},
                onSaveClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit Lesson Mode")
@Composable
private fun EditLessonPreview() {
    HassanAlHawaryTheme {
        Surface {
            AddEditLessonContent(
                state = AddEditLessonUiState.Stable(
                    lessonId = "123",
                    title = "Existing Lesson",
                    order = "1",
                    existingPdfUrl = "lessons/pdfs/existing.pdf",
                    existingAudioUrl = "lessons/audios/existing.mp3"
                ),
                onTitleChange = {},
                onOrderChange = {},
                onPdfSelected = {},
                onAudioSelected = {},
                onClearPdf = {},
                onClearAudio = {},
                onSaveClick = {}
            )
        }
    }
}
