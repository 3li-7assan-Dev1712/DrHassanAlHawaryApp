package com.example.study.presentation.model

sealed interface DashboardSection {
    data object Study : DashboardSection
    data object TopStudents : DashboardSection
    data object Summaries : DashboardSection
}