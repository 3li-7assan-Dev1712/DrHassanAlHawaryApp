package com.example.feature.auth.presentation

data class AuthScreenState(
    val isSignInSuccessful: Boolean = false,
    val showSignInProgressBar: Boolean = false,
    val errorMessage: String? = null
)
