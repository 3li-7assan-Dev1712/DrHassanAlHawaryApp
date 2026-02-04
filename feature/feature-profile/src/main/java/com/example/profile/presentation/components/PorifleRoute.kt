package com.example.profile.presentation.components

sealed class ProfileRoute {
    data object About : ProfileRoute()
    data object Share : ProfileRoute()
    data object Rate : ProfileRoute()
    data object Privacy : ProfileRoute()
    data object Terms : ProfileRoute()
    data object Licenses : ProfileRoute()
    data object Support : ProfileRoute()
}
