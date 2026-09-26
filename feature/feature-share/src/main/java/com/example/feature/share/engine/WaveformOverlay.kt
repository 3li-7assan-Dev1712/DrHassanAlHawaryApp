package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import androidx.media3.common.OverlaySettings
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.StaticOverlaySettings
import kotlin.math.ceil

/**
 * Animates the waveform + times for the exported video by calling the same
 * [ShareFramePainter.drawWaveform] the live preview uses, once per frame, into
 * a bitmap covering just [ShareFrameLayout.animatedRegion] (the rest of the
 * frame is [ShareCardBitmapRenderer]'s static base image underneath).
 *
 * - Progress comes from the frame's own presentation time over the clip length,
 *   so it can't desync from the audio. (The old version indexed a 30-per-second
 *   envelope with a 20 fps frame counter and drifted to 2/3 speed.)
 * - The bitmap is drawn at OUTPUT resolution: BitmapOverlay composites it 1:1 in
 *   output pixels, so a 1080-reference-sized strip on the 720-wide output ran
 *   126% too wide.
 * - Double-buffered: BitmapOverlay re-uploads its texture when the Bitmap
 *   reference or its generationId changes; alternating two buffers removes any
 *   doubt (spec §4.6 "Buffer gotcha").
 */
@UnstableApi
class WaveformOverlay(
    context: Context,
    envelope: FloatArray,
    private val clipDurationMs: Long,
    outputWidthPx: Int,
) : BitmapOverlay() {

    private val painter = ShareFramePainter(context)
    private val bars = WaveformBars.fromEnvelope(envelope)
    private val region = ShareFrameLayout.animatedRegion
    private val scale = outputWidthPx / ShareFrameLayout.WIDTH

    private val buffers = Array(2) {
        Bitmap.createBitmap(
            ceil(region.width() * scale).toInt().coerceAtLeast(1),
            ceil(region.height() * scale).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
    }
    private var index = 0

    // NDC (-1..1, Y up): pin the bitmap's centre to the region's centre on the frame.
    private val settings: OverlaySettings = StaticOverlaySettings.Builder()
        .setOverlayFrameAnchor(0f, 0f)
        .setBackgroundFrameAnchor(
            region.centerX() / ShareFrameLayout.WIDTH * 2f - 1f,
            1f - region.centerY() / ShareFrameLayout.HEIGHT * 2f,
        )
        .setScale(1f, 1f)
        .build()

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val bitmap = buffers[index]
        index = 1 - index

        val elapsedMs = (presentationTimeUs / 1_000L).coerceIn(0L, clipDurationMs)
        val progress = if (clipDurationMs > 0) elapsedMs.toFloat() / clipDurationMs else 0f

        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        canvas.scale(scale, scale)
        canvas.translate(-region.left, -region.top)
        painter.drawWaveform(canvas, bars, progress, elapsedMs, clipDurationMs)
        return bitmap
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings = settings

    override fun release() {
        super.release()
        buffers.forEach { it.recycle() }
    }
}
