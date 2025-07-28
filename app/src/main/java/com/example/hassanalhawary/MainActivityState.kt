package com.example.hassanalhawary

data class MainActivityState(
    val showProgressBar: Boolean = false,
    val errorMessage: String? = null,
    val navigateTo: String? = null
)