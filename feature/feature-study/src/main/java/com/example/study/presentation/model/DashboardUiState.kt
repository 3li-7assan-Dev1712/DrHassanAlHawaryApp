package com.example.study.presentation.model

import com.example.domain.module.LeaderBoard
import com.example.domain.module.Level
import com.example.domain.module.QuizType

data class DashboardUiState (
    val loadingLevels: Boolean = true,
    val levels: List<Level> = emptyList(),
    val loadingTopStudents: Boolean = true,
    val topStudents: List<LeaderBoard> = emptyList(),
    val loadingMotivationalMessages: Boolean = true,
    val motivationalMessages: List<String> = emptyList(),
    val levelsErrorMessage: String? = null,
    val topStudentsErrorMessage: String? = null,
    val motivationalMessagesErrorMessage: String? = null,
    val latestQuizId: String? = null,
    val latestQuizType: QuizType? = null,
    val hasNewQuiz: Boolean = false,
    val userQuizScore: Int? = null,
    val hasJourneyAnimationPlayed: Boolean = false
)
