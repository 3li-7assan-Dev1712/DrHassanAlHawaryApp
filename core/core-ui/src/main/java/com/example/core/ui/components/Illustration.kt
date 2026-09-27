package com.example.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * The app's illustrations, each with a dark variant (`<name>_dark`: white fills → #2C2C2A,
 * light grays → #3A332E, mid grays → #444441, ink → #D3D1C7, gold unchanged, skin
 * unchanged). Picked by the APP's theme ([Brand.colors]), not the system's: the app has
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
