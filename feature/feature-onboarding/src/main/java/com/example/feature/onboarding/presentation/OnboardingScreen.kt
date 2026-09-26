package com.example.feature.onboarding.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.onboarding.R
import kotlinx.coroutines.launch
import kotlin.math.max

data class OnboardingPage(
    @DrawableRes val illustrationRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                illustrationRes = R.drawable.study_boy,
                titleRes = R.string.study,
                descriptionRes = R.string.study_institure_des
            ),
            OnboardingPage(
                illustrationRes = R.drawable.journey_illu,
                titleRes = R.string.journey_title,
                descriptionRes = R.string.journey_description
            ),
            OnboardingPage(
                illustrationRes = R.drawable.network_error,
                titleRes = R.string.network_error_title,
                descriptionRes = R.string.network_error_description
            ),
            OnboardingPage(
                illustrationRes = R.drawable.summary_illu,
                titleRes = R.string.pdf_illu_title,
                descriptionRes = R.string.pdf_illu_descritption
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage
    val isLast = currentPage == pages.lastIndex
    val next = max(0, (currentPage + 1).coerceAtMost(pages.lastIndex))

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            OnboardingBottomBar(
                isLast = isLast,
                onSkip = { onFinished() },
                onNext = {
                    scope.launch {
                        pagerState.animateScrollToPage(next)
                    }
                },
                onGetStarted = { onFinished() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(page = pages[pageIndex])
            }

            Spacer(Modifier.height(8.dp))

            PagerDots(
                pageCount = pages.size,
                currentPage = currentPage
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Image(
            painter = painterResource(page.illustrationRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = stringResource(id = page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(id = page.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PagerDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width = if (isSelected) 18.dp else 8.dp
            val height = 8.dp

            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = width, height = height),
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
            ) {}
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    isLast: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onGetStarted: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkip) {
                Text(stringResource(id = R.string.onboarding_skip))
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(visible = !isLast) {
                Button(onClick = onNext) {
                    Text(stringResource(id = R.string.onboarding_next))
                }
            }

            AnimatedVisibility(visible = isLast) {
                Button(onClick = onGetStarted) {
                    Text(stringResource(id = R.string.onboarding_get_started))
                }
            }
        }
    }
}
