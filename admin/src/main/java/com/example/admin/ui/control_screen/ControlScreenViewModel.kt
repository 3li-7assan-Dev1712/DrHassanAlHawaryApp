package com.example.admin.ui.control_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Quiz
import com.example.domain.use_cases.study.GetAllQuizzesUseCase
import com.example.domain.use_cases.study.UpdateQuizControlsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ControlScreenUiState(
    val quizzes: List<Quiz> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ControlScreenViewModel @Inject constructor(
    private val getAllQuizzesUseCase: GetAllQuizzesUseCase,
    private val updateQuizControlsUseCase: UpdateQuizControlsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControlScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val quizzes = getAllQuizzesUseCase()
                _uiState.update { it.copy(quizzes = quizzes, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleQuizActiveStatus(quizId: String, newStatus: Boolean, startAt: Long?, endAt: Long?) {
        // Optimistic UI update
        _uiState.update { state ->
            val updatedQuizzes = state.quizzes.map { 
                if (it.id == quizId) it.copy(isActive = newStatus) else it 
            }
            state.copy(quizzes = updatedQuizzes)
        }

        viewModelScope.launch {
            // Let it toggle silently without a blocking loading spinner if preferred, 
            // but we can just rely on the optimistic update.
            val result = updateQuizControlsUseCase(quizId, newStatus, startAt, endAt)
            result.onSuccess {
                // Background refresh to sync exact state just in case
                loadQuizzes()
            }.onFailure { e ->
                // If it fails, refresh to revert to the true server state
                loadQuizzes()
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}