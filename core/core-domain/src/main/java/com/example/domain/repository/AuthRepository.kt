package com.example.domain.repository

import com.example.domain.module.LoginResult

interface AuthRepository {

    // Platform Activity context (CredentialManager requires it to launch its picker UI).
    // Typed `Any` because this module has no Android SDK dependency; cast to Context in the impl.
    suspend fun loginWithGoogle(activityContext: Any): LoginResult

    suspend fun getLoggedInUser(): LoginResult?

    suspend fun getUserSecurityRole(): String


    suspend fun signOut()

    suspend fun deleteAccount(): Result<Unit>

    fun observeAuthState(): kotlinx.coroutines.flow.Flow<Boolean>
}
