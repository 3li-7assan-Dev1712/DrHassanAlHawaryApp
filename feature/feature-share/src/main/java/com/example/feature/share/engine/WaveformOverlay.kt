package com.example.feature.share.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import androidx.media3.common.OverlaySettings
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.StaticOverlaySettings

/**
 * Animates the waveform strip for the exported video. Only the strip region
 * is redrawn per frame (roughly 1080x290, not the full 1080x1920 frame) - the
 * rest of the card comes from [ShareCardBitmapRenderer]'s static image, which
 * Transformer uses as the base video frame; this overlay draws on top of it.
 *
 * Double-buffers two bitmaps and alternates on every call. BitmapOverlay
 * re-uploads its GL texture when the Bitmap reference *or* its generationId
 * changes; mutating one buffer does bump generationId, but double-buffering
 * removes the question entirely for one extra small allocation (see the
 * spec's §4.6 "Buffer gotcha" - if the exported waveform ever looks frozen,
 * this is the first place to check).
 */
class WaveformOverlay(
    private val envelope: FloatArray,
    private val spec: ShareCardSpec,
    accentColorArgb: Int = 0xFF036B5C.toInt(),
) : BitmapOverlay() {

    private val stripWidthPx = (spec.waveform.width * spec.referenceWidthPx).toInt().coerceAtLeast(1)
    private val stripHeightPx = (spec.waveform.height * spec.referenceHeightPx).toInt().coerceAtLeast(1)

    private val buffers = Array(2) { Bitmap.createBitmap(stripWidthPx, stripHeightPx, Bitmap.Config.ARGB_8888) }
    private var index = 0

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColorArgb }

    // NDC (-1..1, Y up) anchor mapping spec.waveform's centre from 0..1 (top-left, Y down).
    private val settings: OverlaySettings = StaticOverlaySettings.Builder()
        .setOverlayFrameAnchor(0f, 0f)
        .setBackgroundFrameAnchor(
            (spec.waveform.left + spec.waveform.right) - 1f,
            1f - (spec.waveform.top + spec.waveform.bottom),
        )
        .setScale(1f, 1f)
        .build()

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val bitmap = buffers[index]
        index = 1 - index

        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        if (envelope.isNotEmpty()) {
            val frame = ((presentationTimeUs * FRAME_RATE) / 1_000_000L).toInt()
            drawBars(canvas, frame)
        }

        return bitmap
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings = settings

    private fun drawBars(canvas: Canvas, frame: Int) {
        val barWidth = stripWidthPx.toFloat() / BAR_COUNT
        val gap = barWidth * 0.25f
        val centerY = stripHeightPx / 2f
        val minHeight = stripHeightPx * MIN_HEIGHT_FRACTION

        // Window the envelope around the current frame so bars scroll rather than flicker.
        val windowStart = (frame - BAR_COUNT / 2).coerceIn(0, (envelope.size - BAR_COUNT).coerceAtLeast(0))

        for (i in 0 until BAR_COUNT) {
            val envelopeIndex = (windowStart + i).coerceIn(0, envelope.size - 1)
            val amplitude = envelope[envelopeIndex].coerceIn(0f, 1f)
            val barHeight = (stripHeightPx * amplitude).coerceAtLeast(minHeight)
            val left = i * barWidth + gap / 2f
            val rect = RectF(left, centerY - barHeight / 2f, left + barWidth - gap, centerY + barHeight / 2f)
            canvas.drawRoundRect(rect, barWidth / 2f, barWidth / 2f, barPaint)
        }
    }

    override fun release() {
        super.release()
        buffers.forEach { it.recycle() }
    }

    companion object {
        private const val BAR_COUNT = 48
        private const val MIN_HEIGHT_FRACTION = 6f / 360f
        private const val FRAME_RATE = 30
    }
}
