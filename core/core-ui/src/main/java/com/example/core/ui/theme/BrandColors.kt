package com.example.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The dark brand palette: the home screen and the share images (video frame,
 * quote images). One set of values for all of them - add a token here rather
 * than re-typing a hex value in a feature module.
 *
 * Dark-only by design: the home screen uses it regardless of the app's
 * light/dark setting (docs/plans/home-redesign.md, question 1).
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
