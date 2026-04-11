package com.example.admin.ui.upload_quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Channel
import com.example.domain.module.Question
import com.example.domain.module.QuestionType
import com.example.domain.module.Quiz
import com.example.domain.module.QuizType
import com.example.domain.use_cases.channel.GetChannelsUseCase
import com.example.domain.use_cases.study.UploadQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class UploadQuizUiState(
    val title: String = "",
    val questions: List<Question> = emptyList(),
    val quizType: QuizType = QuizType.WEEKLY,
    val targetLevelId: String? = null,
    val availableChannels: List<Channel> = emptyList(),
    val selectedBatchNames: Set<String> = emptySet(),
    val isActive: Boolean = true,
    val startAt: Long? = null,
    val endAt: Long? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UploadQuizViewModel @Inject constructor(
    private val getChannelsUseCase: GetChannelsUseCase,
    private val uploadQuizUseCase: UploadQuizUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadQuizUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchChannels()
    }

    private fun fetchChannels() {
        viewModelScope.launch {
            getChannelsUseCase().collect { channels ->
                _uiState.update { it.copy(availableChannels = channels) }
            }
        }
    }

    fun toggleBatchSelection(batchName: String) {
        _uiState.update { state ->
            val newSelection = if (state.selectedBatchNames.contains(batchName)) {
                state.selectedBatchNames - batchName
            } else {
                state.selectedBatchNames + batchName
            }
            state.copy(selectedBatchNames = newSelection)
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onTypeChange(type: QuizType) {
        _uiState.update { it.copy(quizType = type) }
    }

    fun onTargetLevelChange(levelId: String) {
        _uiState.update { it.copy(targetLevelId = levelId) }
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

    fun onIsActiveChange(isActive: Boolean) {
        _uiState.update { it.copy(isActive = isActive) }
    }

    fun onStartAtChange(startAt: Long?) {
        _uiState.update { it.copy(startAt = startAt) }
    }

    fun onEndAtChange(endAt: Long?) {
        _uiState.update { it.copy(endAt = endAt) }
    }

    fun uploadQuiz() {
        val state = _uiState.value
        if (state.title.isBlank() || state.questions.isEmpty()) {
            _uiState.update { it.copy(error = "Title and at least one question are required.") }
            return
        }

        if (state.quizType == QuizType.FINAL_EXAM && state.targetLevelId == null) {
            _uiState.update { it.copy(error = "Target Level is required for Final Exams.") }
            return
        }

        if (state.selectedBatchNames.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one channel.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            
            val quiz = Quiz(
                id = UUID.randomUUID().toString(),
                title = state.title,
                questions = state.questions,
                type = state.quizType,
                targetLevelId = state.targetLevelId,
                batchIds = state.selectedBatchNames.toList(),
                isActive = state.isActive,
                startAt = state.startAt?.let { Date(it) },
                endAt = state.endAt?.let { Date(it) }
            )
            
            val result = uploadQuizUseCase(quiz)
            
            result.onSuccess {
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, error = e.message ?: "Upload failed") }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = UploadQuizUiState(availableChannels = _uiState.value.availableChannels)
    }
}
