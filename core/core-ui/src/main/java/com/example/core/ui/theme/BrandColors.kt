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
    /** Fills (pills, the play button) under [onGold] content. */
    val goldSoft: Color,
    val gold: Color,
    /**
     * Gold for text and icons that sit directly on [background]: the active nav tab, the
     * sign-in name. [goldSoft] in dark; in light that pale gold is ~1.5:1, so deep gold.
     */
    val goldText: Color,
    val goldStroke: Color,
    val onGold: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** True for the dark palette: its screens want light status/navigation bar icons. */
    val isDark: Boolean,
    // Semantic tokens for the secondary screens (overnight UI pass). Screen code reads these,
    // never raw hex.
    /** Segmented-control track and other subtle fills. */
    val surfaceMuted: Color = surface,
    /** Outline icons. */
    val accent: Color = gold,
    /** Progress bars, active chip border, timeline dots. */
    val accentStrong: Color = gold,
    /** Chips, pills, icon circles. */
    val accentContainer: Color = goldSoft,
    /** Text and icons on [accentContainer]. */
    val onAccentContainer: Color = onGold,
    /** Links ("عرض الكل") and accent subtitles. */
    val accentText: Color = goldText,
    val success: Color = Color(0xFF1D9E75),
    val successContainer: Color = Color(0xFFE1F5EE),
    val onSuccessContainer: Color = Color(0xFF085041),
    /** Destructive actions (delete account). */
    val danger: Color = Color(0xFFA32D2D),
    /** The فاسألوا button, under white text. */
    val fasalooTeal: Color = Color(0xFF0F6E56),
    /** The tablet's immersive designs viewer: dark in both themes (Figma viewerBackground). */
    val viewerBackground: Color = Color(0xFF0B0B0B),
    /** Text, icons and outlines on [viewerBackground] (Figma onViewerBackground). */
    val onViewerBackground: Color = Color(0xFFF1EFE8),
)

val DarkBrandPalette = BrandPalette(
    background = BrandTokens.background,
    surface = BrandTokens.surface,
    divider = BrandTokens.divider,
    goldSoft = BrandTokens.goldSoft,
    gold = BrandTokens.gold,
    goldText = BrandTokens.goldSoft,
    goldStroke = BrandTokens.goldStroke,
    onGold = BrandTokens.onGold,
    textPrimary = BrandTokens.textPrimary,
    textSecondary = BrandTokens.textSecondary,
    textMuted = BrandTokens.textMuted,
    isDark = true,
    surfaceMuted = Color(0xFF241C17),
    accent = Color(0xFFEF9F27),
    accentStrong = Color(0xFFEF9F27),
    accentContainer = Color(0xFF412402),
    onAccentContainer = Color(0xFFFAC775),
    accentText = Color(0xFFFAC775),
    success = Color(0xFF5DCAA5),
    successContainer = Color(0xFF04342C),
    onSuccessContainer = Color(0xFF9FE1CB),
    danger = Color(0xFFF09595),
    fasalooTeal = Color(0xFF0F6E56),
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
    goldText = Color(0xFF9A5A00),
    goldStroke = BrandTokens.goldStroke,
    onGold = BrandTokens.onGold,
    textPrimary = Color(0xFF2A211E),
    textSecondary = Color(0xFF5C524C),
    textMuted = Color(0xFF6F645A),
    isDark = false,
    surfaceMuted = Color(0xFFEFEAE2),
    accent = Color(0xFFBA7517),
    accentStrong = Color(0xFFEF9F27),
    accentContainer = Color(0xFFFAEEDA),
    onAccentContainer = Color(0xFF633806),
    accentText = Color(0xFF854F0B),
    success = Color(0xFF1D9E75),
    successContainer = Color(0xFFE1F5EE),
    onSuccessContainer = Color(0xFF085041),
    danger = Color(0xFFA32D2D),
    fasalooTeal = Color(0xFF0F6E56),
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
