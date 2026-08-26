package com.example.feature.share.engine

/**
 * Layout for the "quote card" - a static branded image sharing an excerpt of
 * article text, as opposed to [ShareCardSpec]'s animated audio-clip video.
 * Reuses [ShareCardContent][com.example.feature.share.domain.ShareCardContent]
 * as its content model (title -> the excerpt itself, category -> the "from
 * this article" attribution line, instituteName -> the small brand name next
 * to the logo) and the same normalised 0..1 coordinate convention against a
 * 1080x1920 reference canvas, for the same reason [ShareCardSpec] does:
 * [TextCardBitmapRenderer] (the exported image) and `TextCardPreview` (the
 * live Compose preview) both read this SAME spec, so neither can hardcode a
 * pixel value the other doesn't also derive from it.
 *
 * Top to bottom: a small quote mark, then the excerpt itself starting near
 * the very top of the card and given the large majority of the vertical
 * space (this is the whole point of the card). Everything else is a single
 * compact footer group anchored to the bottom - attribution line, a thin
 * divider, then the small logo + two-line (name/tagline) brand lockup - kept
 * deliberately small so it reads as a signature, not a second headline.
 */
data class TextCardSpec(
    val referenceWidthPx: Float = 1080f,
    val referenceHeightPx: Float = 1920f,
    val scrimStops: List<Pair<Float, Long>>,
    val quoteMark: Rect01,
    val quote: TextBlock01,
    val attribution: TextBlock01,
    val divider: Rect01,
    val brandLogo: Rect01,
    val brandName: TextBlock01,
    val brandTagline: TextBlock01,
) {
    companion object {
        fun default(): TextCardSpec = TextCardSpec(
            // Mostly clear - the gradient background already carries the brand
            // color story. Only darkens toward the bottom, so the small brand
            // lockup stays legible even if the background is ever a photo.
            scrimStops = listOf(
                0f to 0x00000000,
                0.6f to 0x00000000,
                0.85f to 0x40000000,
                1f to 0xE60E1513,
            ),
            quoteMark = Rect01(0.09f, 0.06f, 0.30f, 0.13f),
            // Starts right under the quote mark and extends almost all the way to
            // the bottom footer group - the excerpt owns the card. Sized/positioned
            // so a ~450-char Arabic excerpt (this feature's cap) fits with generous
            // margin at minFontSizePx before drawCardText's height-aware shrink ever
            // needs to fall back to truncation.
            quote = TextBlock01(
                rect = Rect01(0.09f, 0.145f, 0.91f, 0.854f),
                fontSizePx = 62f,
                minFontSizePx = 30f,
                maxLines = 18,
                weight = ShareFontWeight.SEMI_BOLD,
            ),
            // Directly above the divider/brand footer, not right after the excerpt -
            // its position never depends on how long the excerpt is.
            attribution = TextBlock01(
                rect = Rect01(0.12f, 0.872f, 0.88f, 0.905f),
                fontSizePx = 26f,
                alpha = 0.6f,
            ),
            divider = Rect01(0.44f, 0.9165f, 0.56f, 0.918f),
            // Diameter only depends on the rect's WIDTH (see drawCardLogo) - bottom
            // is just kept roughly square for clarity, it isn't otherwise used. Sits
            // beside (not below) the name/tagline column, vertically centered against it.
            brandLogo = Rect01(0.680f, 0.936f, 0.745f, 0.973f),
            // A wide column with a low minFontSizePx floor: the app name/tagline must
            // never be cut off, so it shrinks to fit rather than ellipsizing.
            brandName = TextBlock01(
                rect = Rect01(0.255f, 0.928f, 0.660f, 0.952f),
                fontSizePx = 24f,
                minFontSizePx = 14f,
                weight = ShareFontWeight.SEMI_BOLD,
            ),
            brandTagline = TextBlock01(
                rect = Rect01(0.255f, 0.955f, 0.660f, 0.980f),
                fontSizePx = 19f,
                minFontSizePx = 12f,
                alpha = 0.62f,
            ),
        )
    }
}
