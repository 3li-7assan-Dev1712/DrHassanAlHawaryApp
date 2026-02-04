package com.example.profile.domain.use_case


import com.example.profile.domain.model.SystemActions
import javax.inject.Inject

class ShareAppUseCase @Inject constructor(
    private val actions: SystemActions
) {
    operator fun invoke(shareText: String) = actions.shareText(shareText)
}

class RateAppUseCase @Inject constructor(
    private val actions: SystemActions
) {
    operator fun invoke(packageName: String) = actions.openPlayStore(packageName)
}

class ContactSupportUseCase @Inject constructor(
    private val actions: SystemActions
) {
    fun email(to: String, subject: String, body: String = "") =
        actions.openEmail(to, subject, body)

    fun openUrl(url: String) = actions.openUrl(url)
}
