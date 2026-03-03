package com.example.domain.module

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?,
    val idToken: String?,
    val userProfilePictureUrl: String?
)