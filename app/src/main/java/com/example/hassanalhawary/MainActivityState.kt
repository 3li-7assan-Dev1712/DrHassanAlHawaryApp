package com.example.hassanalhawary

import com.example.domain.module.UserData

data class MainActivityState(
    val showProgressBar: Boolean = false,
    val errorMessage: String? = null,
    val navigateTo: String? = null,
    val isUserLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val showSplashScreen: Boolean = true,
    val currentUserDate: UserData? = null
)