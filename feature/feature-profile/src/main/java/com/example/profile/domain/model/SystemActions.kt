package com.example.profile.domain.model


interface SystemActions {
    fun shareText(text: String)
    fun openPlayStore(appPackageName: String)
    fun openEmail(to: String, subject: String, body: String = "")
    fun openUrl(url: String)
}
