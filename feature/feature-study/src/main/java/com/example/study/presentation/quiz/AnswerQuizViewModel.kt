package com.example.study.presentation.quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.QuestionType
import com.example.domain.module.Quiz
import com.example.domain.use_cases.study.GetQuizWithQuestionsUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.SubmitQuizAndPromoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnswerQuizUiState(
    val quiz: Quiz? = null,
    val userAnswers: Map<String, Any> = emptyMap(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null,
    val finalScore: Int? = null
)

@HiltViewModel
class AnswerQuizViewModel @Inject constructor(
    private val getQuizWithQuestionsUseCase: GetQuizWithQuestionsUseCase,
    private val getStudentDataUseCase: GetStudentDataUseCase,
    private val submitQuizAndPromoteUseCase: SubmitQuizAndPromoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnswerQuizUiState())
    val uiState = _uiState.asStateFlow()

    private val TAG = "AnswerQuizViewModel"
    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val student = getStudentDataUseCase().first()
                val batchId = student?.batch ?: ""

                val quiz = getQuizWithQuestionsUseCase(batchId)
                Log.d(TAG, "loadQuiz: $quiz")
                if (quiz != null) {
                    _uiState.update { it.copy(quiz = quiz, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No active quiz found for your batch.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMcqAnswer(questionId: String, answerIndex: Int) {
        _uiState.update { it.copy(userAnswers = it.userAnswers + (questionId to answerIndex)) }
    }

    fun onTfAnswer(questionId: String, answer: Boolean) {
        _uiState.update { it.copy(userAnswers = it.userAnswers + (questionId to answer)) }
    }

    fun resetQuiz() {
        _uiState.update {
            it.copy(
                userAnswers = emptyMap(),
                submitSuccess = false,
                finalScore = null,
                error = null
            )
        }
    }

    fun submitQuiz() {
        val state = _uiState.value
        val quiz = state.quiz ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                val answersList = quiz.questions.map { question ->
                    state.userAnswers[question.id] ?: when (question.type) {
                        QuestionType.MCQ -> -1 // -1 or null if not answered? Cloud function might expect a specific value
                        QuestionType.TF -> false
                    }
                }

                submitQuizAndPromoteUseCase(quiz.id, answersList).onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true,
                            finalScore = result.score
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }
}
