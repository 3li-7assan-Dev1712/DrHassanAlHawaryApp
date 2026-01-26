package com.example.data_firebase.model


data class StudentDto(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val isChannelMember: Boolean = false,
    val isConnectedToTelegram: Boolean = false
)