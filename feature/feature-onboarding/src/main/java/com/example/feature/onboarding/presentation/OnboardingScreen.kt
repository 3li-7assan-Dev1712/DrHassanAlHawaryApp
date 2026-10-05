package com.example.feature.onboarding.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.lerp
import kotlin.math.abs
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
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
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
        val reduced = reducedMotion
        val rtlSign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            OnboardingPageContent(
                page = PAGES[index],
                // Parallax: the illustration travels at ~60% of the swipe. Read in the draw
                // phase, so swiping doesn't recompose. Off under reduced motion.
                illustrationModifier = if (reduced) Modifier else Modifier.graphicsLayer {
                    val pageOffset = (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
                    translationX = rtlSign * pageOffset * size.width * PARALLAX_LAG
                },
            )
        }

        PagerDots(pageCount = PAGES.size, position = { pagerState.currentPage + pagerState.currentPageOffsetFraction })
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
                // التالي → لنبدأ crossfades on the last page.
                AnimatedContent(
                    targetState = isLast,
                    transitionSpec = { Motion.contentSwap(reduced) },
                    label = "onboardingButton",
                ) { last ->
                    Text(
                        stringResource(id = if (last) R.string.onboarding_get_started else R.string.onboarding_next),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, illustrationModifier: Modifier = Modifier) {
    val colors = Brand.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(illustrationModifier) {
            IllustrationBox(page.illustration, height = 240.dp)
        }
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

/**
 * The active pill follows the finger: each dot's width and colour come straight from the
 * pager position ([position], e.g. 1.4 between the 2nd and 3rd page), so the pill
 * stretches from one dot to the next while swiping. No animation of its own.
 */
@Composable
private fun PagerDots(pageCount: Int, position: () -> Float) {
    val colors = Brand.colors
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { index ->
            // 1 when this dot is the current page, 0 once the pager is a full page away.
            val selectedness by remember(index) {
                derivedStateOf { (1f - abs(position() - index)).coerceIn(0f, 1f) }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = lerp(8.dp, 18.dp, selectedness), height = 8.dp)
                    .background(lerp(colors.divider, colors.accentStrong, selectedness), RoundedCornerShape(50)),
            )
        }
    }
}

/** How far the illustration lags behind its page: it moves at 1 − 0.4 = 60% of the swipe. */
private const val PARALLAX_LAG = 0.4f

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
