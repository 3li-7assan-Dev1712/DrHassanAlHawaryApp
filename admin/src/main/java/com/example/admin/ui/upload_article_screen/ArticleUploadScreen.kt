package com.example.admin.ui.upload_article_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleUploadScreen(
    viewModel: ArticleViewModel = hiltViewModel(),
    onArticleUploaded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // --- Side Effects ---
    // Show snackbar for user messages
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ArticleUserEvent.OnUserMessageShown)
        }
    }

    // Navigate away on successful upload
    LaunchedEffect(uiState.isArticleUploaded) {
        if (uiState.isArticleUploaded) {
            onArticleUploaded()
        }
    }

    //
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Upload New Article") })
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Uploading Article...")
            }
        } else {
            // --- Input Form State ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Input
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(ArticleUserEvent.OnTitleChanged(it)) },
                    label = { Text("Article Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                //  Content Input
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onEvent(ArticleUserEvent.OnContentChanged(it)) },
                    label = { Text("Article Content...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                // --- Date Picker Button ---
                val formattedDate = remember(uiState.publishDate) {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                        Date(
                            uiState.publishDate
                        )
                    )
                }
                Button(onClick = { showDatePicker = true }) {
                    Text("Publish Date: $formattedDate")
                }

                // --- Upload Button ---
                Button(
                    onClick = { viewModel.onEvent(ArticleUserEvent.OnUploadClicked) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload Article")
                }
            }
        }
    }

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.publishDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEvent(ArticleUserEvent.OnPublishDateChanged(it))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}