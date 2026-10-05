package com.example.feature.share.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import com.example.core.ui.R
import com.example.domain.text.ArabicNumerals
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.engine.ShareFrameLayout.Colors
import kotlin.math.max

/**
 * Draws the branded share VIDEO frame described by [ShareFrameLayout], in that
 * spec's 1080x1920 reference coordinates. The ONLY implementation of the design:
 *
 *  - export: [ShareCardBitmapRenderer] calls [drawStatic] into the base frame and
 *    [WaveformOverlay] calls [drawWaveform] into its per-frame strip bitmap;
 *  - preview: the Compose card shows that same rendered base bitmap and calls
 *    [drawWaveform] on its own canvas, scaled by `width / ShareFrameLayout.WIDTH`.
 *
 * Header, chip, footer and the RTL text helpers come from [BrandFramePainter],
 * shared with the article quote images. Not thread-safe: give each host its own
 * instance.
 */
class ShareFramePainter(context: Context) {

    private val brand = BrandFramePainter(context)
    private val subtitleText = context.getString(R.string.share_frame_subtitle)
    private val speakerName = context.getString(R.string.share_video_speaker_name)
    private val linkText = context.getString(R.string.share_video_link)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    // drawWaveform runs once per video frame; the time labels only change once a second.
    private var elapsedCache: Pair<String, StaticLayout>? = null
    private var totalCache: Pair<String, StaticLayout>? = null

    fun drawStatic(canvas: Canvas, content: ShareCardContent) {
        brand.drawBackground(canvas)
        brand.drawHeader(canvas, content.logoResId, subtitleText, speakerName)
        val titleTop = content.kindLabel?.takeIf { it.isNotBlank() }
            ?.let { brand.drawGoldChip(canvas, it, ShareFrameLayout.CHIP_TOP) + ShareFrameLayout.TITLE_GAP_AFTER_CHIP }
            ?: ShareFrameLayout.TITLE_TOP_WITHOUT_CHIP
        drawTitleAndDate(canvas, content, titleTop)
        brand.drawCirclePhoto(canvas, R.drawable.dr_hassan_photo, ShareFrameLayout.photo, ShareFrameLayout.PHOTO_RING_WIDTH)
        brand.drawFooter(canvas, ShareFrameLayout.VIDEO_DIVIDER_Y, ShareFrameLayout.VIDEO_FOOTER_TOP, linkText)
    }

    /**
     * The animated part: [bars] in time order (0..1 each, see [WaveformBars]),
     * laid out LEFT TO RIGHT like every media timeline in the app (even in RTL): the
     * played portion ([progress] 0..1) fills in from the left, gold over gray; elapsed
     * time on the left, total on the right.
     * Draws only inside [ShareFrameLayout.animatedRegion].
     */
    fun drawWaveform(canvas: Canvas, bars: FloatArray, progress: Float, elapsedMs: Long, totalMs: Long) {
        val strip = ShareFrameLayout.waveform
        val count = ShareFrameLayout.BAR_COUNT
        val pitch = strip.width() / count
        val barWidth = pitch * ShareFrameLayout.BAR_WIDTH_FRACTION
        val centerY = strip.centerY()
        val clampedProgress = progress.coerceIn(0f, 1f)

        for (i in 0 until count) {
            val amplitude = bars.getOrElse(i) { 0f }.coerceIn(0f, 1f)
            val height = strip.height() * max(amplitude, ShareFrameLayout.BAR_MIN_HEIGHT_FRACTION)
            val centerX = strip.left + (i + 0.5f) * pitch
            val played = (i + 0.5f) / count <= clampedProgress
            fillPaint.color = if (played) Colors.gold else Colors.muted
            rect.set(centerX - barWidth / 2f, centerY - height / 2f, centerX + barWidth / 2f, centerY + height / 2f)
            canvas.drawRoundRect(rect, barWidth / 2f, barWidth / 2f, fillPaint)
        }

        val halfWidth = ShareFrameLayout.CONTENT_WIDTH / 2f
        val elapsed = ArabicNumerals.formatMediaTime(elapsedMs)
        val total = ArabicNumerals.formatMediaTime(totalMs)
        val elapsedLayout = elapsedCache?.takeIf { it.first == elapsed }?.second
            ?: timeLayout(elapsed, halfWidth, Layout.Alignment.ALIGN_OPPOSITE).also { elapsedCache = elapsed to it }
        val totalLayout = totalCache?.takeIf { it.first == total }?.second
            ?: timeLayout(total, halfWidth, Layout.Alignment.ALIGN_NORMAL).also { totalCache = total to it }
        // RTL layouts: ALIGN_OPPOSITE is the left edge, ALIGN_NORMAL the right edge.
        brand.draw(canvas, elapsedLayout, ShareFrameLayout.CONTENT_LEFT, ShareFrameLayout.TIMES_TOP)
        brand.draw(canvas, totalLayout, ShareFrameLayout.CONTENT_LEFT + halfWidth, ShareFrameLayout.TIMES_TOP)
    }

    private fun drawTitleAndDate(canvas: Canvas, content: ShareCardContent, titleTop: Float) {
        val width = ShareFrameLayout.CONTENT_WIDTH.toInt()
        val dateLine = ArabicNumerals.formatDateLine(content.hijriDate, content.gregorianDate)
        val date = dateLine?.let {
            brand.fitLayout(it, brand.medium, ShareFrameLayout.DATE_SIZE, 26f, Colors.goldLight, width, Layout.Alignment.ALIGN_CENTER, 1)
        }
        val dateBlock = date?.let { ShareFrameLayout.DATE_GAP_AFTER_TITLE + it.height } ?: 0f

        // The title's height budget is whatever keeps title + date above the centre band.
        val title = brand.fitLayout(
            text = ArabicNumerals.digits(content.title),
            typeface = brand.bold,
            maxSize = ShareFrameLayout.TITLE_MAX_SIZE,
            minSize = ShareFrameLayout.TITLE_MIN_SIZE,
            color = Colors.onSurface,
            width = width,
            alignment = Layout.Alignment.ALIGN_CENTER,
            maxLines = ShareFrameLayout.TITLE_MAX_LINES,
            maxHeight = ShareFrameLayout.CENTER_BAND_TOP - titleTop - dateBlock,
        )
        brand.draw(canvas, title, ShareFrameLayout.CONTENT_LEFT, titleTop)
        date?.let {
            brand.draw(canvas, it, ShareFrameLayout.CONTENT_LEFT, titleTop + title.height + ShareFrameLayout.DATE_GAP_AFTER_TITLE)
        }
    }


    private fun timeLayout(text: String, width: Float, alignment: Layout.Alignment): StaticLayout =
        brand.layout(text, brand.textPaint(brand.regular, ShareFrameLayout.TIMES_SIZE, Colors.secondaryText), width.toInt(), alignment, maxLines = 1)
}
