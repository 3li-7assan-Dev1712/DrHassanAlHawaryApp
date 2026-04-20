package app.netlify.devalihassan.core.util

sealed class LoginRegisterProviderElement {
    object GoogleElement: LoginRegisterProviderElement()
    object FacebookElement: LoginRegisterProviderElement()
    object TelegramElement: LoginRegisterProviderElement()
}