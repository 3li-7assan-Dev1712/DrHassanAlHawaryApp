package com.example.domain.repository

import com.example.domain.module.LoginResult

interface AuthRepository {

    suspend fun loginWithGoogle(): LoginResult

    suspend fun getLoggedInUser(): LoginResult?

    suspend fun getUserSecurityRole(): String


    suspend fun signOut()

    suspend fun deleteAccount(): Result<Unit>

    fun observeAuthState(): kotlinx.coroutines.flow.Flow<Boolean>
}
