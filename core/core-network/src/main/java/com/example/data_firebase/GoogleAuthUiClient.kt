package com.example.data_firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.domain.module.LoginResult
import com.example.domain.module.SignOutResult
import com.example.domain.module.UserData
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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

    val googleIdOption =
        GetGoogleIdOption.Builder().setServerClientId(context.getString(R.string.web_client_id))
            .setFilterByAuthorizedAccounts(false).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()


    suspend fun login(): LoginResult {

        val result = credentialManager.getCredential(context, request)

        val credential = result.credential as CustomCredential
        val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
        val googleIdToken = googleIdTokenCredential.idToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            LoginResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        userProfilePictureUrl = photoUrl?.toString()
                    )
                }, errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            LoginResult(
                data = null, errorMessage = e.message
            )
        }
    }


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

    fun getUserData(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            userProfilePictureUrl = photoUrl?.toString()
        )
    }

}