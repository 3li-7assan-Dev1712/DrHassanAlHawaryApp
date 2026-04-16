package com.example.data

import android.util.Log
import com.example.data_firebase.GoogleAuthUiClient
import com.example.domain.module.LoginResult
import com.example.domain.module.UserData
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
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
    override suspend fun loginWithEmailAndPassword(
        email: String, password: String
    ): LoginResult {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            val methods = firebaseAuth.fetchSignInMethodsForEmail(email).await()

            val providers = methods.signInMethods ?: emptyList()

            if (providers.contains("google.com") && !providers.contains("password")) {
                return LoginResult(
                    data = null,
                    errorMessage = "هذا الحساب مربوط بحسابك قوقل الرجاء الدخول عن طريق قوقل وليس الايميل"
                )
            }
            if (firebaseUser != null) {
                // Successfully logged in
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
                        idToken = firebaseUser.getIdToken(false).await().token,
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

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified ?: false
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val methods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
            val providers = methods.signInMethods ?: emptyList()

            if (providers.contains("google.com") && !providers.contains("password")) {
                return Result.failure(
                    Exception("This account uses Google login. Password reset is not available.")
                )
            }

            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
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


    // helper functions
    suspend fun linkEmailPassword(email: String, password: String): Result<Unit> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)

            firebaseAuth.currentUser
                ?.linkWithCredential(credential)
                ?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
