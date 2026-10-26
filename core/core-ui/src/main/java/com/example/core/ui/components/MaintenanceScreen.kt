package com.example.core.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.HassanAlHawaryTheme

/**
 * Shown instead of the whole app while `config/app_config.maintenanceMode` is on (MainActivity).
 * Figma Maintenance: phone `36:1450` / `36:1471`, Medium `64:6927` / `64:6945`, Expanded
 * `61:3935` / `61:3953`. One layout for every window: a column as wide as the phone, at most 520
 * on a tablet, with the message centred in the space above the actions. No rail.
 *
 * [message] (the admin's maintenanceMessage) replaces the two default paragraphs when it isn't
 * blank. «إعادة المحاولة» asks the server again ([onRetry]) and reads «جارٍ التحقق…» while
 * [isRetrying]. The column scrolls when a long message or a large font doesn't fit. It keeps
 * clear of [windowInsets] (the system bars; tests pass none to measure against Figma).
 *
 * Not drawn: the tablet frames' optional ETA chip (the config has no return time; the message
 * can say it).
 */
@Composable
fun MaintenanceScreen(
    message: String,
    isRetrying: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.systemBars,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brand.colors.background)
            .windowInsetsPadding(windowInsets),
        contentAlignment = Alignment.TopCenter,
    ) {
        val viewportHeight = if (constraints.hasBoundedHeight) maxHeight else 0.dp
        Column(
            modifier = Modifier
                .widthIn(max = MaintenanceColumnMaxWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Figma's two equal flexible spaces: SpaceBetween over (nothing, message, actions)
            // centres the message above the actions, and unlike weights it still scrolls.
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier)
            MaintenanceMessage(message)
            MaintenanceActions(isRetrying = isRetrying, onRetry = onRetry)
        }
    }
}

@Composable
private fun MaintenanceMessage(message: String) {
    val colors = Brand.colors
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(MaintenanceCircleSize)
                .background(colors.accentContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(TablerIcons.Tool),
                contentDescription = null,
                tint = colors.onAccentContainer,
                modifier = Modifier.size(MaintenanceIconSize),
            )
        }
        Spacer(Modifier.height(MaintenanceIconToTexts))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaintenanceTextsPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaintenanceTextsSpacing),
        ) {
            Text(
                text = stringResource(R.string.maintenance_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
            )
            val paragraphs = if (message.isBlank()) {
                listOf(stringResource(R.string.maintenance_body), stringResource(R.string.maintenance_apology))
            } else {
                listOf(message)
            }
            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaintenanceBodyStyle,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Button/Secondary «إعادة المحاولة», then the 48dp text button to the Telegram channel. */
@Composable
private fun MaintenanceActions(isRetrying: Boolean, onRetry: () -> Unit) {
    val colors = Brand.colors
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MaintenanceActionsPadding,
                end = MaintenanceActionsPadding,
                bottom = MaintenanceActionsBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(MaintenanceActionsSpacing),
    ) {
        OutlinedButton(
            onClick = onRetry,
            enabled = !isRetrying,
            shape = CircleShape,
            border = BorderStroke(1.dp, if (isRetrying) colors.divider else colors.textMuted),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.textPrimary,
                disabledContentColor = colors.textMuted,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(MaintenanceActionHeight),
        ) {
            Text(
                text = stringResource(if (isRetrying) R.string.maintenance_checking else R.string.maintenance_retry),
                style = MaintenanceLabelStyle,
            )
        }
        TextButton(
            onClick = {
                // No app for t.me links: nothing happens rather than a crash.
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, MaintenanceTelegramUrl.toUri()))
                }
            },
            colors = ButtonDefaults.textButtonColors(contentColor = colors.accentText),
            modifier = Modifier
                .fillMaxWidth()
                .height(MaintenanceActionHeight),
        ) {
            Text(text = stringResource(R.string.maintenance_telegram), style = MaintenanceLabelStyle)
        }
    }
}

/** Figma Maintenance, in dp. The column's width is the first-run screens' 520. */
private val MaintenanceColumnMaxWidth = AdaptivePanesDefaults.FirstRunColumnWidth
private val MaintenanceCircleSize = 96.dp
private val MaintenanceIconSize = 44.dp
private val MaintenanceIconToTexts = 24.dp
private val MaintenanceTextsPadding = 32.dp
private val MaintenanceTextsSpacing = 12.dp
private val MaintenanceActionsPadding = 16.dp
private val MaintenanceActionsBottom = 24.dp
private val MaintenanceActionsSpacing = 4.dp
private val MaintenanceActionHeight = 48.dp

/** The Sheikh's official channel (as on «عن الشيخ»). */
private const val MaintenanceTelegramUrl = "https://t.me/Dr_alhawary"

/** Figma: Cairo Regular 15/26. */
private val MaintenanceBodyStyle = TextStyle(
    fontFamily = Cairo,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 26.sp,
)

/** Figma Material/labelLarge: Cairo Medium 14/20. */
private val MaintenanceLabelStyle = TextStyle(
    fontFamily = Cairo,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

@Preview(name = "Maintenance - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun MaintenanceCompactPreview() {
    HassanAlHawaryTheme(darkTheme = false) { MaintenanceScreen(message = "", isRetrying = false, onRetry = {}) }
}

@Preview(name = "Maintenance - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun MaintenanceCompactDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { MaintenanceScreen(message = "", isRetrying = false, onRetry = {}) }
}

@Preview(name = "Maintenance - compact, admin message, retrying", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun MaintenanceMessagePreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        MaintenanceScreen(
            message = "نحدّث قاعدة البيانات، ونعود بإذن الله الساعة التاسعة مساءً.",
            isRetrying = true,
            onRetry = {},
        )
    }
}

@Preview(name = "Maintenance - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun MaintenanceMediumPreview() {
    HassanAlHawaryTheme(darkTheme = false) { MaintenanceScreen(message = "", isRetrying = false, onRetry = {}) }
}

@Preview(name = "Maintenance - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun MaintenanceMediumDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { MaintenanceScreen(message = "", isRetrying = false, onRetry = {}) }
}

@Preview(name = "Maintenance - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun MaintenanceExpandedPreview() {
    HassanAlHawaryTheme(darkTheme = false) { MaintenanceScreen(message = "", isRetrying = false, onRetry = {}) }
}

@Preview(name = "Maintenance - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun MaintenanceExpandedDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { MaintenanceScreen(message = "", isRetrying = false, onRetry = {}) }
}
