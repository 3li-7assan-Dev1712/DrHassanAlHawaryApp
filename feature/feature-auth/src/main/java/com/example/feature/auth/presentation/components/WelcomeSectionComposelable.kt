package com.example.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.R
import com.example.core.ui.theme.BrandTokens
import com.example.feature.auth.R as AuthR

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SheikhPhoto()

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.welcome_to_app),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        if (isAdmin) {
            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = stringResource(R.string.admin_badge),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

/** Photo diameter; shrinks on short screens (and in landscape) to leave room for the button. */
private val PHOTO_SIZE = 120.dp
private val PHOTO_SIZE_COMPACT = 96.dp
private const val COMPACT_HEIGHT_DP = 640
private val PHOTO_RING = 2.dp
private val PHOTO_HALO = 8.dp

/**
 * The sheikh's photo cropped to a circle, one thin gold ring, a soft surface halo and a
 * faint gold glow behind it. Uses the plain square photo: `dr_hassan_image` is a finished
 * logo whose speech-bubble corner shows through any circular clip.
 */
@Composable
private fun SheikhPhoto(modifier: Modifier = Modifier) {
    val photoSize =
        if (LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP) PHOTO_SIZE_COMPACT else PHOTO_SIZE
    Box(
        modifier = modifier
            .size(photoSize + PHOTO_HALO * 2)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandTokens.goldStroke.copy(alpha = 0.10f), Color.Transparent),
                        center = center,
                        radius = size.minDimension,
                    ),
                    radius = size.minDimension,
                )
            }
            .background(BrandTokens.surface.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.dr_hassan_photo),
            contentDescription = stringResource(AuthR.string.auth_sheikh_photo_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(photoSize)
                .clip(CircleShape)
                .border(PHOTO_RING, BrandTokens.goldStroke, CircleShape),
        )
    }
}

@Preview(name = "Welcome Screen", widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun WelcomeScreenPrv() {
    WelcomeScreen(modifier = Modifier)
}

@Preview(name = "Welcome Screen - Admin", widthDp = 320, heightDp = 420, showBackground = true)
@Composable
private fun WelcomeScreenAdminPrv() {
    WelcomeScreen(modifier = Modifier, isAdmin = true)
}
