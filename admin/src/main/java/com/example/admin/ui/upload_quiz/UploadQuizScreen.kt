package com.example.admin.ui.upload_quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.R
import com.example.domain.module.Question
import com.example.domain.module.QuestionType
import com.example.domain.module.QuizType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadQuizScreen(
    viewModel: UploadQuizViewModel = hiltViewModel(),
    onUploadSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) {
            onUploadSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.create_quiz)) })
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { viewModel.addTfQuestion() },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.add_tf), modifier = Modifier.padding(8.dp))
                }
                FloatingActionButton(onClick = { viewModel.addMcqQuestion() }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_mcq))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.quiz_title_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Quiz Type Selection ---
            Text(
                stringResource(R.string.quiz_type_label),
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuizType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.quizType == type,
                        onClick = { viewModel.onTypeChange(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            if (uiState.quizType == QuizType.FINAL_EXAM) {
                OutlinedTextField(
                    value = uiState.targetLevelId ?: "",
                    onValueChange = viewModel::onTargetLevelChange,
                    label = { Text(stringResource(R.string.target_level_id_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(uiState.questions) { index, question ->
                    QuestionItem(
                        index = index,
                        question = question,
                        onTextChange = { viewModel.updateQuestionText(question.id, it) },
                        onOptionChange = { optIndex, text ->
                            viewModel.updateMcqOption(
                                question.id,
                                optIndex,
                                text
                            )
                        },
                        onCorrectMcqChange = { viewModel.setMcqCorrectAnswer(question.id, it) },
                        onCorrectTfChange = { viewModel.setTfCorrectAnswer(question.id, it) },
                        onRemove = { viewModel.removeQuestion(question.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            Button(
                onClick = { viewModel.uploadQuiz() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isUploading
            ) {
                if (uiState.isUploading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.upload_quiz))
                }
            }

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun QuestionItem(
    index: Int,
    question: Question,
    onTextChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onCorrectMcqChange: (Int) -> Unit,
    onCorrectTfChange: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Q${index + 1} (${question.type})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove))
                }
            }

            OutlinedTextField(
                value = question.text,
                onValueChange = onTextChange,
                label = { Text(stringResource(R.string.question_text_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (question.type == QuestionType.MCQ) {
                question.options.forEachIndexed { optIndex, option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = question.correctAnswerIndex == optIndex,
                            onClick = { onCorrectMcqChange(optIndex) }
                        )
                        OutlinedTextField(
                            value = option,
                            onValueChange = { onOptionChange(optIndex, it) },
                            label = { Text(stringResource(R.string.option_label, optIndex + 1)) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.correct_answer_label))
                    Text(stringResource(R.string.false_text))
                    Switch(
                        checked = question.correctBooleanAnswer ?: true,
                        onCheckedChange = onCorrectTfChange
                    )
                    Text(stringResource(R.string.true_text))
                }
            }
        }
    }
}
