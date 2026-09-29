package com.example.core.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * Theme switches: every colour moves from its old value to the new one over `medium`
 * instead of the whole app flashing. Instant under reduced motion. On first composition
 * the values start at the target, so nothing animates at launch.
 */
private fun themeSpec(reduced: Boolean): AnimationSpec<Color> =
    if (reduced) snap() else tween(Motion.MEDIUM, easing = Motion.Standard)

@Composable
private fun animated(target: Color, spec: AnimationSpec<Color>): Color {
    val value by animateColorAsState(target, spec, label = "themeColor")
    return value
}

@Composable
internal fun animateBrandPalette(target: BrandPalette, reduced: Boolean): BrandPalette {
    val spec = themeSpec(reduced)
    return target.copy(
        background = animated(target.background, spec),
        surface = animated(target.surface, spec),
        divider = animated(target.divider, spec),
        goldSoft = animated(target.goldSoft, spec),
        gold = animated(target.gold, spec),
        goldText = animated(target.goldText, spec),
        goldStroke = animated(target.goldStroke, spec),
        onGold = animated(target.onGold, spec),
        textPrimary = animated(target.textPrimary, spec),
        textSecondary = animated(target.textSecondary, spec),
        textMuted = animated(target.textMuted, spec),
        surfaceMuted = animated(target.surfaceMuted, spec),
        accent = animated(target.accent, spec),
        accentStrong = animated(target.accentStrong, spec),
        accentContainer = animated(target.accentContainer, spec),
        onAccentContainer = animated(target.onAccentContainer, spec),
        accentText = animated(target.accentText, spec),
        success = animated(target.success, spec),
        successContainer = animated(target.successContainer, spec),
        onSuccessContainer = animated(target.onSuccessContainer, spec),
        danger = animated(target.danger, spec),
        fasalooTeal = animated(target.fasalooTeal, spec),
    )
}

@Composable
internal fun animateColorScheme(target: ColorScheme, reduced: Boolean): ColorScheme {
    val spec = themeSpec(reduced)
    return target.copy(
        primary = animated(target.primary, spec),
        onPrimary = animated(target.onPrimary, spec),
        primaryContainer = animated(target.primaryContainer, spec),
        onPrimaryContainer = animated(target.onPrimaryContainer, spec),
        inversePrimary = animated(target.inversePrimary, spec),
        secondary = animated(target.secondary, spec),
        onSecondary = animated(target.onSecondary, spec),
        secondaryContainer = animated(target.secondaryContainer, spec),
        onSecondaryContainer = animated(target.onSecondaryContainer, spec),
        tertiary = animated(target.tertiary, spec),
        onTertiary = animated(target.onTertiary, spec),
        tertiaryContainer = animated(target.tertiaryContainer, spec),
        onTertiaryContainer = animated(target.onTertiaryContainer, spec),
        background = animated(target.background, spec),
        onBackground = animated(target.onBackground, spec),
        surface = animated(target.surface, spec),
        onSurface = animated(target.onSurface, spec),
        surfaceVariant = animated(target.surfaceVariant, spec),
        onSurfaceVariant = animated(target.onSurfaceVariant, spec),
        surfaceTint = animated(target.surfaceTint, spec),
        inverseSurface = animated(target.inverseSurface, spec),
        inverseOnSurface = animated(target.inverseOnSurface, spec),
        error = animated(target.error, spec),
        onError = animated(target.onError, spec),
        errorContainer = animated(target.errorContainer, spec),
        onErrorContainer = animated(target.onErrorContainer, spec),
        outline = animated(target.outline, spec),
        outlineVariant = animated(target.outlineVariant, spec),
        scrim = animated(target.scrim, spec),
        surfaceBright = animated(target.surfaceBright, spec),
        surfaceDim = animated(target.surfaceDim, spec),
        surfaceContainer = animated(target.surfaceContainer, spec),
        surfaceContainerHigh = animated(target.surfaceContainerHigh, spec),
        surfaceContainerHighest = animated(target.surfaceContainerHighest, spec),
        surfaceContainerLow = animated(target.surfaceContainerLow, spec),
        surfaceContainerLowest = animated(target.surfaceContainerLowest, spec),
    )
}
