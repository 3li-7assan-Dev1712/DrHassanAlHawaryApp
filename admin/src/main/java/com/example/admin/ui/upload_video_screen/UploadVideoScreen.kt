package com.example.admin.ui.upload_video_screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.use_cases.audios.UploadResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoScreen(
    viewModel: UploadVideoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uploadState by viewModel.uploadState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadResult.Success -> {
                Toast.makeText(context, "Video Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is UploadResult.Error -> {
                val error = (uploadState as UploadResult.Error).message
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload New Video") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Video Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.videoUrl,
                onValueChange = { viewModel.videoUrl = it },
                label = { Text("YouTube URL") },
                placeholder = { Text("https://www.youtube.com/watch?v=...") },
                modifier = Modifier.fillMaxWidth()
            )

            if (uploadState is UploadResult.Progress) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.uploadVideo() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Upload to YouTube List")
                }
            }
        }
    }
}