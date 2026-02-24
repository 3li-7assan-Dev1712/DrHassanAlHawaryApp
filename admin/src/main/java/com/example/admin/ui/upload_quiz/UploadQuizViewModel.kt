package com.example.admin.ui.upload_quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class UploadQuizUiState(
    val title: String = "",
    val questions: List<Question> = emptyList(),
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UploadQuizViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadQuizUiState())
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun addMcqQuestion() {
        val newQuestion = Question(
            id = UUID.randomUUID().toString(),
            type = QuestionType.MCQ,
            options = listOf("", "", "", ""),
            correctAnswerIndex = 0
        )
        _uiState.update { it.copy(questions = it.questions + newQuestion) }
    }

    fun addTfQuestion() {
        val newQuestion = Question(
            id = UUID.randomUUID().toString(),
            type = QuestionType.TF,
            correctBooleanAnswer = true
        )
        _uiState.update { it.copy(questions = it.questions + newQuestion) }
    }

    fun removeQuestion(id: String) {
        _uiState.update { it.copy(questions = it.questions.filter { q -> q.id != id }) }
    }

    fun updateQuestionText(id: String, text: String) {
        _uiState.update { state ->
            state.copy(questions = state.questions.map { 
                if (it.id == id) it.copy(text = text) else it 
            })
        }
    }

    fun updateMcqOption(questionId: String, optionIndex: Int, text: String) {
        _uiState.update { state ->
            state.copy(questions = state.questions.map { q ->
                if (q.id == questionId) {
                    val newOptions = q.options.toMutableList().apply { set(optionIndex, text) }
                    q.copy(options = newOptions)
                } else q
            })
        }
    }

    fun setMcqCorrectAnswer(questionId: String, index: Int) {
        _uiState.update { state ->
            state.copy(questions = state.questions.map { q ->
                if (q.id == questionId) q.copy(correctAnswerIndex = index) else q
            })
        }
    }

    fun setTfCorrectAnswer(questionId: String, answer: Boolean) {
        _uiState.update { state ->
            state.copy(questions = state.questions.map { q ->
                if (q.id == questionId) q.copy(correctBooleanAnswer = answer) else q
            })
        }
    }

    fun uploadQuiz() {
        val state = _uiState.value
        if (state.title.isBlank() || state.questions.isEmpty()) {
            _uiState.update { it.copy(error = "Title and at least one question are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            try {
                val quizRef = firestore.collection("weekly_quiz").document()
                val quiz = Quiz(
                    id = quizRef.id,
                    title = state.title,
                    questions = state.questions
                )
                quizRef.set(quiz).await()
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message ?: "Upload failed") }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = UploadQuizUiState()
    }
}
