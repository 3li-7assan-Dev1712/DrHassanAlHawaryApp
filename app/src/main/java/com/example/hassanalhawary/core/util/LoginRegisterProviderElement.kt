package com.example.hassanalhawary.core.util

sealed class LoginRegisterProviderElement {
    object GoogleElement: LoginRegisterProviderElement()
    object FacebookElement: LoginRegisterProviderElement()
    object TelegramElement: LoginRegisterProviderElement()
}