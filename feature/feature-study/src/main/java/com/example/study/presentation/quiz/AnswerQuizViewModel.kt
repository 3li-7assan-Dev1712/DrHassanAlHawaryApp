package com.example.study.presentation.quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.LeaderBoard
import com.example.domain.module.QuestionType
import com.example.domain.module.Quiz
import com.example.domain.module.QuizType
import com.example.domain.use_cases.study.GetLatestQuizUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.SubmitLeaderboardEntryUseCase
import com.example.domain.use_cases.study.SubmitQuizAndPromoteUseCase
import com.example.study.domain.use_case.GetStudentAuthDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
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
    private val getLatestQuizUseCase: GetLatestQuizUseCase,
    private val submitLeaderboardEntryUseCase: SubmitLeaderboardEntryUseCase,
    private val getStudentDataUseCase: GetStudentDataUseCase,
    private val getStudentAuthDataUseCase: GetStudentAuthDataUseCase,
    private val submitQuizAndPromoteUseCase: SubmitQuizAndPromoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnswerQuizUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val quiz = getLatestQuizUseCase()
                if (quiz != null) {
                    _uiState.update { it.copy(quiz = quiz, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No quiz found.") }
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
                // Calculate score
                var score = 0
                quiz.questions.forEach { question ->
                    val userAnswer = state.userAnswers[question.id]
                    if (question.type == QuestionType.MCQ) {
                        if (userAnswer == question.correctAnswerIndex) score++
                    } else {
                        if (userAnswer == question.correctBooleanAnswer) score++
                    }
                }

                // Promotion logic for Final Exam
                Log.d("AnswerQuizViewModel", "submitQuiz: target id: ${quiz.targetLevelId}")
                val uid = getStudentAuthDataUseCase()?.userId ?: ""
                if (quiz.type == QuizType.FINAL_EXAM && quiz.targetLevelId != null) {
                    Log.d("AnswerQuizViewModel", "submitQuiz: should update 1")
                    val answersList = quiz.questions.map { question ->
                        state.userAnswers[question.id] ?: when (question.type) {
                            QuestionType.MCQ -> 0
                            QuestionType.TF -> false
                        }
                    }
                    submitQuizAndPromoteUseCase(answersList)
                }

                val student = getStudentDataUseCase().first()
                if (student != null) {
                    val entry = LeaderBoard(
                        telegramId = student.telegramId,
                        studentName = student.name,
                        telegramPhotoUrl = student.photoUrl ?: "",
                        score = score,
                        answerTimestamp = Date()
                    )
                    submitLeaderboardEntryUseCase(entry).onSuccess {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                submitSuccess = true,
                                finalScore = score
                            )
                        }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                    }
                } else {
                    _uiState.update { it.copy(isSubmitting = false, error = "User not found.") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }
}
