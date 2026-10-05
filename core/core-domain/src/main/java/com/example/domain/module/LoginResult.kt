package com.example.domain.module

data class LoginResult (
    val data: UserData? = null,
    val errorMessage: String? = null,
    /** Why sign-in didn't produce a user; null on success. */
    val error: LoginError? = null,
)

enum class LoginError {
    /** The user dismissed Google's account picker. Not an error to show. */
    Cancelled,
    /** No Google account on the device to sign in with. */
    NoAccount,
    /** Couldn't reach Google or Firebase. */
    Network,
    Unknown,
}
