package com.example.hassanalhawary.ui.screens.login_screen

data class LoginResult (
    val data: UserData? = null,
    val errorMessage: String? = null
)

data class UserData(
    val userId: String,
    val username: String?,
    val userProfilePictureUrl: String?
)
