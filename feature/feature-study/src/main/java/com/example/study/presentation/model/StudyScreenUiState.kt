package com.example.study.presentation.model

import com.example.domain.module.Student

sealed interface StudyScreenUiState {

    data object Loading: StudyScreenUiState

    data class Error(val message: String): StudyScreenUiState

    data class StudentDashboard(val studentData: Student): StudyScreenUiState

    data class NotChannelMember(val studentData: Student): StudyScreenUiState

    data object Guest: StudyScreenUiState

}
