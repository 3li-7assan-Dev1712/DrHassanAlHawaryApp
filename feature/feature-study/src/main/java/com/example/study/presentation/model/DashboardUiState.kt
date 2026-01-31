package com.example.study.presentation.model

import com.example.domain.module.Level

sealed interface DashboardUiState {

    data object Loading: DashboardUiState

    data class Error(val message: String): DashboardUiState

    data class Success(val levels: List<Level>): DashboardUiState


}