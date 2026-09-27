package com.example.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.theme.Brand

/**
 * The app's illustrations, each with a warm dark variant (`<name>_dark`: white fills →
 * #3B312A, light grays → #4A3E35, mid grays → #5E5046, ink → #8A7666, gold → #E9B45E,
 * skin unchanged) that sits on the dark background without light-gray glare. Picked by the APP's theme ([Brand.colors]), not the system's: the app has
 * its own light/dark setting, which a `drawable-night` folder wouldn't follow.
 */
enum class Illustration(@DrawableRes val light: Int, @DrawableRes val dark: Int) {
    PersonAtComputer(R.drawable.study_boy, R.drawable.study_boy_dark),
    ComputerAndServer(R.drawable.network_error, R.drawable.network_error_dark),
    PersonAndGlobe(R.drawable.share_app_illu, R.drawable.share_app_illu_dark),
    Journey(R.drawable.journey_illu, R.drawable.journey_illu_dark),
    Document(R.drawable.summary_illu, R.drawable.summary_illu_dark),
    Whiteboard(R.drawable.rate_illu, R.drawable.rate_illu_dark),
    AboutApp(R.drawable.about_app_illu, R.drawable.about_app_illu_dark),
}

/**
 * An existing empty/error state, illustrated: the drawing in a fixed-height box, a title
 * and an optional body. No action here: screens add their own existing retry, if any.
 */
@Composable
fun EmptyState(
    illustration: Illustration,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    height: Dp = 180.dp,
) {
    val colors = Brand.colors
    androidx.compose.foundation.layout.Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IllustrationBox(illustration, height = height)
        androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = colors.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (body != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
            androidx.compose.material3.Text(
                text = body,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/**
 * [illustration] in a fixed-height box, fitted and bottom-aligned, so text below never
 * jumps when the drawing's proportions change (onboarding pages, empty states).
 */
@Composable
fun IllustrationBox(
    illustration: Illustration,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
) {
    Box(modifier = modifier.fillMaxWidth().height(height), contentAlignment = Alignment.BottomCenter) {
        Image(
            painter = painterResource(if (Brand.colors.isDark) illustration.dark else illustration.light),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
