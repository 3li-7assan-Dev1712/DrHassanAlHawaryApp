package com.example.study.presentation.model

import com.example.domain.module.Level
import com.example.domain.module.Student

data class DashboardUiState (
    val loadingLevels: Boolean = true,
    val levels: List<Level> = emptyList(),
    val loadingTopStudents: Boolean = true,
    val topStudents: List<Student> = emptyList(),
    val loadingMotivationalMessages: Boolean = true,
    val motivationalMessages: List<String> = emptyList(),
    val levelsErrorMessage: String? = null,
    val topStudentsErrorMessage: String? = null,
    val motivationalMessagesErrorMessage: String? = null
)





