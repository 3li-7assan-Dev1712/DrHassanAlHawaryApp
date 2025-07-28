package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.UserData
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult

interface AuthRepository {

    suspend fun loginWithGoogle(): LoginResult

    suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): LoginResult

    suspend fun getLoggedInUser(): UserData?


}