package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
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
import com.example.feature.share.domain.ArabicNumerals
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.engine.ShareFrameLayout.Colors
import kotlin.math.ceil
import kotlin.math.max

/**
 * Draws the branded share video frame described by [ShareFrameLayout], in that
 * spec's 1080x1920 reference coordinates. The ONLY implementation of the design:
 *
 *  - export: [ShareCardBitmapRenderer] calls [drawStatic] into the base frame and
 *    [WaveformOverlay] calls [drawWaveform] into its per-frame strip bitmap;
 *  - preview: the Compose card shows that same rendered base bitmap and calls
 *    [drawWaveform] on its own canvas, scaled by `width / ShareFrameLayout.WIDTH`.
 *
 * All text - Arabic or digits - goes through [StaticLayout] with
 * [TextDirectionHeuristics.RTL]; never `Canvas.drawText`, which breaks Arabic
 * shaping and bidi. Not thread-safe: give each host its own instance.
 */
class ShareFramePainter(private val context: Context) {

    private val bold = font(R.font.cairo_bold)
    private val semiBold = font(R.font.cairo_semi_bold)
    private val medium = font(R.font.cairo_medium)
    private val regular = font(R.font.cairo_regular)

    private val nameText = context.getString(R.string.share_frame_name)
    private val subtitleText = context.getString(R.string.share_frame_subtitle)
    private val appNameText = context.getString(R.string.share_frame_app_name)
    private val ctaText = context.getString(R.string.share_cta_label)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val rect = RectF()

    // drawWaveform runs once per video frame; the time labels only change once a second.
    private var elapsedCache: Pair<String, StaticLayout>? = null
    private var totalCache: Pair<String, StaticLayout>? = null

    fun drawStatic(canvas: Canvas, content: ShareCardContent) {
        canvas.drawColor(Colors.background)
        drawHeader(canvas, content.logoResId)
        val titleTop = drawChip(canvas, content.kindLabel)
        drawTitleAndDate(canvas, content, titleTop)
        content.quote?.trim()?.takeIf { it.isNotEmpty() }?.let { drawQuote(canvas, it) }
        drawFooter(canvas)
    }

    /**
     * The animated part: [bars] in time order (0..1 each, see [WaveformBars]),
     * laid out right-to-left so the played portion ([progress] 0..1) fills in
     * from the right, gold over gray; elapsed time on the right, total on the left.
     * Draws only inside [ShareFrameLayout.animatedRegion].
     */
    fun drawWaveform(canvas: Canvas, bars: FloatArray, progress: Float, elapsedMs: Long, totalMs: Long) {
        val strip = ShareFrameLayout.waveform
        val count = ShareFrameLayout.BAR_COUNT
        val pitch = strip.width() / count
        val barWidth = pitch * ShareFrameLayout.BAR_WIDTH_FRACTION
        val centerY = strip.centerY()
        val clampedProgress = progress.coerceIn(0f, 1f)

        fillPaint.shader = null
        for (i in 0 until count) {
            val amplitude = bars.getOrElse(i) { 0f }.coerceIn(0f, 1f)
            val height = strip.height() * max(amplitude, ShareFrameLayout.BAR_MIN_HEIGHT_FRACTION)
            val centerX = strip.right - (i + 0.5f) * pitch
            val played = (i + 0.5f) / count <= clampedProgress
            fillPaint.color = if (played) Colors.gold else Colors.muted
            rect.set(centerX - barWidth / 2f, centerY - height / 2f, centerX + barWidth / 2f, centerY + height / 2f)
            canvas.drawRoundRect(rect, barWidth / 2f, barWidth / 2f, fillPaint)
        }

        val halfWidth = ShareFrameLayout.CONTENT_WIDTH / 2f
        val elapsed = ArabicNumerals.formatDuration(elapsedMs)
        val total = ArabicNumerals.formatDuration(totalMs)
        val elapsedLayout = elapsedCache?.takeIf { it.first == elapsed }?.second
            ?: timeLayout(elapsed, halfWidth, Layout.Alignment.ALIGN_NORMAL).also { elapsedCache = elapsed to it }
        val totalLayout = totalCache?.takeIf { it.first == total }?.second
            ?: timeLayout(total, halfWidth, Layout.Alignment.ALIGN_OPPOSITE).also { totalCache = total to it }
        draw(canvas, elapsedLayout, ShareFrameLayout.CONTENT_LEFT + halfWidth, ShareFrameLayout.TIMES_TOP)
        draw(canvas, totalLayout, ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.TIMES_TOP)
    }

    private fun drawHeader(canvas: Canvas, logoResId: Int) {
        drawLogo(canvas, logoResId)

        val width = (ShareFrameLayout.HEADER_TEXT_RIGHT - ShareFrameLayout.CONTENT_LEFT).toInt()
        val name = fitLayout(nameText, medium, ShareFrameLayout.HEADER_NAME_SIZE, 30f, Colors.onSurface, width, Layout.Alignment.ALIGN_NORMAL, 1)
        val subtitle = fitLayout(subtitleText, regular, ShareFrameLayout.HEADER_SUBTITLE_SIZE, 24f, Colors.secondaryText, width, Layout.Alignment.ALIGN_NORMAL, 1)
        val top = ShareFrameLayout.HEADER_CENTER_Y - (name.height + subtitle.height) / 2f
        draw(canvas, name, ShareFrameLayout.CONTENT_LEFT, top)
        draw(canvas, subtitle, ShareFrameLayout.CONTENT_LEFT, top + name.height)
    }

    private fun drawLogo(canvas: Canvas, logoResId: Int) {
        val bounds = ShareFrameLayout.logo
        val diameter = bounds.width()
        val source = decodeSampledBitmap(context, logoResId, diameter.toInt())
        val square = centerCropSrcRect(source.width, source.height, 1f)
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

    /** Returns where the title starts - directly under the chip, or higher when there's no chip. */
    private fun drawChip(canvas: Canvas, label: String?): Float {
        if (label.isNullOrBlank()) return ShareFrameLayout.TITLE_TOP_WITHOUT_CHIP

        val maxTextWidth = ShareFrameLayout.CONTENT_WIDTH - 2 * ShareFrameLayout.CHIP_PADDING_H
        val paint = textPaint(semiBold, ShareFrameLayout.CHIP_TEXT_SIZE, Colors.goldLight)
        val textWidth = ceil(Layout.getDesiredWidth(label, paint)).coerceAtMost(maxTextWidth)
        val text = layout(label, paint, textWidth.toInt(), Layout.Alignment.ALIGN_CENTER, maxLines = 1)

        val chipWidth = textWidth + 2 * ShareFrameLayout.CHIP_PADDING_H
        val left = (ShareFrameLayout.WIDTH - chipWidth) / 2f
        val radius = ShareFrameLayout.CHIP_HEIGHT / 2f
        rect.set(left, ShareFrameLayout.CHIP_TOP, left + chipWidth, ShareFrameLayout.CHIP_TOP + ShareFrameLayout.CHIP_HEIGHT)
        fillPaint.color = Colors.goldContainer
        canvas.drawRoundRect(rect, radius, radius, fillPaint)

        val inset = ShareFrameLayout.CHIP_BORDER_WIDTH / 2f
        rect.inset(inset, inset)
        strokePaint.strokeWidth = ShareFrameLayout.CHIP_BORDER_WIDTH
        strokePaint.color = Colors.gold
        strokePaint.alpha = (255 * ShareFrameLayout.CHIP_BORDER_ALPHA).toInt()
        canvas.drawRoundRect(rect, radius - inset, radius - inset, strokePaint)
        strokePaint.alpha = 255

        draw(canvas, text, left + ShareFrameLayout.CHIP_PADDING_H, ShareFrameLayout.CHIP_TOP + (ShareFrameLayout.CHIP_HEIGHT - text.height) / 2f)
        return ShareFrameLayout.CHIP_TOP + ShareFrameLayout.CHIP_HEIGHT + ShareFrameLayout.TITLE_GAP_AFTER_CHIP
    }

    private fun drawTitleAndDate(canvas: Canvas, content: ShareCardContent, titleTop: Float) {
        val width = ShareFrameLayout.CONTENT_WIDTH.toInt()
        val dateLine = ArabicNumerals.formatDateLine(content.hijriDate, content.gregorianDate)
        val date = dateLine?.let {
            fitLayout(it, medium, ShareFrameLayout.DATE_SIZE, 26f, Colors.goldLight, width, Layout.Alignment.ALIGN_CENTER, 1)
        }
        val dateBlock = date?.let { ShareFrameLayout.DATE_GAP_AFTER_TITLE + it.height } ?: 0f

        // The title's height budget is whatever keeps title + date above the centre band.
        val title = fitLayout(
            text = ArabicNumerals.digits(content.title),
            typeface = bold,
            maxSize = ShareFrameLayout.TITLE_MAX_SIZE,
            minSize = ShareFrameLayout.TITLE_MIN_SIZE,
            color = Colors.onSurface,
            width = width,
            alignment = Layout.Alignment.ALIGN_CENTER,
            maxLines = ShareFrameLayout.TITLE_MAX_LINES,
            maxHeight = ShareFrameLayout.CENTER_BAND_TOP - titleTop - dateBlock,
        )
        draw(canvas, title, ShareFrameLayout.CONTENT_LEFT, titleTop)
        date?.let {
            draw(canvas, it, ShareFrameLayout.CONTENT_LEFT, titleTop + title.height + ShareFrameLayout.DATE_GAP_AFTER_TITLE)
        }
    }

    private fun drawQuote(canvas: Canvas, quote: String) {
        val textWidth = (ShareFrameLayout.CONTENT_WIDTH - 2 * ShareFrameLayout.QUOTE_PADDING_H).toInt()
        val text = fitLayout(
            text = "«${ArabicNumerals.digits(quote.take(ShareFrameLayout.QUOTE_MAX_CHARS))}»",
            typeface = regular,
            maxSize = ShareFrameLayout.QUOTE_TEXT_SIZE,
            minSize = ShareFrameLayout.QUOTE_MIN_TEXT_SIZE,
            color = Colors.onSurface,
            width = textWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            maxLines = ShareFrameLayout.QUOTE_MAX_LINES,
        )
        val cardHeight = text.height + 2 * ShareFrameLayout.QUOTE_PADDING_V
        rect.set(ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.QUOTE_TOP, ShareFrameLayout.CONTENT_RIGHT, ShareFrameLayout.QUOTE_TOP + cardHeight)
        fillPaint.color = Colors.surface
        canvas.drawRoundRect(rect, ShareFrameLayout.QUOTE_RADIUS, ShareFrameLayout.QUOTE_RADIUS, fillPaint)
        draw(canvas, text, ShareFrameLayout.CONTENT_LEFT + ShareFrameLayout.QUOTE_PADDING_H, ShareFrameLayout.QUOTE_TOP + ShareFrameLayout.QUOTE_PADDING_V)
    }

    private fun drawFooter(canvas: Canvas) {
        fillPaint.color = Colors.divider
        canvas.drawRect(
            ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.DIVIDER_Y,
            ShareFrameLayout.CONTENT_RIGHT, ShareFrameLayout.DIVIDER_Y + ShareFrameLayout.DIVIDER_HEIGHT,
            fillPaint,
        )

        // CTA pill on the left (the end side in RTL).
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

        // App name on the right (the start side in RTL).
        val nameLeft = ShareFrameLayout.CONTENT_LEFT + ctaWidth + 24f
        val name = fitLayout(
            appNameText, regular, ShareFrameLayout.FOOTER_APP_NAME_SIZE, 24f, Colors.secondaryText,
            (ShareFrameLayout.CONTENT_RIGHT - nameLeft).toInt(), Layout.Alignment.ALIGN_NORMAL, 1,
        )
        draw(canvas, name, nameLeft, ShareFrameLayout.FOOTER_TOP + (ShareFrameLayout.FOOTER_HEIGHT - name.height) / 2f)
    }

    private fun timeLayout(text: String, width: Float, alignment: Layout.Alignment): StaticLayout =
        layout(text, textPaint(regular, ShareFrameLayout.TIMES_SIZE, Colors.secondaryText), width.toInt(), alignment, maxLines = 1)

    /** Shrinks from [maxSize] toward [minSize] until the text fits [maxLines] and
     * [maxHeight]; if it still doesn't fit at [minSize], ellipsizes at [maxLines]. */
    private fun fitLayout(
        text: String,
        typeface: Typeface,
        maxSize: Float,
        minSize: Float,
        color: Int,
        width: Int,
        alignment: Layout.Alignment,
        maxLines: Int,
        maxHeight: Float = Float.MAX_VALUE,
    ): StaticLayout {
        var size = maxSize
        var result = layout(text, textPaint(typeface, size, color), width, alignment)
        while ((result.lineCount > maxLines || result.height > maxHeight) && size > minSize) {
            size = (size - 2f).coerceAtLeast(minSize)
            result = layout(text, textPaint(typeface, size, color), width, alignment)
        }
        return if (result.lineCount > maxLines) {
            layout(text, textPaint(typeface, size, color), width, alignment, maxLines)
        } else {
            result
        }
    }

    private fun layout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment,
        maxLines: Int = Int.MAX_VALUE,
    ): StaticLayout {
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
        if (maxLines != Int.MAX_VALUE) {
            builder.setMaxLines(maxLines).setEllipsize(TextUtils.TruncateAt.END)
        }
        return builder.build()
    }

    private fun textPaint(typeface: Typeface, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = size
        this.color = color
    }

    private fun draw(canvas: Canvas, layout: StaticLayout, left: Float, top: Float) {
        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun font(resId: Int): Typeface = requireNotNull(ResourcesCompat.getFont(context, resId))
}
