package app.netlify.devalihassan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.core.ui.theme.BrandTheme

/**
 * The app's theme is core-ui's [com.example.core.ui.theme.HassanAlHawaryTheme]; this only
 * forwards to it. It used to be a full copy, which silently missed anything added there
 * later (the light/dark brand palette for home, the bottom bar and sign-in).
 */
@Composable
fun HassanAlHawaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    brandTheme: BrandTheme = BrandTheme.BROWN,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) = com.example.core.ui.theme.HassanAlHawaryTheme(
    darkTheme = darkTheme,
    brandTheme = brandTheme,
    dynamicColor = dynamicColor,
    content = content,
)
