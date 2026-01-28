package com.example.study.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.study.domain.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lesson: Lesson,
    onNavigateBack: () -> Unit,
    onPlayAudioClick: (String) -> Unit,
    onOpenPdfClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(lesson.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { onPlayAudioClick(lesson.audioUrl) }) {
                Text("Play Audio")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onOpenPdfClick(lesson.pdfUrl) }) {
                Text("Open PDF")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonDetailScreenPreview() {
    MaterialTheme {
        LessonDetailScreen(
            lesson = Lesson("1", "Introduction to Islamic Beliefs", "audio_url_1", "pdf_url_1"),
            onNavigateBack = {},
            onPlayAudioClick = {},
            onOpenPdfClick = {}
        )
    }
}
