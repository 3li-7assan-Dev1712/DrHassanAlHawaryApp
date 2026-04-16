package com.example.profile.presentation.profile

import com.example.domain.module.SignOutResult
import com.example.domain.module.UserData

data class ProfileUiState(
    val userData: UserData?,
    val signOutResult: SignOutResult? = null,
    val isDeleting: Boolean = false,
    val currentAppVersion: String = "1.0.0"
)
