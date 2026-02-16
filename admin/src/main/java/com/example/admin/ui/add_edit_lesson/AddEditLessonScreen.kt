package com.example.admin.ui.add_edit_lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AddEditLessonScreen(
    lessonId: String?, // Null if adding, non-null if editing
    onSave: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var pdfFileName by remember { mutableStateOf<String?>(null) }
    var audioFileName by remember { mutableStateOf<String?>(null) }



    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onSave) {
                Icon(Icons.Default.Save, contentDescription = "Save Lesson")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Lesson Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            FilePickerButton(
                label = "Select PDF",
                fileName = pdfFileName,
                icon = Icons.Default.PictureAsPdf,
                onClick = {
                    // Logic to open file picker for PDF
                    pdfFileName = "selected_lesson.pdf" // Placeholder
                }
            )

            FilePickerButton(
                label = "Select Audio",
                fileName = audioFileName,
                icon = Icons.Default.Audiotrack,
                onClick = {
                    // Logic to open file picker for audio
                    audioFileName = "selected_audio.mp3" // Placeholder
                }
            )
        }
    }
}

@Composable
private fun FilePickerButton(
    label: String,
    fileName: String?,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
        if (fileName != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Selected: $fileName",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddLessonPreview() {
    AddEditLessonScreen(lessonId = null, onSave = {})
}

@Preview(showBackground = true)
@Composable
private fun EditLessonPreview() {
    AddEditLessonScreen(lessonId = "123", onSave = {})
}
