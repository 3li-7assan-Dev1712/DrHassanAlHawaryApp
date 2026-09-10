package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.core.ui.R
import com.example.feature.share.domain.ShareCardContent
import javax.inject.Inject

/**
 * spec + content -> a static branded "quote card" image: the selected article
 * excerpt ([ShareCardContent.title]) dominates the top of the card, with a
 * small attribution line beneath it and, at the very bottom, a compact
 * logo + two-line (name/tagline) brand lockup - deliberately the smallest
 * element on the card, behind a thin divider, so it reads as a signature
 * rather than a second headline. Unlike the video card, this bitmap IS the
 * final shareable asset - no per-frame overlay, no encode step.
 */
class TextCardBitmapRenderer @Inject constructor() {

    fun render(context: Context, content: ShareCardContent, spec: TextCardSpec): Bitmap {
        val width = spec.referenceWidthPx.toInt()
        val height = spec.referenceHeightPx.toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawCardBackground(context, canvas, content.background, width, height)
        drawCardScrim(canvas, spec.scrimStops, width, height)

        val boldTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_bold))
        val semiBoldTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_semi_bold))
        val regularTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_regular))

        drawQuoteMark(canvas, spec.quoteMark, width, height)
        drawCardText(canvas, content.title, spec.quote, width, height, boldTypeface)
        content.category?.let { drawCardText(canvas, it, spec.attribution, width, height, regularTypeface) }

        drawCardAccentRule(canvas, spec.divider, width, height)
        drawCardLogo(context, canvas, content.logoResId, spec.brandLogo, width, height)
        drawCardText(canvas, content.instituteName, spec.brandName, width, height, semiBoldTypeface)
        val brandTagline = context.getString(R.string.share_brand_line)
        drawCardText(canvas, brandTagline, spec.brandTagline, width, height, regularTypeface)

        return bitmap
    }

    /** A large, low-opacity decorative quotation mark above the excerpt - the
     * one purely visual flourish that tells the viewer "this is a quote" at a glance. */
    private fun drawQuoteMark(canvas: Canvas, rect: Rect01, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = CARD_ACCENT_COLOR
            alpha = 140
            textSize = rect.height * height
            typeface = Typeface.DEFAULT_BOLD
        }
        val baseline = rect.top * height + paint.textSize * 0.8f
        canvas.drawText(QUOTE_GLYPH, rect.left * width, baseline, paint)
    }

    companion object {
        private const val QUOTE_GLYPH = "”" // right double quotation mark
    }
}
