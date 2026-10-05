package app.netlify.devalihassan.ui.q_a

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.net.toUri
import app.netlify.devalihassan.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.text.ArabicNumerals
import kotlinx.coroutines.launch

private val STEPS = listOf(
    "افتح فاسألوا في تيليجرام",
    "اكتب سؤالك بوضوح واختصار",
    "تابع الرد في المحادثة نفسها",
)

@Composable
fun QAScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    /** Opens the existing Search screen (its route takes no preset filter). */
    onOpenSearch: () -> Unit = {},
) {
    val context = LocalContext.current
    val telegramUrl = stringResource(id = R.string.fasalo_telegram_url)
    val cannotOpen = stringResource(id = R.string.q_a_cannot_open)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = Brand.colors

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(id = R.string.q_a_title), onBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.fasalo_logo),
                contentDescription = stringResource(id = R.string.q_a_title),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "أرسل سؤالك الشرعي",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "فاسألوا هي المنصة الموحدة لأسئلة الفتاوى، وتعمل عبر تيليجرام.",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.7.em),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Card {
                STEPS.forEachIndexed { index, step ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(colors.accentContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = ArabicNumerals.digits(index + 1),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.onAccentContainer,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(text = step, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }

            Card {
                Text(
                    text = "قبل أن تسأل",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
                Text(
                    text = "لعل سؤالك أُجيب عنه من قبل",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
                Spacer(Modifier.height(10.dp))
                // Read-only: tapping it opens the Search screen.
                Surface(
                    onClick = onOpenSearch,
                    shape = RoundedCornerShape(12.dp),
                    color = colors.background,
                    border = BorderStroke(0.5.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(TablerIcons.Search), contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ابحث في الفتاوى المجاب عنها", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                    }
                }
            }

            Button(
                onClick = {
                    if (!openTelegram(context, telegramUrl)) {
                        scope.launch { snackbarHostState.showSnackbar(cannotOpen) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.fasalooTeal, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(painterResource(TablerIcons.Send), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.q_a_open_telegram),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
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
        Column(Modifier.padding(16.dp)) { content() }
    }
}

/**
 * Opens the فاسألوا link (Telegram if installed). If nothing resolves it, retries the same
 * https://t.me link explicitly in a browser. Returns false when both fail.
 */
private fun openTelegram(context: Context, url: String): Boolean {
    val view = Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(view)
        return true
    } catch (_: ActivityNotFoundException) {
        // Fall through to the browser.
    }
    val browser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER).apply {
        data = url.toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(browser)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

@Preview(name = "فاسألوا - light", locale = "ar", widthDp = 360, heightDp = 720)
@Composable
private fun QAScreenLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { QAScreen() }
}

@Preview(name = "فاسألوا - dark", locale = "ar", widthDp = 360, heightDp = 720)
@Composable
private fun QAScreenDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { QAScreen() }
}
