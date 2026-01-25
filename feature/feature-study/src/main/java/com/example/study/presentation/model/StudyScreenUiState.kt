package com.example.study.presentation.model

import com.example.study.domain.model.Student

sealed interface StudyScreenUiState {

    data object Loading: StudyScreenUiState

    data class Error(val message: String): StudyScreenUiState

    data class StudentDashboard(val studentData: Student): StudyScreenUiState

    data object Guest: StudyScreenUiState



}