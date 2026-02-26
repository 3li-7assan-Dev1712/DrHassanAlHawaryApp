package com.example.study.presentation.quiz

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.module.Question
import com.example.domain.module.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerQuizScreen(
    viewModel: AnswerQuizViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.quiz?.title ?: "الاختبار الأسبوعي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (uiState.submitSuccess) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("تم تسليم الاختبار بنجاح!", style = MaterialTheme.typography.headlineSmall)
                    Text("نتيجتك: ${uiState.finalScore}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("العودة للرئيسية")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.quiz?.questions ?: emptyList()) { question ->
                        QuizQuestionItem(
                            question = question,
                            userAnswer = uiState.userAnswers[question.id],
                            onMcqAnswer = { viewModel.onMcqAnswer(question.id, it) },
                            onTfAnswer = { viewModel.onTfAnswer(question.id, it) }
                        )
                    }

                    item {
                        Button(
                            onClick = { viewModel.submitQuiz() },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            enabled = !uiState.isSubmitting
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("تسليم الإجابات")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizQuestionItem(
    question: Question,
    userAnswer: Any?,
    onMcqAnswer: (Int) -> Unit,
    onTfAnswer: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (question.type == QuestionType.MCQ) {
                question.options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = userAnswer == index,
                            onClick = { onMcqAnswer(index) }
                        )
                        Text(text = option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = userAnswer == true, onClick = { onTfAnswer(true) })
                        Text("صح")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = userAnswer == false, onClick = { onTfAnswer(false) })
                        Text("خطأ")
                    }
                }
            }
        }
    }
}
