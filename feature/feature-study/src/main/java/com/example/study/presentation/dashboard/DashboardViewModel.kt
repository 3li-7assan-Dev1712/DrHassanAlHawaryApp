package com.example.study.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.GetLeaderboardUseCase
import com.example.domain.use_cases.study.GetMotivationalMessagesUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.ObserveQuizWithQuestionsUseCase
import com.example.study.domain.use_case.GetLevelsUseCase
import com.example.study.domain.use_case.SyncLevelsUseCase
import com.example.study.domain.use_case.SyncPlaylistsUseCase
import com.example.study.presentation.model.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getLevelsUseCase: GetLevelsUseCase,
    private val syncLevelsUseCase: SyncLevelsUseCase,
    private val syncPlaylistsUseCase: SyncPlaylistsUseCase,
    private val getMotivationalMessagesUseCase: GetMotivationalMessagesUseCase,
    private val observeQuizWithQuestionsUseCase: ObserveQuizWithQuestionsUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val getStudentDataUseCase: GetStudentDataUseCase
) : ViewModel() {

    private val TAG = "DashboardViewModel"

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Levels, Playlists and Lessons
        viewModelScope.launch {
            try {
                syncLevelsUseCase()
                syncPlaylistsUseCase()
                getLevelsUseCase()
                    .catch { e -> Log.e(TAG, "getLevels error: ${e.message}") }
                    .collect { levels ->
                        if (levels.isNullOrEmpty()) {
                            _uiState.update {
                                it.copy(
                                    levelsErrorMessage = "لم يتم العثور على بيانات المراحل الدراسية",
                                    loadingLevels = false
                                )
                            }
                        } else {
                            _uiState.update { it.copy(levels = levels, loadingLevels = false) }

                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Levels error", e)
                _uiState.update { it.copy(levelsErrorMessage = e.message, loadingLevels = false) }
            }
        }

        // Motivational Messages
        viewModelScope.launch {
            try {
                val messages = getMotivationalMessagesUseCase()
                _uiState.update {
                    it.copy(
                        motivationalMessages = messages,
                        loadingMotivationalMessages = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Messages error", e)
                _uiState.update {
                    it.copy(
                        motivationalMessagesErrorMessage = e.message,
                        loadingMotivationalMessages = false
                    )
                }
            }
        }


        // Leaderboard, User Score and Latest Quiz (Reactive pipeline)
        observeQuizAndLeaderboard()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeQuizAndLeaderboard() {
        viewModelScope.launch {
            getStudentDataUseCase()
                .map { it?.batch }
                .distinctUntilChanged()
                .flatMapLatest { batch ->
                    _uiState.update { it.copy(batch = batch) }

                    // Now observe the quiz reactive flow instead of a one-time fetch
                    observeQuizWithQuestionsUseCase(batch ?: "")
                        .flatMapLatest { quiz ->
                            if (quiz != null) {
                                _uiState.update {
                                    it.copy(
                                        latestQuizId = quiz.id,
                                        latestQuizType = quiz.type,
                                        latestQuizTotalQuestions = quiz.questions.size,
                                        hasNewQuiz = true,
                                        loadingTopStudents = true
                                    )
                                }

                                combine(
                                    getLeaderboardUseCase(quiz.id),
                                    getStudentDataUseCase()
                                ) { leaderboard, student ->
                                    val userEntry = if (student != null) {
                                        leaderboard.find { entry -> entry.telegramId == student.telegramId }
                                    } else null

                                    Pair(leaderboard, userEntry?.score)
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        hasNewQuiz = false,
                                        loadingTopStudents = false
                                    )
                                }
                                flowOf(
                                    Pair(
                                        emptyList<com.example.domain.module.LeaderBoard>(),
                                        null as Int?
                                    )
                                )
                            }
                        }
                }
                .catch { e ->
                    Log.e(TAG, "Quiz/Leaderboard observation error", e)
                    _uiState.update {
                        it.copy(
                            loadingTopStudents = false,
                            topStudentsErrorMessage = e.message
                        )
                    }
                }
                .collectLatest { (leaderboard, score) ->
                    Log.d(TAG, "observeQuizAndLeaderboard: $leaderboard $score")
                    _uiState.update {
                        it.copy(
                            topStudents = leaderboard,
                            loadingTopStudents = false,
                            userQuizScore = score
                        )
                    }
                }
        }
    }

    fun onJourneyAnimationFinished() {
        _uiState.update { it.copy(hasJourneyAnimationPlayed = true) }
    }
}
