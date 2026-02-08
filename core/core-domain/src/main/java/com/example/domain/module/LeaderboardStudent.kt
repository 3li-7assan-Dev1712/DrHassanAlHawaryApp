package com.example.domain.module

data class LeaderboardStudent(
    val rank: Int,
    val name: String,
    val score: Int,
    val photoUrl: String? = null
)
