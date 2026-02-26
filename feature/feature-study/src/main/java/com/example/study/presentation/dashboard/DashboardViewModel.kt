package com.example.study.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.GetLatestQuizUseCase
import com.example.domain.use_cases.study.GetLeaderboardUseCase
import com.example.domain.use_cases.study.GetMotivationalMessagesUseCase
import com.example.study.domain.use_case.GetLevelsUseCase
import com.example.study.domain.use_case.SyncLevelsUseCase
import com.example.study.presentation.model.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getLevelsUseCase: GetLevelsUseCase,
    private val syncLevelsUseCase: SyncLevelsUseCase,
    private val getMotivationalMessagesUseCase: GetMotivationalMessagesUseCase,
    private val getLatestQuizUseCase: GetLatestQuizUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    val TAG = "DashboardViewModel"

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Levels
        viewModelScope.launch {
            try {
                syncLevelsUseCase()
                getLevelsUseCase().collect { levels ->
                    if (levels.isNullOrEmpty()) {
                        _uiState.update { it.copy(levelsErrorMessage = "No levels found", loadingLevels = false) }
                    } else {
                        _uiState.update { it.copy(levels = levels, loadingLevels = false) }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Levels error: ${e.message}")
            }
        }

        // Motivational Messages
        viewModelScope.launch {
            try {
                val messages = getMotivationalMessagesUseCase()
                _uiState.update { it.copy(motivationalMessages = messages, loadingMotivationalMessages = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(motivationalMessagesErrorMessage = e.message, loadingMotivationalMessages = false) }
            }
        }

        // Latest Quiz
        viewModelScope.launch {
            try {
                val quiz = getLatestQuizUseCase()
                if (quiz != null) {
                    _uiState.update { it.copy(latestQuizId = quiz.id, hasNewQuiz = true) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching quiz: ${e.message}")
            }
        }

        // Top Students (Leaderboard)
        loadTopStudents()
    }

    fun loadTopStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingTopStudents = true) }
            try {
                val leaderboard = getLeaderboardUseCase()
                _uiState.update { it.copy(topStudents = leaderboard, loadingTopStudents = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingTopStudents = false, topStudentsErrorMessage = e.message) }
            }
        }
    }
}
