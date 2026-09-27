package com.example.feature.share.engine

import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.example.core.ui.theme.BrandGold
import com.example.core.ui.theme.BrandGoldContainer
import com.example.core.ui.theme.BrandGoldLight
import com.example.core.ui.theme.BrandGoldSoft
import com.example.core.ui.theme.OnBrandGold
import com.example.core.ui.theme.ShareFrameBackground
import com.example.core.ui.theme.ShareFrameDivider
import com.example.core.ui.theme.ShareFrameMuted
import com.example.core.ui.theme.ShareFrameOnSurface
import com.example.core.ui.theme.ShareFrameSecondaryText
import com.example.core.ui.theme.ShareFrameSurface

/**
 * The single layout spec of the branded 9:16 share video frame, in pixels of a
 * 1080x1920 reference canvas. [ShareFramePainter] is the only code that reads
 * it, and both hosts draw through that painter - the exported MP4 (base bitmap
 * + per-frame [WaveformOverlay]) and the live Compose preview (which scales the
 * canvas by `width / WIDTH`) - so the two cannot drift apart.
 *
 * Vertical budget, top to bottom (see docs/plans/share-video-redesign.md §4):
 *  - header / chip / title / date all end above [CENTER_BAND_TOP], because
 *    WhatsApp/Telegram draw their play button over the centre ~25% of a video;
 *  - the sheikh's circular photo sits in the centre band (it's decorative);
 *  - waveform, times and footer end above [SAFE_BOTTOM], clear of the
 *    WhatsApp Status controls over the bottom 7%.
 */
object ShareFrameLayout {
    const val WIDTH = 1080f
    const val HEIGHT = 1920f

    const val SIDE_MARGIN = 73f
    const val CONTENT_LEFT = SIDE_MARGIN
    const val CONTENT_RIGHT = WIDTH - SIDE_MARGIN
    const val CONTENT_WIDTH = CONTENT_RIGHT - CONTENT_LEFT

    /** 0.375 x HEIGHT: nothing in the title group may extend past this. */
    const val CENTER_BAND_TOP = 720f
    /** 0.93 x HEIGHT. */
    const val SAFE_BOTTOM = 1786f

    // --- Header: logo on the right, name + subtitle to its left (RTL) ---
    const val LOGO_DIAMETER = 162f
    const val LOGO_TOP = 88f
    const val LOGO_RING_WIDTH = 2f
    val logo = RectF(CONTENT_RIGHT - LOGO_DIAMETER, LOGO_TOP, CONTENT_RIGHT, LOGO_TOP + LOGO_DIAMETER)
    const val HEADER_TEXT_RIGHT = CONTENT_RIGHT - LOGO_DIAMETER - 45f
    const val HEADER_NAME_SIZE = 42f
    const val HEADER_SUBTITLE_SIZE = 32f
    /** The name/subtitle pair is centred vertically on the logo. */
    const val HEADER_CENTER_Y = LOGO_TOP + LOGO_DIAMETER / 2f

    // --- Chip ---
    const val CHIP_TOP = 292f
    const val CHIP_HEIGHT = 90f
    const val CHIP_PADDING_H = 48f
    const val CHIP_TEXT_SIZE = 34f
    const val CHIP_BORDER_WIDTH = 2f
    const val CHIP_BORDER_ALPHA = 0.45f

    // --- Title (2 lines max, auto-shrinks) + date line ---
    const val TITLE_GAP_AFTER_CHIP = 24f
    /** Where the title starts when the chip is hidden. */
    const val TITLE_TOP_WITHOUT_CHIP = CHIP_TOP + 30f
    const val TITLE_MAX_SIZE = 80f
    const val TITLE_MIN_SIZE = 44f
    const val TITLE_MAX_LINES = 2
    const val DATE_GAP_AFTER_TITLE = 8f
    const val DATE_SIZE = 36f

    // --- Centre: the sheikh's circular photo with a gold ring ---
    const val PHOTO_DIAMETER = 300f
    const val PHOTO_RING_WIDTH = 5f
    val photo = RectF((WIDTH - PHOTO_DIAMETER) / 2f, 885f, (WIDTH + PHOTO_DIAMETER) / 2f, 885f + PHOTO_DIAMETER)

    // --- Waveform + times: the only animated region (drawn per frame by the overlay) ---
    val waveform = RectF(CONTENT_LEFT, 1370f, CONTENT_RIGHT, 1510f)
    const val BAR_COUNT = 34
    /** Bar width as a fraction of the bar pitch (the rest is gap). */
    const val BAR_WIDTH_FRACTION = 0.5f
    /** Silence still shows a short bar rather than a dot. */
    const val BAR_MIN_HEIGHT_FRACTION = 0.12f
    const val TIMES_TOP = 1528f
    /** Large enough to read on a phone: at 34px the Arabic-Indic zeros read as dots. */
    const val TIMES_SIZE = 52f
    /** Everything [ShareFramePainter.drawWaveform] may touch - the overlay bitmap covers exactly this. */
    val animatedRegion = RectF(0f, 1350f, WIDTH, 1614f)

    // --- Divider + footer ---
    const val DIVIDER_Y = 1654f
    const val DIVIDER_HEIGHT = 2f
    const val FOOTER_TOP = 1696f
    const val FOOTER_HEIGHT = 90f
    const val CTA_MIN_WIDTH = 267f
    const val CTA_PADDING_H = 48f
    const val CTA_TEXT_SIZE = 32f
    const val FOOTER_APP_NAME_SIZE = 34f

    // The video frame's footer sits higher than the quote images' so a text link fits
    // under the CTA pill (a pill inside a video can't be tapped) above SAFE_BOTTOM.
    const val VIDEO_DIVIDER_Y = 1624f
    const val VIDEO_FOOTER_TOP = 1648f
    const val LINK_GAP = 8f
    const val LINK_SIZE = 30f

    /** ARGB ints for Canvas, all from core-ui's theme tokens - no hex here. */
    object Colors {
        val background = ShareFrameBackground.toArgb()
        val surface = ShareFrameSurface.toArgb()
        val onSurface = ShareFrameOnSurface.toArgb()
        val secondaryText = ShareFrameSecondaryText.toArgb()
        val muted = ShareFrameMuted.toArgb()
        val divider = ShareFrameDivider.toArgb()
        val gold = BrandGold.toArgb()
        val goldLight = BrandGoldLight.toArgb()
        val goldSoft = BrandGoldSoft.toArgb()
        val goldContainer = BrandGoldContainer.toArgb()
        val onGold = OnBrandGold.toArgb()
    }
}
