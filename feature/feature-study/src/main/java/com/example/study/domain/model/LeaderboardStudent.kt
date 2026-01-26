package com.example.study.domain.model

data class LeaderboardStudent(
    val rank: Int,
    val name: String,
    val score: Int,
    val photoUrl: String? = null
)
