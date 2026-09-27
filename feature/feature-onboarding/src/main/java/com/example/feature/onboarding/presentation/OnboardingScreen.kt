package com.example.feature.onboarding.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.core.ui.components.Illustration
import com.example.core.ui.components.IllustrationBox
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.feature.onboarding.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val illustration: Illustration,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

private val PAGES = listOf(
    OnboardingPage(Illustration.PersonAtComputer, R.string.onboarding_all_title, R.string.onboarding_all_body),
    OnboardingPage(Illustration.ComputerAndServer, R.string.onboarding_offline_title, R.string.onboarding_offline_body),
    OnboardingPage(Illustration.PersonAndGlobe, R.string.onboarding_share_title, R.string.onboarding_share_body),
)

/**
 * Three pages on the app background (following the app theme): a fixed-height
 * illustration box so titles never jump, title, body. "تخطي" and "التالي" / "لنبدأ" sit
 * directly on the page background (no tinted bar behind them).
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val colors = Brand.colors
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == PAGES.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            OnboardingPageContent(PAGES[index])
        }

        PagerDots(pageCount = PAGES.size, currentPage = pagerState.currentPage)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onFinished) {
                Text(stringResource(id = R.string.onboarding_skip), color = colors.textMuted)
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (isLast) onFinished() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentStrong, contentColor = colors.onGold),
            ) {
                Text(
                    stringResource(id = if (isLast) R.string.onboarding_get_started else R.string.onboarding_next),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val colors = Brand.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IllustrationBox(page.illustration, height = 240.dp)
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(id = page.titleRes),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(id = page.descriptionRes),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.7.em),
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PagerDots(pageCount: Int, currentPage: Int) {
    val colors = Brand.colors
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (selected) 18.dp else 8.dp, height = 8.dp),
                shape = RoundedCornerShape(50),
                color = if (selected) colors.accentStrong else colors.divider,
            ) {}
        }
    }
}

@Preview(name = "Onboarding - light", locale = "ar", widthDp = 360, heightDp = 720)
@Composable
private fun OnboardingLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { OnboardingScreen(onFinished = {}) }
}

@Preview(name = "Onboarding - dark", locale = "ar", widthDp = 360, heightDp = 720)
@Composable
private fun OnboardingDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { OnboardingScreen(onFinished = {}) }
}
