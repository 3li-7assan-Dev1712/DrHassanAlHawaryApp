package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.UserData
import com.example.hassanalhawary.core.util.GoogleAuthUiClient
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(

    private val googleAuthUiClient: GoogleAuthUiClient
)

    : AuthRepository {
    override suspend fun loginWithGoogle(): LoginResult {
        return googleAuthUiClient.login()
    }

    override suspend fun getLoggedInUser(): UserData? {
//        TODO("Not yet implemented")
        return null
    }
}