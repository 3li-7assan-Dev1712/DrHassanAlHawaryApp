package com.example.profile.domain.repository

import com.example.domain.module.SignOutResult
import com.example.domain.module.UserData


interface ProfileRepository {

    fun getCurrentUserData(): UserData?

    suspend fun signOut(): SignOutResult







}