package com.example.study.presentation.lessons

import com.example.domain.module.Lesson

sealed interface LessonsUiState {

    data object Loading: LessonsUiState

    data class Error(val message: String): LessonsUiState

    data class Success(val lessons: List<Lesson>): LessonsUiState


}