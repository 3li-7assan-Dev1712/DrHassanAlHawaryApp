package com.example.hassanalhawary.core

sealed class LoginRegisterProviderElement {
    object GoogleElement: LoginRegisterProviderElement()
    object FacebookElement: LoginRegisterProviderElement()
    object TelegramElement: LoginRegisterProviderElement()
}