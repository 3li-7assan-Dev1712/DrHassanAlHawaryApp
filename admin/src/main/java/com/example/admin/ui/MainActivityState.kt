package com.example.admin.ui

import com.example.domain.module.UserData

data class MainActivityState(
    val isAdminLoggedIn: Boolean = false,
    val isUserAdmin: Boolean = false,
    val isUserSuperAdmin: Boolean = false,
    val isAdminConnectedToTelegram: Boolean = false,
    val isLoading: Boolean = true,
    val currentUserDate: UserData? = null,
    val idToken: String? = null
)
