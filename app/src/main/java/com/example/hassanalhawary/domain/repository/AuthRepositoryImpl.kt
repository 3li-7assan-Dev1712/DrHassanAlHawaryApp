package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.core.util.GoogleAuthUiClient
import com.example.hassanalhawary.domain.model.UserData
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(

    private val googleAuthUiClient: GoogleAuthUiClient,
    private val firebaseAuth: FirebaseAuth
)

    : AuthRepository {
    override suspend fun loginWithGoogle(): LoginResult {
        return googleAuthUiClient.login()
    }

    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): LoginResult {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Successfully logged in
                LoginResult(
                    data = UserData(
                        userId = firebaseUser.uid,
                        username = firebaseUser.displayName,
                        userProfilePictureUrl = firebaseUser.photoUrl?.toString()
                    ),
                    errorMessage = null
                )
            } else {
                // Should not happen if await() succeeds without exception but user is null
                LoginResult(
                    data = null,
                    errorMessage = "Login successful but user data not found."
                )
            }
        } catch (e: Exception) {
            LoginResult(
                data = null,
                errorMessage = e.message ?: "Login failed. Please try again."
            )
        }
    }

    override suspend fun getLoggedInUser(): UserData? {
//        TODO("Not yet implemented")
        return null
    }
}