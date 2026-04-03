package com.example.admin.ui.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.R
import com.example.admin.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.animation.LoadingScreen
import com.example.domain.module.Lesson

@Composable
fun LessonsScreen(
    playlistId: String,
    onAddLesson: () -> Unit,
    lessonsViewModel: AdminLessonsViewModel = hiltViewModel(),
    onEditLesson: (String) -> Unit
) {
    val state by lessonsViewModel.uiState.collectAsState()
    var lessonToDelete by remember { mutableStateOf<Lesson?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLesson) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_lesson))
            }
        }
    ) {

        when (val currentState = state) {
            is AdminLessonsUiState.Error -> {
                Text(text = currentState.message)
            }

            AdminLessonsUiState.Loading -> {
                LoadingScreen()
            }

            is AdminLessonsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentState.lessons) { lesson ->
                        LessonItem(
                            lesson = lesson,
                            onEditClick = { onEditLesson(lesson.id) },
                            onDeleteClick = { lessonToDelete = lesson }
                        )
                    }
                }
            }
        }
    }

    lessonToDelete?.let { lesson ->
        AlertDialog(
            onDismissRequest = { lessonToDelete = null },
            title = { Text(text = stringResource(R.string.delete)) },
            text = { Text(text = stringResource(R.string.delete_confirmation_msg, lesson.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        lessonsViewModel.deleteLesson(lesson.id)
                        lessonToDelete = null
                    }
                ) {
                    Text(text = stringResource(R.string.delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { lessonToDelete = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun LessonItem(
    lesson: Lesson,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.duration, lesson.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_lesson)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonsScreenPreview() {
    HassanAlHawaryTheme {
        Surface {
            LessonsScreen("123", onAddLesson = {}, onEditLesson = {})
        }
    }
}
