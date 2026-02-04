package com.example.profile.data


import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.profile.domain.model.SystemActions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemActions {

    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntend = Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntend)
    }

    override fun openPlayStore(appPackageName: String) {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            "market://details?id=$appPackageName".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            context.startActivity(webIntent)
        }
    }

    override fun openEmail(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun openUrl(url: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            url.toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}
