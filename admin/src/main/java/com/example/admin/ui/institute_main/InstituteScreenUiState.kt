package com.example.admin.ui.institute_main

import com.example.domain.module.Student

sealed interface InstituteScreenUiState {

    data object Loading: InstituteScreenUiState

    data class Error(val message: String): InstituteScreenUiState

    data class AdminDashboard(val studentData: Student): InstituteScreenUiState

    data object Guest: InstituteScreenUiState



}