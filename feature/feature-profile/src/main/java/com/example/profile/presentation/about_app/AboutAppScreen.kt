package com.example.profile.presentation.about_app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.Illustration
import com.example.core.ui.components.IllustrationBox
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.text.BidiText

private const val WEBSITE_URL = "https://www.dr-alhawary.com"

/**
 * "عن التطبيق": illustration, the app (not the sheikh) as the name with its logo, the
 * version, a description card, and the website + "تواصل معنا" rows. No "what's new" row:
 * the project has no release notes.
 */
@Composable
fun AboutAppScreen(
    onBack: () -> Unit,
    /** Same target as Profile's "الدعم والتواصل". */
    onContact: () -> Unit = {},
    viewModel: AboutViewModel = hiltViewModel()
) {
    val info by viewModel.appInfo.collectAsState()
    val colors = Brand.colors
    val context = LocalContext.current

    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.about_app), onBack = onBack) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IllustrationBox(Illustration.AboutApp, modifier = Modifier.widthIn(max = 210.dp), height = 160.dp)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.admin_logo_app),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, colors.accent, CircleShape),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.about_app_name),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
            }
            Spacer(Modifier.height(4.dp))
            // versionName as-is (Latin digits): it's a technical string.
            Text(
                text = "الإصدار ${info.versionName}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted,
            )

            Spacer(Modifier.height(16.dp))
            Card {
                Text(
                    text = stringResource(R.string.about_app_description),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.8.em),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(Modifier.height(16.dp))
            Card {
                LinkRow(
                    icon = TablerIcons.World,
                    title = stringResource(R.string.about_website),
                    subtitle = BidiText.ltr("dr-alhawary.com"),
                ) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)))
                    } catch (_: ActivityNotFoundException) {
                        // No browser: nothing to open.
                    }
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = colors.divider)
                LinkRow(icon = TablerIcons.Headset, title = stringResource(R.string.about_contact_us), subtitle = null, onClick = onContact)
            }
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Brand.colors.surface,
        border = BorderStroke(0.5.dp, Brand.colors.divider),
    ) {
        Column { content() }
    }
}

@Composable
private fun LinkRow(@DrawableRes icon: Int, title: String, subtitle: String?, onClick: () -> Unit) {
    val colors = Brand.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = colors.textPrimary)
            subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = colors.textMuted) }
        }
        Icon(painterResource(TablerIcons.ChevronLeft), contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
    }
}
