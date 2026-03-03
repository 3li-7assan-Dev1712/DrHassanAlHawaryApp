package com.example.study.presentation.model

import com.example.domain.module.User

sealed interface StudyScreenUiState {

    data object Loading: StudyScreenUiState

    data class Error(val message: String): StudyScreenUiState

    data class StudentDashboard(val userData: User): StudyScreenUiState

    data object Guest: StudyScreenUiState

    data object NotAllowed: StudyScreenUiState

}
