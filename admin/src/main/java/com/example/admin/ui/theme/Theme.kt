package app.netlify.devalihassan.admin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.core.ui.theme.BrandTheme

/**
 * The admin app's theme is core-ui's [com.example.core.ui.theme.HassanAlHawaryTheme]; this
 * only forwards to it. It used to be a full copy, which silently missed anything added
 * there later (the light/dark brand palette the shared sign-in screen reads).
 */
@Composable
fun HassanAlHawaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    brandTheme: BrandTheme = BrandTheme.BROWN,
    content: @Composable () -> Unit
) = com.example.core.ui.theme.HassanAlHawaryTheme(
    darkTheme = darkTheme,
    brandTheme = brandTheme,
    content = content,
)
