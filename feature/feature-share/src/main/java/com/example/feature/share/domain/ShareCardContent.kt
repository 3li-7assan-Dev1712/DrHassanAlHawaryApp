package com.example.feature.share.domain

import androidx.annotation.DrawableRes

/**
 * Generic content for a branded share card. Deliberately not tied to any
 * content-type model (Audio, Video, ...) so feature-video / feature-image
 * can reuse this same engine later.
 */
data class ShareCardContent(
    val title: String,
    val category: String?,
    val instituteName: String,
    val background: ShareBackgroundSource,
    @DrawableRes val logoResId: Int,
)

sealed interface ShareBackgroundSource {
    data class FromDrawableRes(@DrawableRes val resId: Int) : ShareBackgroundSource
    data class FromImageUri(val uri: String) : ShareBackgroundSource
    data object Gradient : ShareBackgroundSource
}
