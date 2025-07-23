package com.example.hassanalhawary.ui.screens.login_screen

import com.example.hassanalhawary.domain.model.UserData

data class LoginResult (
    val data: UserData? = null,
    val errorMessage: String? = null
)
