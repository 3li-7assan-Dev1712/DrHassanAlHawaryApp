package com.example.feature.share.engine

/**
 * Layout of the article quote images, in the same 1080x1920 reference pixels as
 * [ShareFrameLayout] - whose header, footer, margins and colours it reuses, so a
 * quote image and a video frame are recognisably the same brand.
 *
 * Top to bottom: header ("من مقالات الشيخ") / body area, content vertically
 * centred in it: a large gold quote mark, the excerpt (right-aligned, readable
 * size - never shrunk to cram text in; long excerpts become more pages) and the
 * "١ / ٣" page number / the "من مقال" source card / footer.
 */
object QuoteCardLayout {
    const val BODY_TOP = 300f

    // Source card, anchored just above the shared footer divider.
    const val SOURCE_CARD_HEIGHT = 200f
    const val SOURCE_CARD_BOTTOM = ShareFrameLayout.DIVIDER_Y - 40f
    const val SOURCE_CARD_TOP = SOURCE_CARD_BOTTOM - SOURCE_CARD_HEIGHT
    const val SOURCE_CARD_RADIUS = 36f
    const val SOURCE_CARD_PADDING_H = 40f
    const val SOURCE_BAR_WIDTH = 6f
    const val SOURCE_BAR_INSET_V = 36f
    const val SOURCE_BAR_GAP = 24f
    const val SOURCE_LABEL_SIZE = 30f
    const val SOURCE_TITLE_SIZE = 36f
    const val SOURCE_TITLE_MIN_SIZE = 26f
    const val SOURCE_TITLE_MAX_LINES = 2

    const val BODY_BOTTOM = SOURCE_CARD_TOP - 48f

    const val QUOTE_MARK_SIZE = 115f
    const val QUOTE_MARK_GAP = 36f

    const val BODY_TEXT_SIZE = 46f
    /** Extra px between lines on top of the font's own line height - airy, easy to read. */
    const val BODY_LINE_SPACING_EXTRA = 22f
    const val BODY_WIDTH = ShareFrameLayout.CONTENT_WIDTH

    const val PAGE_NUMBER_GAP = 28f
    const val PAGE_NUMBER_SIZE = 30f
}
