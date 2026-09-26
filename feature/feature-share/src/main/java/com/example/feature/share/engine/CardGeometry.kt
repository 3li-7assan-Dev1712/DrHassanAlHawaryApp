package com.example.feature.share.engine

/** A rectangle in 0..1 coordinates against a spec's reference canvas. */
data class Rect01(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

enum class ShareFontWeight { REGULAR, MEDIUM, SEMI_BOLD, BOLD }

/**
 * A text block positioned in 0..1 coordinates, with a font size expressed in
 * px *at the reference canvas width* - both renderers scale it by their own
 * actual width / referenceWidthPx. Used by [TextCardSpec] (the quote image).
 */
data class TextBlock01(
    val rect: Rect01,
    val fontSizePx: Float,
    val minFontSizePx: Float = fontSizePx,
    val maxLines: Int = 1,
    val weight: ShareFontWeight = ShareFontWeight.REGULAR,
    val colorArgb: Long = 0xFFFFFFFF,
    val alpha: Float = 1f,
)
