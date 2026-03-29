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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.R
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
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ArticleUserEvent.OnUserMessageShown)
        }
    }

    LaunchedEffect(uiState.isArticleUploaded) {
        if (uiState.isArticleUploaded) {
            onArticleUploaded()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(title = { 
                Text(
                    if (uiState.articleId == null) 
                        stringResource(R.string.upload_new_article) 
                    else 
                        stringResource(R.string.edit_article)
                ) 
            })
        }
    ) { paddingValues ->
        // Keep showing loading if we are uploading OR if we just finished successfully (waiting for nav)
        if (uiState.isLoading || uiState.isArticleUploaded) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.loading))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(ArticleUserEvent.OnTitleChanged(it)) },
                    label = { Text(stringResource(R.string.article_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onEvent(ArticleUserEvent.OnContentChanged(it)) },
                    label = { Text(stringResource(R.string.article_content_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                val formattedDate = remember(uiState.publishDate) {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                        Date(uiState.publishDate)
                    )
                }
                Button(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.publish_date_label, formattedDate))
                }

                Button(
                    onClick = { viewModel.onEvent(ArticleUserEvent.OnUploadClicked) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.articleId == null) 
                            stringResource(R.string.upload_article_button) 
                        else 
                            stringResource(R.string.save)
                    )
                }
            }
        }
    }

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
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
