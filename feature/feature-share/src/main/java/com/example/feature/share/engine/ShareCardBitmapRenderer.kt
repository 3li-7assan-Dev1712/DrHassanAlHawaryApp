package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.example.core.ui.R
import com.example.feature.share.domain.ShareCardContent
import javax.inject.Inject

/**
 * spec + content -> a static 1080x1920 bitmap: everything in the design
 * EXCEPT the animated waveform bars, which [WaveformOverlay] draws per-frame
 * on top of this at export time.
 *
 * Reads the exact same [ShareCardSpec] as the Compose preview - see that
 * class's doc comment for why the two must never diverge. Drawing itself
 * (background/scrim/logo/text/accent rule) is shared with [TextCardBitmapRenderer]
 * via CardDrawing.kt - this class only owns the layout, i.e. which spec rect
 * gets which piece of content.
 */
class ShareCardBitmapRenderer @Inject constructor() {

    fun render(context: Context, content: ShareCardContent, spec: ShareCardSpec): Bitmap {
        val width = spec.referenceWidthPx.toInt()
        val height = spec.referenceHeightPx.toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawCardBackground(context, canvas, content.background, width, height)
        drawCardScrim(canvas, spec.scrimStops, width, height)
        drawCardLogo(context, canvas, content.logoResId, spec.logo, width, height)

        val boldTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_bold))
        val semiBoldTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_semi_bold))
        val regularTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_regular))

        drawCardText(canvas, content.instituteName, spec.instituteLine, width, height, semiBoldTypeface)
        drawCardText(canvas, content.title, spec.title, width, height, boldTypeface)
        content.category?.let { drawCardText(canvas, it, spec.category, width, height, regularTypeface) }

        drawCardAccentRule(canvas, spec.accentRule, width, height)

        val brandLine = context.getString(R.string.share_brand_line)
        drawCardText(canvas, brandLine, spec.brandLine, width, height, semiBoldTypeface)

        return bitmap
    }
}
