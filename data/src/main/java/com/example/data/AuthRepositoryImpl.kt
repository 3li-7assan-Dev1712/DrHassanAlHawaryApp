package com.example.data

import android.util.Log
import com.example.data_firebase.GoogleAuthUiClient
import com.example.domain.module.LoginResult
import com.example.domain.module.UserData
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(

    private val googleAuthUiClient: GoogleAuthUiClient,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions
)

    : AuthRepository {
    override suspend fun loginWithGoogle(): LoginResult {
        return googleAuthUiClient.login()
    }

    private val TAG = "AuthRepositoryImpl"

    override suspend fun getLoggedInUser(): LoginResult? {
        try {
            val firebaseUser = firebaseAuth.currentUser

            return if (firebaseUser != null) {
                LoginResult(
                    data = UserData(
                        userId = firebaseUser.uid,
                        username = firebaseUser.displayName,
                        email = firebaseUser.email,
                        idToken = firebaseUser.getIdToken(false).await().token,
                        userProfilePictureUrl = firebaseUser.photoUrl?.toString()
                    ), errorMessage = null
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "getLoggedInUser: ${e.message}")
            return null
        }
    }

    override suspend fun getUserSecurityRole(): String {
        return try {
            // Force refresh the token to get the latest claims from the Cloud Function
            val claims = firebaseAuth.currentUser?.getIdToken(true)?.await()?.claims
            val role = claims?.get("role") as? String
            role ?: "none"
        } catch (e: Exception) {
            Log.d("AuthRepositoryImpl", "getUserSecurityRole: ${e.message}")
            "none"
        }
    }

    override suspend fun signOut() {
        try {
            googleAuthUiClient.signOut()

            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.d(TAG, "signOut: ${e.message}")
        }
    }

    override fun observeAuthState(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser != null)
            }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }


    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseFunctions
                .getHttpsCallable("deleteMyAccount")
                .call()
                .await()

            signOut()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteAccount error: ${e.message}")
            Result.failure(e)
        }
    }
}
