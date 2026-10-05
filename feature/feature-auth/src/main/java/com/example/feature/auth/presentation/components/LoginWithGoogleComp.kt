package com.example.feature.auth.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import com.example.feature.auth.R

/**
 * Google's own button themes (Sign in with Google branding guidelines), dark and light,
 * each with the official "G" cropped from that theme's asset (the crop keeps the theme's
 * fill as its backdrop). Google's brand, not ours, so they stay here.
 */
private class GoogleButtonStyle(
    val fill: Color,
    val stroke: Color,
    val text: Color,
    @DrawableRes val logo: Int,
)

private val GoogleDark = GoogleButtonStyle(
    fill = Color(0xFF131314),
    stroke = Color(0xFF8E918F),
    text = Color(0xFFE3E3E3),
    logo = R.drawable.ic_google_logo,
)

private val GoogleLight = GoogleButtonStyle(
    fill = Color(0xFFFFFFFF),
    stroke = Color(0xFF747775),
    text = Color(0xFF1F1F1F),
    logo = R.drawable.ic_google_logo_light,
)

/**
 * "Continue with Google" in Google's pill style, dark or light with the app theme: 48dp
 * tall, 1dp stroke, the official
 * "G" (cropped from Google's signin-assets kit, never redrawn) at 20dp on the start side.
 * While [isLoading] the button shrinks to a 48dp circle with a spinner (the label
 * crossfades out) and taps are ignored; on failure it grows back with the label.
 */
@Composable
fun LoginWithGoogleComp(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onElementClick: () -> Unit
) {
    val loadingDescription = stringResource(R.string.auth_signing_in)
    val google = if (Brand.colors.isDark) GoogleDark else GoogleLight
    val reduced = reducedMotion
    // The caller's width (full width) is kept by this box; the pill inside animates its own.
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            onClick = onElementClick,
            enabled = !isLoading,
            shape = CircleShape,
            color = google.fill,
            contentColor = google.text,
            border = BorderStroke(1.dp, google.stroke),
            modifier = Modifier
                .animateContentSize(if (reduced) snap() else tween(Motion.MEDIUM, easing = Motion.Standard))
                .then(if (isLoading) Modifier.size(48.dp) else Modifier.fillMaxWidth())
                .heightIn(min = 48.dp)
                .semantics {
                    if (isLoading) stateDescription = loadingDescription
                },
        ) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { Motion.contentSwap(reduced) },
                contentAlignment = Alignment.Center,
                label = "signIn",
            ) { loading ->
                if (loading) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = google.text,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(ButtonPadding),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(google.logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.auth_continue_with_google),
                            fontFamily = Cairo,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Google's Android padding: 12dp before the logo, 10dp after it, 12dp after the text. */
private val ButtonPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)

@Preview(showBackground = true, backgroundColor = 0xFF1A1512, widthDp = 320)
@Composable
private fun LoginWithGoogleCompPreview() {
    LoginWithGoogleComp(modifier = Modifier.fillMaxWidth()) {}
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1512, widthDp = 320, name = "Loading")
@Composable
private fun LoginWithGoogleCompLoadingPreview() {
    LoginWithGoogleComp(modifier = Modifier.fillMaxWidth(), isLoading = true) {}
}
