package com.example.profile.data

import com.example.data_firebase.GoogleAuthUiClient
import com.example.domain.module.SignOutResult
import com.example.domain.module.UserData
import com.example.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(

    private val googleAuthUiClient: GoogleAuthUiClient,
) : ProfileRepository {


    override suspend fun getCurrentUserData(): UserData? =
        googleAuthUiClient.getUserData()


    override suspend fun signOut(): SignOutResult {
        return googleAuthUiClient.signOut()
    }
}