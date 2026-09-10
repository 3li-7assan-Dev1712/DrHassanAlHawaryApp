package com.example.core.ui.theme

// green palette
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import backgroundDark
import backgroundDarkGreen
import backgroundLight
import backgroundLightGreen
import errorContainerDark
import errorContainerDarkGreen
import errorContainerLight
import errorContainerLightGreen
import errorDark
import errorDarkGreen
import errorLight
import errorLightGreen
import inverseOnSurfaceDark
import inverseOnSurfaceDarkGreen
import inverseOnSurfaceLight
import inverseOnSurfaceLightGreen
import inversePrimaryDark
import inversePrimaryDarkGreen
import inversePrimaryLight
import inversePrimaryLightGreen
import inverseSurfaceDark
import inverseSurfaceDarkGreen
import inverseSurfaceLight
import inverseSurfaceLightGreen
import onBackgroundDark
import onBackgroundDarkGreen
import onBackgroundLight
import onBackgroundLightGreen
import onErrorContainerDark
import onErrorContainerDarkGreen
import onErrorContainerLight
import onErrorContainerLightGreen
import onErrorDark
import onErrorDarkGreen
import onErrorLight
import onErrorLightGreen
import onPrimaryContainerDark
import onPrimaryContainerDarkGreen
import onPrimaryContainerLight
import onPrimaryContainerLightGreen
import onPrimaryDark
import onPrimaryDarkGreen
import onPrimaryLight
import onPrimaryLightGreen
import onSecondaryContainerDark
import onSecondaryContainerDarkGreen
import onSecondaryContainerLight
import onSecondaryContainerLightGreen
import onSecondaryDark
import onSecondaryDarkGreen
import onSecondaryLight
import onSecondaryLightGreen
import onSurfaceDark
import onSurfaceDarkGreen
import onSurfaceLight
import onSurfaceLightGreen
import onSurfaceVariantDark
import onSurfaceVariantDarkGreen
import onSurfaceVariantLight
import onSurfaceVariantLightGreen
import onTertiaryContainerDark
import onTertiaryContainerDarkGreen
import onTertiaryContainerLight
import onTertiaryContainerLightGreen
import onTertiaryDark
import onTertiaryDarkGreen
import onTertiaryLight
import onTertiaryLightGreen
import outlineDark
import outlineDarkGreen
import outlineLight
import outlineLightGreen
import outlineVariantDark
import outlineVariantDarkGreen
import outlineVariantLight
import outlineVariantLightGreen
import primaryContainerDark
import primaryContainerDarkGreen
import primaryContainerLight
import primaryContainerLightGreen
import primaryDark
import primaryDarkGreen
import primaryLight
import primaryLightGreen
import scrimDark
import scrimDarkGreen
import scrimLight
import scrimLightGreen
import secondaryContainerDark
import secondaryContainerDarkGreen
import secondaryContainerLight
import secondaryContainerLightGreen
import secondaryDark
import secondaryDarkGreen
import secondaryLight
import secondaryLightGreen
import surfaceBrightDark
import surfaceBrightDarkGreen
import surfaceBrightLight
import surfaceBrightLightGreen
import surfaceContainerDark
import surfaceContainerDarkGreen
import surfaceContainerHighDark
import surfaceContainerHighDarkGreen
import surfaceContainerHighLight
import surfaceContainerHighLightGreen
import surfaceContainerHighestDark
import surfaceContainerHighestDarkGreen
import surfaceContainerHighestLight
import surfaceContainerHighestLightGreen
import surfaceContainerLight
import surfaceContainerLightGreen
import surfaceContainerLowDark
import surfaceContainerLowDarkGreen
import surfaceContainerLowLight
import surfaceContainerLowLightGreen
import surfaceContainerLowestDark
import surfaceContainerLowestDarkGreen
import surfaceContainerLowestLight
import surfaceContainerLowestLightGreen
import surfaceDark
import surfaceDarkGreen
import surfaceDimDark
import surfaceDimDarkGreen
import surfaceDimLight
import surfaceDimLightGreen
import surfaceLight
import surfaceLightGreen
import surfaceVariantDark
import surfaceVariantDarkGreen
import surfaceVariantLight
import surfaceVariantLightGreen
import tertiaryContainerDark
import tertiaryContainerDarkGreen
import tertiaryContainerLight
import tertiaryContainerLightGreen
import tertiaryDark
import tertiaryDarkGreen
import tertiaryLight
import tertiaryLightGreen

private val brownLightScheme: ColorScheme
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

private val brownDarkScheme = darkColorScheme(
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

private val greenLightScheme: ColorScheme
    get() = lightColorScheme(
        primary = primaryLightGreen,
        onPrimary = onPrimaryLightGreen,
        primaryContainer = primaryContainerLightGreen,
        onPrimaryContainer = onPrimaryContainerLightGreen,
        secondary = secondaryLightGreen,
        onSecondary = onSecondaryLightGreen,
        secondaryContainer = secondaryContainerLightGreen,
        onSecondaryContainer = onSecondaryContainerLightGreen,
        tertiary = tertiaryLightGreen,
        onTertiary = onTertiaryLightGreen,
        tertiaryContainer = tertiaryContainerLightGreen,
        onTertiaryContainer = onTertiaryContainerLightGreen,
        error = errorLightGreen,
        onError = onErrorLightGreen,
        errorContainer = errorContainerLightGreen,
        onErrorContainer = onErrorContainerLightGreen,
        background = backgroundLightGreen,
        onBackground = onBackgroundLightGreen,
        surface = surfaceLightGreen,
        onSurface = onSurfaceLightGreen,
        surfaceVariant = surfaceVariantLightGreen,
        onSurfaceVariant = onSurfaceVariantLightGreen,
        outline = outlineLightGreen,
        outlineVariant = outlineVariantLightGreen,
        scrim = scrimLightGreen,
        inverseSurface = inverseSurfaceLightGreen,
        inverseOnSurface = inverseOnSurfaceLightGreen,
        inversePrimary = inversePrimaryLightGreen,
        surfaceDim = surfaceDimLightGreen,
        surfaceBright = surfaceBrightLightGreen,
        surfaceContainerLowest = surfaceContainerLowestLightGreen,
        surfaceContainerLow = surfaceContainerLowLightGreen,
        surfaceContainer = surfaceContainerLightGreen,
        surfaceContainerHigh = surfaceContainerHighLightGreen,
        surfaceContainerHighest = surfaceContainerHighestLightGreen,
    )

private val greenDarkScheme = darkColorScheme(
    primary = primaryDarkGreen,
    onPrimary = onPrimaryDarkGreen,
    primaryContainer = primaryContainerDarkGreen,
    onPrimaryContainer = onPrimaryContainerDarkGreen,
    secondary = secondaryDarkGreen,
    onSecondary = onSecondaryDarkGreen,
    secondaryContainer = secondaryContainerDarkGreen,
    onSecondaryContainer = onSecondaryContainerDarkGreen,
    tertiary = tertiaryDarkGreen,
    onTertiary = onTertiaryDarkGreen,
    tertiaryContainer = tertiaryContainerDarkGreen,
    onTertiaryContainer = onTertiaryContainerDarkGreen,
    error = errorDarkGreen,
    onError = onErrorDarkGreen,
    errorContainer = errorContainerDarkGreen,
    onErrorContainer = onErrorContainerDarkGreen,
    background = backgroundDarkGreen,
    onBackground = onBackgroundDarkGreen,
    surface = surfaceDarkGreen,
    onSurface = onSurfaceDarkGreen,
    surfaceVariant = surfaceVariantDarkGreen,
    onSurfaceVariant = onSurfaceVariantDarkGreen,
    outline = outlineDarkGreen,
    outlineVariant = outlineVariantDarkGreen,
    scrim = scrimDarkGreen,
    inverseSurface = inverseSurfaceDarkGreen,
    inverseOnSurface = inverseOnSurfaceDarkGreen,
    inversePrimary = inversePrimaryDarkGreen,
    surfaceDim = surfaceDimDarkGreen,
    surfaceBright = surfaceBrightDarkGreen,
    surfaceContainerLowest = surfaceContainerLowestDarkGreen,
    surfaceContainerLow = surfaceContainerLowDarkGreen,
    surfaceContainer = surfaceContainerDarkGreen,
    surfaceContainerHigh = surfaceContainerHighDarkGreen,
    surfaceContainerHighest = surfaceContainerHighestDarkGreen,
)

@Composable
fun HassanAlHawaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    brandTheme: BrandTheme = BrandTheme.BROWN,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (brandTheme) {
        BrandTheme.BROWN -> if (darkTheme) brownDarkScheme else brownLightScheme
        BrandTheme.GREEN -> if (darkTheme) greenDarkScheme else greenLightScheme
    }

    CompositionLocalProvider(LocalBrandTheme provides brandTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CairoTypography,
            content = content
        )
    }
}
