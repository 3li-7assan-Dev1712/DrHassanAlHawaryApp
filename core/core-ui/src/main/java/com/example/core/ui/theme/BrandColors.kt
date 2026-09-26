package com.example.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The fixed dark brand palette: the sign-in screen and the share images (video frame,
 * quote images). One set of values for all of them - add a token here rather
 * than re-typing a hex value in a feature module.
 *
 * Always dark: sign-in and the share images use it regardless of the app's
 * light/dark setting. Home and the bottom bar use [Brand.colors] instead, which
 * follows the setting.
 */
object BrandTokens {
    /** Screen background, and the status/navigation bar areas. */
    val background = Color(0xFF1A1512)
    /** Cards and tiles. */
    val surface = Color(0xFF2C2C2A)
    /** Hairlines and progress tracks. */
    val divider = Color(0xFF444441)
    /** Active navigation item, pills, badges. */
    val goldSoft = Color(0xFFFAC775)
    /** Icons and links ("عرض الكل"). */
    val gold = Color(0xFFEF9F27)
    /** The logo ring. */
    val goldStroke = Color(0xFFBA7517)
    /** Text and icons on [goldSoft]. */
    val onGold = Color(0xFF412402)
    val textPrimary = Color(0xFFF1EFE8)
    val textSecondary = Color(0xFFB4B2A9)
    /** Metadata and inactive navigation. */
    val textMuted = Color(0xFF888780)
}

/**
 * The brand palette for screens that follow the app's light/dark setting (home, the
 * bottom navigation bar). Read it with [Brand.colors]; [HassanAlHawaryTheme] provides
 * [DarkBrandPalette] or [LightBrandPalette]. Same token names as [BrandTokens], which
 * stays the fixed dark palette for sign-in and the share images.
 */
@Immutable
data class BrandPalette(
    val background: Color,
    val surface: Color,
    val divider: Color,
    val goldSoft: Color,
    val gold: Color,
    val goldStroke: Color,
    val onGold: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** True for the dark palette: its screens want light status/navigation bar icons. */
    val isDark: Boolean,
)

val DarkBrandPalette = BrandPalette(
    background = BrandTokens.background,
    surface = BrandTokens.surface,
    divider = BrandTokens.divider,
    goldSoft = BrandTokens.goldSoft,
    gold = BrandTokens.gold,
    goldStroke = BrandTokens.goldStroke,
    onGold = BrandTokens.onGold,
    textPrimary = BrandTokens.textPrimary,
    textSecondary = BrandTokens.textSecondary,
    textMuted = BrandTokens.textMuted,
    isDark = true,
)

/**
 * Warm paper background with white cards. Every text token passes WCAG AA (4.5:1) on both
 * [background] and [surface]; [gold] is darkened from the dark palette's #EF9F27 (2.2:1 on
 * light) so links and icons stay readable. [goldSoft]/[onGold] fills are shared with dark.
 */
val LightBrandPalette = BrandPalette(
    background = Color(0xFFF4EEE5),
    surface = Color(0xFFFFFFFF),
    divider = Color(0xFFE0D6C8),
    goldSoft = BrandTokens.goldSoft,
    gold = Color(0xFF9A5A00),
    goldStroke = BrandTokens.goldStroke,
    onGold = BrandTokens.onGold,
    textPrimary = Color(0xFF2A211E),
    textSecondary = Color(0xFF5C524C),
    textMuted = Color(0xFF6F645A),
    isDark = false,
)

val LocalBrandPalette = staticCompositionLocalOf { DarkBrandPalette }

object Brand {
    /** The brand palette matching the current light/dark setting. */
    val colors: BrandPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandPalette.current
}

// The share images' names for the same palette (plus the few colours only they use).
val BrandGold = BrandTokens.gold
val BrandGoldSoft = BrandTokens.goldSoft
val OnBrandGold = BrandTokens.onGold
/** The share date line and chip text: a lighter gold than [BrandGold], readable at small sizes. */
val BrandGoldLight = Color(0xFFF4B869)
/** The share chip's fill. */
val BrandGoldContainer = Color(0xFF412402)

val ShareFrameBackground = BrandTokens.background
val ShareFrameSurface = BrandTokens.surface
val ShareFrameOnSurface = Color(0xFFFFFFFF)
val ShareFrameSecondaryText = BrandTokens.textSecondary
/** Unplayed waveform bars. */
val ShareFrameMuted = Color(0xFF5F605B)
val ShareFrameDivider = BrandTokens.divider
