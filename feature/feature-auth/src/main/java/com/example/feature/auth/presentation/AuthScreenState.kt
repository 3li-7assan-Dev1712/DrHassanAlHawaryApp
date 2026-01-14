package com.example.feature.auth.presentation

data class AuthScreenState(
    val isSignInSuccessful: Boolean = false,
    val showSignInProgressBar: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "",
    val enteredEmail: String = "",
    val enteredPassword: String = "",
    val enterValidEmailMsg: String = "",
    val enterValidPasswordMsg: String = "",
    val navigateTo: String? = null

)