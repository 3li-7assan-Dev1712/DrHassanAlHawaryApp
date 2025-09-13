package com.example.hassanalhawary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import backgroundDark
import backgroundLight
import errorContainerDark
import errorContainerLight
import errorDark
import errorLight
import inverseOnSurfaceDark
import inverseOnSurfaceLight
import inversePrimaryDark
import inversePrimaryLight
import inverseSurfaceDark
import inverseSurfaceLight
import onBackgroundDark
import onBackgroundLight
import onErrorContainerDark
import onErrorContainerLight
import onErrorDark
import onErrorLight
import onPrimaryContainerDark
import onPrimaryContainerLight
import onPrimaryDark
import onPrimaryLight
import onSecondaryContainerDark
import onSecondaryContainerLight
import onSecondaryDark
import onSecondaryLight
import onSurfaceDark
import onSurfaceLight
import onSurfaceVariantDark
import onSurfaceVariantLight
import onTertiaryContainerDark
import onTertiaryContainerLight
import onTertiaryDark
import onTertiaryLight
import outlineDark
import outlineLight
import outlineVariantDark
import outlineVariantLight
import primaryContainerDark
import primaryContainerLight
import primaryDark
import primaryLight
import scrimDark
import scrimLight
import secondaryContainerDark
import secondaryContainerLight
import secondaryDark
import secondaryLight
import surfaceBrightDark
import surfaceBrightLight
import surfaceContainerDark
import surfaceContainerHighDark
import surfaceContainerHighLight
import surfaceContainerHighestDark
import surfaceContainerHighestLight
import surfaceContainerLight
import surfaceContainerLowDark
import surfaceContainerLowLight
import surfaceContainerLowestDark
import surfaceContainerLowestLight
import surfaceDark
import surfaceDimDark
import surfaceDimLight
import surfaceLight
import surfaceVariantDark
import surfaceVariantLight
import tertiaryContainerDark
import tertiaryContainerLight
import tertiaryDark
import tertiaryLight

private val lightScheme: ColorScheme
    get() = lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun HassanAlHawaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        /*    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }*/

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CairoTypography,
        content = content
    )
}