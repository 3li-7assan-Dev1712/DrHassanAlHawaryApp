package com.example.feature.share.domain

import androidx.annotation.DrawableRes
import com.example.domain.text.GregorianDate
import com.example.domain.text.HijriDate

/**
 * Generic content for a branded share card. Deliberately not tied to any
 * content-type model (Audio, Video, ...) so feature-video / feature-image
 * can reuse this same engine later.
 *
 * The video card draws [title], [kindLabel] and [hijriDate]/[gregorianDate]
 * as separate blocks - never one concatenated string, which is what
 * broke the bidi layout of the server's combined "title - date" strings (see
 * [ShareTitleParser]). The quote card only uses [title]/[category].
 */
data class ShareCardContent(
    val title: String,
    /** Quote card: the "from this article" attribution line. Unused by the video card. */
    val category: String?,
    val instituteName: String,
    val background: ShareBackgroundSource,
    @DrawableRes val logoResId: Int,
    /** Video card chip, e.g. "خطبة الجمعة". Null hides the chip. */
    val kindLabel: String? = null,
    val hijriDate: HijriDate? = null,
    val gregorianDate: GregorianDate? = null,
)

sealed interface ShareBackgroundSource {
    data class FromDrawableRes(@DrawableRes val resId: Int) : ShareBackgroundSource
    data class FromImageUri(val uri: String) : ShareBackgroundSource
    data object Gradient : ShareBackgroundSource
}
