package com.example.admin.ui.connect_telegram


import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.core.ui.R

@Composable
fun ConnectTelegramScreen(

) {
    val context = LocalContext.current

    val telegramLoginUrl =
        "https://oauth.telegram.org/auth?bot_id=8255460260&origin=https://dr-hassan-al-hawary.web.app&return_to=https://dr-hassan-al-hawary.web.app/telegram-callback.html&request_access=write"

    // Brand-ish color (Telegram-like). Replace with your own from theme if you have it.
    val telegramBlue = Color(0xFF2AABEE)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // Hero card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon in tinted circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(telegramBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.telegram_img),
                        contentDescription = "Telegram",
                        modifier = Modifier.size(34.dp),
                        tint = Color.Unspecified
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Connect your Telegram",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Link your account to access courses, track progress, and receive new-lesson alerts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Benefit bullets (simple + premium)
                BenefitRow("Instant alerts for new lessons")
                Spacer(Modifier.height(8.dp))
                BenefitRow("Sync progress across devices")
                Spacer(Modifier.height(8.dp))
                BenefitRow("Fast login (no passwords)")

                Spacer(Modifier.height(18.dp))

                // Primary CTA
                Button(
                    onClick = {
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(context, telegramLoginUrl.toUri())

                        // IMPORTANT:
                        // Don't call onConnect() here.
                        // Call it after you receive a valid Telegram callback (deep link / web callback).
                        // onConnect()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = telegramBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.telegram_img),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Connect to Telegram",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Secondary actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                }
            }
        }

        Spacer(Modifier.height(14.dp))


        Text(
            text = "We only use Telegram to verify your membership and deliver course notifications.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minimal “check” dot (no extra icon dependency)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ConnectTelegramScreen(
    )
}