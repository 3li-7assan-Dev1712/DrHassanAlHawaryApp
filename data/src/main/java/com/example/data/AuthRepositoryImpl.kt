package com.example.data

import com.example.data_firebase.GoogleAuthUiClient
import com.example.domain.module.LoginResult
import com.example.domain.module.UserData
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
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
        email: String, password: String
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
                        email = firebaseUser.email,
                        userProfilePictureUrl = firebaseUser.photoUrl?.toString()
                    ), errorMessage = null
                )
            } else {
                // Should not happen if await() succeeds without exception but user is null
                LoginResult(
                    data = null, errorMessage = "Login successful but user data not found."
                )
            }
        } catch (e: Exception) {
            LoginResult(
                data = null, errorMessage = e.message ?: "Login failed. Please try again."
            )
        }
    }

    override suspend fun registerNewUserWithEmailPassword(
        userName: String, email: String, password: String
    ): LoginResult {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Optionally update the user's profile with a display name

                if (userName.isNotBlank()) {
                    val profileUpdates =
                        UserProfileChangeRequest.Builder().setDisplayName(userName).build()
                    firebaseUser.updateProfile(profileUpdates).await()
                }

                // Fetch the updated user (or map directly)
                val updatedUser = firebaseAuth.currentUser!! // User should exist and be current
                LoginResult(
                    data = UserData(
                        userId = updatedUser.uid,
                        username = updatedUser.displayName,
                        email = updatedUser.email,
                        userProfilePictureUrl = updatedUser.photoUrl?.toString()
                    ), errorMessage = null
                )
            } else {
                LoginResult(
                    data = null, errorMessage = "Login successful but user data not found."
                )
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            LoginResult(
                data = null, errorMessage = "An account already exists with this email address."
            )
        } catch (e: FirebaseAuthWeakPasswordException) {
            // The password is too weak. Please choose a stronger password.
            LoginResult(
                data = null,
                errorMessage = "The password is too weak. Please choose a stronger password"
            )
        } catch (e: Exception) {
//            Registration failed. Please try again.
            LoginResult(
                data = null, errorMessage = e.message ?: "Registration failed. Please try again."
            )
        }

    }

    override suspend fun getLoggedInUser(): LoginResult? {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            LoginResult(
                data = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    email = firebaseUser.email,
                    userProfilePictureUrl = firebaseUser.photoUrl?.toString()
                ), errorMessage = null
            )
        } else {
            null
        }
    }
}