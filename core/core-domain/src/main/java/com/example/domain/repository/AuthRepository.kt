package com.example.domain.repository

import com.example.domain.module.LoginResult

interface AuthRepository {

    suspend fun loginWithGoogle(): LoginResult

    suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): LoginResult

    suspend fun registerNewUserWithEmailPassword(
        userName: String,
        email: String,
        password: String
    ): LoginResult

    suspend fun getLoggedInUser(): LoginResult?

    suspend fun sendEmailVerification(): Result<Unit>

    suspend fun isEmailVerified(): Boolean

    suspend fun reloadUser(): Result<Unit>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun getUserSecurityRole(): String


}
