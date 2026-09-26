package com.example.data_firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.domain.module.LoginError
import com.example.domain.module.LoginResult
import com.example.domain.module.SignOutResult
import com.example.domain.module.UserData
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import javax.inject.Inject

/*
This is the class that will be used to sign in / out/ and get the user data
 */
class GoogleAuthUiClient
@Inject constructor(
    private val context: Context,
    private val credentialManager: CredentialManager,
    private val auth: FirebaseAuth,
) {

    // The button flow: always shows Google's account chooser (including "add account").
    private val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT).build())
        .build()


    suspend fun login(activityContext: Context): LoginResult {
        val googleIdToken = try {
            val credential = credentialManager.getCredential(activityContext, request).credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return failure(LoginError.Unknown, "Unexpected credential type: ${credential.type}")
            }
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: GetCredentialCancellationException) {
            return failure(LoginError.Cancelled, e.message)
        } catch (e: NoCredentialException) {
            return failure(LoginError.NoAccount, e.message)
        } catch (e: GetCredentialInterruptedException) {
            return failure(LoginError.Network, e.message)
        } catch (e: GetCredentialException) {
            Log.d("GoogleAuthClient", "getCredential: ${e.type} ${e.message}")
            return failure(LoginError.Unknown, e.message)
        } catch (e: GoogleIdTokenParsingException) {
            return failure(LoginError.Unknown, e.message)
        }

        // Unchanged: exchange the Google ID token for a Firebase session.
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            LoginResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        email = email,
                        idToken = googleIdToken,
                        userProfilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null,
                error = if (user == null) LoginError.Unknown else null,
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.d("GoogleAuthClient", "login: ${e.message}")
            failure(if (e is FirebaseNetworkException) LoginError.Network else LoginError.Unknown, e.message)
        }
    }

    private fun failure(error: LoginError, message: String?) =
        LoginResult(data = null, errorMessage = message, error = error)


    suspend fun signOut(): SignOutResult {
        return try {
            // Sign out from the Google account on the device.
            // This clears the user's Google session for your app.
            withContext(Dispatchers.IO) {

                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )
                // Also sign out from Firebase.
                auth.signOut()
                SignOutResult(success = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignOutResult(success = false, error = e.message)
        }
    }

    suspend fun getUserData(): UserData? {
        val user = auth.currentUser ?: return null
        return try {
            val tokenResult = user.getIdToken(false).await()
            UserData(
                userId = user.uid,
                username = user.displayName,
                email = user.email,
                idToken = tokenResult.token,
                userProfilePictureUrl = user.photoUrl?.toString()
            )
        } catch (e: Exception) {
            // Fallback: Return data without token if fetching token fails
            UserData(
                userId = user.uid,
                username = user.displayName,
                email = user.email,
                idToken = null,
                userProfilePictureUrl = user.photoUrl?.toString()
            )
        }
    }

}
