package com.example.hassanalhawary.core

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.hassanalhawary.R
import com.example.hassanalhawary.domain.model.UserData
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
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


    private fun getSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id)).build()
        ).setAutoSelectEnabled(true).build()
    }

    suspend fun signOut() {/*try {
            onTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }*/

    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid, username = displayName, userProfilePictureUrl = photoUrl?.toString()
        )
    }
}