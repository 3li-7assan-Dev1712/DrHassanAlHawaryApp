package com.example.feature.auth.presentation.components

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
import com.example.core.ui.theme.Cairo
import com.example.feature.auth.R

/**
 * Google's own dark-theme button colours (Sign in with Google branding guidelines).
 * They belong to Google's brand, not ours, so they stay here rather than in BrandTokens.
 */
private object GoogleButtonColors {
    val fill = Color(0xFF131314)
    val stroke = Color(0xFF8E918F)
    val text = Color(0xFFE3E3E3)
}

/**
 * "Continue with Google" in Google's dark pill style: 48dp tall, 1dp stroke, the official
 * "G" (cropped from Google's signin-assets kit, never redrawn) at 20dp on the start side.
 * While [isLoading] a spinner takes the logo's place and taps are ignored.
 */
@Composable
fun LoginWithGoogleComp(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onElementClick: () -> Unit
) {
    val loadingDescription = stringResource(R.string.auth_signing_in)
    Surface(
        onClick = onElementClick,
        enabled = !isLoading,
        shape = CircleShape,
        color = GoogleButtonColors.fill,
        contentColor = GoogleButtonColors.text,
        border = BorderStroke(1.dp, GoogleButtonColors.stroke),
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                if (isLoading) stateDescription = loadingDescription
            },
    ) {
        Row(
            modifier = Modifier.padding(ButtonPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = GoogleButtonColors.text,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
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
