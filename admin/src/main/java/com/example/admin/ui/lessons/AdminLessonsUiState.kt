package com.example.admin.ui.lessons

import com.example.domain.module.Lesson

sealed interface AdminLessonsUiState {
    data class Success(val lessons: List<Lesson>) : AdminLessonsUiState
    data class Error(val message: String) : AdminLessonsUiState
    object Loading : AdminLessonsUiState
}