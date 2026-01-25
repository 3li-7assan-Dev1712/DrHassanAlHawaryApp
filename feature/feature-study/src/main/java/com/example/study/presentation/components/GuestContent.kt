package com.example.study.presentation.components

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.core.ui.R

@Composable
fun GuestContent(
    onConnect: () -> Unit
) {

    val context = LocalContext.current
    val telegramLoginUrl =
        "https://oauth.telegram.org/auth?bot_id=8255460260&origin=https://dr-hassan-al-hawary.web.app&return_to=https://dr-hassan-al-hawary.web.app/telegram-callback.html&request_access=write"



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /*
        Image(
            painter = painterResource(id = R.drawable.student_zone_illustration),
            contentDescription = "Study Zone Illustration",
            modifier = Modifier.size(150.dp)
        )
         */
        Icon(
            painter = painterResource(id = R.drawable.telegram_logo),
            contentDescription = "Telegram",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
        Text(
            text = "Connect to Your Student Account",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Link your Telegram account to access your courses, progress, and alerts.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Button(onClick = {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, telegramLoginUrl.toUri())
            onConnect()
        }) {
            Text("Connect to Telegram")
        }
    }
}