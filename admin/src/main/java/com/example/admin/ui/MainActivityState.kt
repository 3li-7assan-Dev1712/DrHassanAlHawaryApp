package com.example.admin.ui

data class MainActivityState(
    val isAdminLoggedIn: Boolean = false,
    val isAdminConnectedToTelegram: Boolean = false,
    val isLoading: Boolean = true,
)