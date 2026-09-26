package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import com.example.core.ui.R
import com.example.feature.share.engine.ShareFrameLayout.Colors
import kotlin.math.ceil
import kotlin.math.max

/**
 * The branded pieces every share image has in common - background, header
 * (gold-ringed logo + name + subtitle), gold chip, footer (divider, app name,
 * CTA pill) - plus the RTL text helpers they're built from. Used by
 * [ShareFramePainter] (the video frame) and [QuoteCardPainter] (the article
 * quote images), so both designs stay identical wherever they overlap.
 *
 * Coordinates are [ShareFrameLayout]'s 1080x1920 reference pixels. All text goes
 * through [StaticLayout] with [TextDirectionHeuristics.RTL] - never
 * `Canvas.drawText`, which breaks Arabic shaping and bidi. Not thread-safe:
 * give each thread its own instance.
 */
class BrandFramePainter(private val context: Context) {

    val bold = font(R.font.cairo_bold)
    val semiBold = font(R.font.cairo_semi_bold)
    val medium = font(R.font.cairo_medium)
    val regular = font(R.font.cairo_regular)

    private val nameText = context.getString(R.string.share_frame_name)
    private val appNameText = context.getString(R.string.share_frame_app_name)
    private val ctaText = context.getString(R.string.share_cta_label)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val rect = RectF()

    fun drawBackground(canvas: Canvas) {
        canvas.drawColor(Colors.background)
    }

    /** Logo on the right, "الشيخ د. حسن الهواري" + [subtitle] to its left, centred on the logo. */
    fun drawHeader(canvas: Canvas, logoResId: Int, subtitle: String) {
        drawLogo(canvas, logoResId)

        val width = (ShareFrameLayout.HEADER_TEXT_RIGHT - ShareFrameLayout.CONTENT_LEFT).toInt()
        val name = fitLayout(nameText, medium, ShareFrameLayout.HEADER_NAME_SIZE, 30f, Colors.onSurface, width, Layout.Alignment.ALIGN_NORMAL, 1)
        val sub = fitLayout(subtitle, regular, ShareFrameLayout.HEADER_SUBTITLE_SIZE, 24f, Colors.secondaryText, width, Layout.Alignment.ALIGN_NORMAL, 1)
        val top = ShareFrameLayout.HEADER_CENTER_Y - (name.height + sub.height) / 2f
        draw(canvas, name, ShareFrameLayout.CONTENT_LEFT, top)
        draw(canvas, sub, ShareFrameLayout.CONTENT_LEFT, top + name.height)
    }

    /** A centred gold pill with [label], its top at [top]. Returns its bottom edge. */
    fun drawGoldChip(canvas: Canvas, label: String, top: Float): Float {
        val maxTextWidth = ShareFrameLayout.CONTENT_WIDTH - 2 * ShareFrameLayout.CHIP_PADDING_H
        val paint = textPaint(semiBold, ShareFrameLayout.CHIP_TEXT_SIZE, Colors.goldLight)
        val textWidth = ceil(Layout.getDesiredWidth(label, paint)).coerceAtMost(maxTextWidth)
        val text = layout(label, paint, textWidth.toInt(), Layout.Alignment.ALIGN_CENTER, maxLines = 1)

        val chipWidth = textWidth + 2 * ShareFrameLayout.CHIP_PADDING_H
        val left = (ShareFrameLayout.WIDTH - chipWidth) / 2f
        val radius = ShareFrameLayout.CHIP_HEIGHT / 2f
        rect.set(left, top, left + chipWidth, top + ShareFrameLayout.CHIP_HEIGHT)
        fillPaint.color = Colors.goldContainer
        canvas.drawRoundRect(rect, radius, radius, fillPaint)

        val inset = ShareFrameLayout.CHIP_BORDER_WIDTH / 2f
        rect.inset(inset, inset)
        strokePaint.strokeWidth = ShareFrameLayout.CHIP_BORDER_WIDTH
        strokePaint.color = Colors.gold
        strokePaint.alpha = (255 * ShareFrameLayout.CHIP_BORDER_ALPHA).toInt()
        canvas.drawRoundRect(rect, radius - inset, radius - inset, strokePaint)
        strokePaint.alpha = 255

        draw(canvas, text, left + ShareFrameLayout.CHIP_PADDING_H, top + (ShareFrameLayout.CHIP_HEIGHT - text.height) / 2f)
        return top + ShareFrameLayout.CHIP_HEIGHT
    }

    /** Divider, then the gold CTA pill on the left and the app name on the right. */
    fun drawFooter(canvas: Canvas) {
        fillPaint.color = Colors.divider
        canvas.drawRect(
            ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.DIVIDER_Y,
            ShareFrameLayout.CONTENT_RIGHT, ShareFrameLayout.DIVIDER_Y + ShareFrameLayout.DIVIDER_HEIGHT,
            fillPaint,
        )

        val ctaPaint = textPaint(semiBold, ShareFrameLayout.CTA_TEXT_SIZE, Colors.onGold)
        val ctaTextWidth = ceil(Layout.getDesiredWidth(ctaText, ctaPaint))
        val ctaWidth = max(ShareFrameLayout.CTA_MIN_WIDTH, ctaTextWidth + 2 * ShareFrameLayout.CTA_PADDING_H)
            .coerceAtMost(ShareFrameLayout.CONTENT_WIDTH / 2f)
        val radius = ShareFrameLayout.FOOTER_HEIGHT / 2f
        rect.set(ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.FOOTER_TOP, ShareFrameLayout.CONTENT_LEFT + ctaWidth, ShareFrameLayout.FOOTER_TOP + ShareFrameLayout.FOOTER_HEIGHT)
        fillPaint.color = Colors.goldSoft
        canvas.drawRoundRect(rect, radius, radius, fillPaint)
        val cta = layout(ctaText, ctaPaint, ctaWidth.toInt(), Layout.Alignment.ALIGN_CENTER, maxLines = 1)
        draw(canvas, cta, ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.FOOTER_TOP + (ShareFrameLayout.FOOTER_HEIGHT - cta.height) / 2f)

        val nameLeft = ShareFrameLayout.CONTENT_LEFT + ctaWidth + 24f
        val name = fitLayout(
            appNameText, regular, ShareFrameLayout.FOOTER_APP_NAME_SIZE, 24f, Colors.secondaryText,
            (ShareFrameLayout.CONTENT_RIGHT - nameLeft).toInt(), Layout.Alignment.ALIGN_NORMAL, 1,
        )
        draw(canvas, name, nameLeft, ShareFrameLayout.FOOTER_TOP + (ShareFrameLayout.FOOTER_HEIGHT - name.height) / 2f)
    }

    /**
     * The largest size in [minSize]..[maxSize] (binary search, 1 px precision) at
     * which [text] fits [maxLines] and [maxHeight]; if nothing fits even at
     * [minSize], that size ellipsized at [maxLines].
     */
    fun fitLayout(
        text: CharSequence,
        typeface: Typeface,
        maxSize: Float,
        minSize: Float,
        color: Int,
        width: Int,
        alignment: Layout.Alignment,
        maxLines: Int,
        maxHeight: Float = Float.MAX_VALUE,
    ): StaticLayout {
        fun build(size: Int, limit: Int = Int.MAX_VALUE) = layout(text, textPaint(typeface, size.toFloat(), color), width, alignment, limit)
        fun fits(l: StaticLayout) = l.lineCount <= maxLines && l.height <= maxHeight

        var low = minSize.toInt()
        var high = maxSize.toInt()
        build(high).let { if (fits(it)) return it }
        var best: StaticLayout? = null
        while (low <= high) {
            val mid = (low + high) / 2
            val candidate = build(mid)
            if (fits(candidate)) {
                best = candidate
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best ?: build(minSize.toInt(), maxLines)
    }

    fun layout(
        text: CharSequence,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment,
        maxLines: Int = Int.MAX_VALUE,
        lineSpacingExtra: Float = 0f,
    ): StaticLayout {
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setLineSpacing(lineSpacingExtra, 1f)
        if (maxLines != Int.MAX_VALUE) {
            builder.setMaxLines(maxLines).setEllipsize(TextUtils.TruncateAt.END)
        }
        return builder.build()
    }

    fun textPaint(typeface: Typeface, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = size
        this.color = color
    }

    fun draw(canvas: Canvas, layout: StaticLayout, left: Float, top: Float) {
        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawLogo(canvas: Canvas, logoResId: Int) {
        val bounds = ShareFrameLayout.logo
        val diameter = bounds.width()
        val source = decodeSampledBitmap(logoResId, diameter.toInt())
        val square = centerSquare(source.width, source.height)
        val cropped = Bitmap.createBitmap(source, square.left, square.top, square.width(), square.height())

        val shader = BitmapShader(cropped, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val scale = diameter / cropped.width
        shader.setLocalMatrix(Matrix().apply {
            setScale(scale, scale)
            postTranslate(bounds.left, bounds.top)
        })
        fillPaint.shader = shader
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), diameter / 2f, fillPaint)
        fillPaint.shader = null

        strokePaint.strokeWidth = ShareFrameLayout.LOGO_RING_WIDTH
        strokePaint.color = Colors.gold
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), (diameter - strokePaint.strokeWidth) / 2f, strokePaint)

        if (cropped !== source) cropped.recycle()
        source.recycle()
    }

    private fun decodeSampledBitmap(resId: Int, targetWidth: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= targetWidth && sampleSize < 16) sampleSize *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return requireNotNull(BitmapFactory.decodeResource(context.resources, resId, opts))
    }

    private fun centerSquare(width: Int, height: Int): Rect {
        val side = minOf(width, height)
        val left = (width - side) / 2
        val top = (height - side) / 2
        return Rect(left, top, left + side, top + side)
    }

    private fun font(resId: Int): Typeface = requireNotNull(ResourcesCompat.getFont(context, resId))
}
