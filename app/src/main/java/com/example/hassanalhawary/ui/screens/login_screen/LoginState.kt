package com.example.hassanal_hawary.ui.screens.login_screens

data class LoginState(
    val isSignInSuccessful: Boolean = false,
    val showSignInProgressBar: Boolean = false,
    val errorMessage: String? = null,
    val enteredEmail: String = "",
    val enteredPassword: String = "",
    val enterValidEmailMsg: String = "",
    val enterValidPassowrdMsg: String= "",
    val navigateTo: String? = null

    )
