package com.example.hassanalhawary.ui.screens.ask_question_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hassanalhawary.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskQuestionScreen(
    onNavigateBack: () -> Unit,
    onBrowseExistingQuestions: () -> Unit,
    viewModel: AskQuestionViewModel = hiltViewModel(), // Or your ViewModel instance
    onSubmitQuestion: (questionText: String, category: String?) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) } // Example category
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submissionAttempted by remember { mutableStateOf(false) }

    // Dummy categories for the example
    val categories = listOf("Fiqh & Worship", "Aqeedah", "Family Matters", "Character", "General")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask a Question") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    submissionAttempted = true
                    if (questionText.isNotBlank()) {
                        onSubmitQuestion(questionText, selectedCategory)
                        showSuccessDialog = true
                        questionText = "" // Clear field after submission
                        selectedCategory = null
                        submissionAttempted = false
                    }
                },
                icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = "Submit") },
                text = { Text("Submit Question") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                painter = painterResource(R.drawable.lightbulb_icon),
                contentDescription = "Idea",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Seek Knowledge",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Have a question for Shaikh Hassan? Please articulate it clearly. Your pursuit of understanding is valued.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp), // Make it feel substantial
                label = { Text("Your Question") },
                placeholder = { Text("Type your question here...") },
                supportingText = {
                    if (submissionAttempted && questionText.isBlank()) {
                        Text("Question cannot be empty.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Min. 20 characters recommended for clarity.")
                    }
                },
                isError = submissionAttempted && questionText.isBlank(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done // Or ImeAction.Next if category is mandatory
                ),
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(Modifier.height(20.dp))

            // Optional Category Selection
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory ?: "Select Category (Optional)",
                    onValueChange = {}, // Not directly editable
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SuggestionChip(
                onClick = onBrowseExistingQuestions,
                label = { Text("Check Previously Answered Questions") },
                icon = {
                    Icon(
                        Icons.Outlined.Done,
                        contentDescription = "Browse",
                        modifier = Modifier.size(
                            FilterChipDefaults.IconSize
                        )
                    )
                },
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(80.dp)) // Space for FAB
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Question Submitted!") },
            text = { Text("Your question has been sent for review. You'll be notified if it's answered. JazakAllah Khair!") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun AskQuestionScreenPreview() {
    // YourAppTheme { // Apply your theme for accurate preview
    AskQuestionScreen(
        onNavigateBack = {},
        onBrowseExistingQuestions = {},
        onSubmitQuestion = { _, _ -> }
    )
    // }
}