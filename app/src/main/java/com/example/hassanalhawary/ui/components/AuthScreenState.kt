package com.example.hassanalhawary.ui.components

data class AuthScreenState(
    val isSignInSuccessful: Boolean = false,
    val showSignInProgressBar: Boolean = false,
    val errorMessage: String? = null,
    val userName: String? = null,
    val enteredEmail: String = "",
    val enteredPassword: String = "",
    val enterValidEmailMsg: String = "",
    val enterValidPassowrdMsg: String= "",
    val navigateTo: String? = null

    )