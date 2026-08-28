package com.example.feature.share.engine

/** A rectangle in 0..1 coordinates against the spec's reference canvas. */
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
 * actual width / referenceWidthPx.
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

/**
 * The single source of truth for the branded share card's design, expressed in
 * normalised 0..1 coordinates against a 1080x1920 (9:16) reference canvas.
 *
 * ShareCardBitmapRenderer (Canvas, burned into the video) and ShareCardPreview
 * (Compose, the live preview) both read this SAME spec - multiplying by their
 * own actual pixel size - so the exported video can never drift from what the
 * user previewed. Neither renderer may hardcode a pixel value of its own.
 */
data class ShareCardSpec(
    val referenceWidthPx: Float = 1080f,
    val referenceHeightPx: Float = 1920f,
    /** height fraction (0..1, top to bottom) -> ARGB, for a vertical gradient scrim. */
    val scrimStops: List<Pair<Float, Long>>,
    val logo: Rect01,
    val instituteLine: TextBlock01,
    val title: TextBlock01,
    val category: TextBlock01,
    val waveform: Rect01,
    val brandLine: TextBlock01,
    val accentRule: Rect01,
    /** Clear of status-bar-ish UI at the top of a share sheet preview. */
    val safeTop: Float = 0.09f,
    /** Clear of WhatsApp Status' bottom controls overlaying the video. */
    val safeBottom: Float = 0.07f,
) {
    companion object {
        fun default(): ShareCardSpec = ShareCardSpec(
            // A soft top scrim gives the logo/institute line a dark backing against
            // whatever is in the photo there; it fades out before the open photo area,
            // then the bottom stop (core-ui's backgroundDark, 0xFF0E1513, ~85% opaque)
            // takes over so the burned-in card matches the app's own dark theme.
            scrimStops = listOf(
                0f to 0x99000000,
                0.24f to 0x00000000,
                0.35f to 0x00000000,
                1f to 0xD90E1513,
            ),
            // 150x150px circle at the reference width, centred, clear of safeTop.
            logo = Rect01(0.4306f, 0.09f, 0.5694f, 0.1681f),
            instituteLine = TextBlock01(
                rect = Rect01(0.08f, 0.178f, 0.92f, 0.203f),
                fontSizePx = 34f,
                weight = ShareFontWeight.SEMI_BOLD,
            ),
            title = TextBlock01(
                rect = Rect01(0.06f, 0.49f, 0.94f, 0.661f),
                fontSizePx = 62f,
                minFontSizePx = 44f,
                maxLines = 3,
                weight = ShareFontWeight.BOLD,
            ),
            category = TextBlock01(
                rect = Rect01(0.06f, 0.667f, 0.94f, 0.693f),
                fontSizePx = 32f,
                alpha = 0.7f,
            ),
            // Half the height of the original edge-to-edge band, re-centred in the
            // same vertical slot, with side margins matching the brand/institute
            // lines (0.08-0.92) instead of running full-bleed - reads as a slim
            // accent strip rather than a dominant block of the card.
            waveform = Rect01(0.08f, 0.745f, 0.92f, 0.838f),
            accentRule = Rect01(0.08f, 0.891f, 0.92f, 0.8925f),
            brandLine = TextBlock01(
                rect = Rect01(0.08f, 0.901f, 0.92f, 0.927f),
                fontSizePx = 34f,
                weight = ShareFontWeight.SEMI_BOLD,
                alpha = 0.9f,
            ),
        )
    }
}
