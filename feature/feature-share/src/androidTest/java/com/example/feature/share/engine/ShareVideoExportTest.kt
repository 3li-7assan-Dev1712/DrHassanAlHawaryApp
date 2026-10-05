package com.example.feature.share.engine

import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.ui.R
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.domain.ShareCardContent
import com.example.domain.text.ShareTitleParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * End-to-end: synthetic audio -> WaveformAnalyzer -> base frame -> WaveformOverlay
 * -> Media3 export -> decode frames of the MP4 and check them. Covers the three
 * bugs found in the old export (docs/plans/share-video-redesign.md, step 0):
 * letterbox, waveform desync / overflow, and one loud peak flattening the rest.
 * Sampled frames are also written to `cacheDir/share_export_frames/` for review.
 */
@UnstableApi
@RunWith(AndroidJUnit4::class)
class ShareVideoExportTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val outDir = File(context.cacheDir, "share_export_frames").apply { mkdirs() }

    @Test
    fun analyzerKeepsQuietSpeechVisibleAfterALoudSpike() = runBlocking {
        val wav = writeTestWav()
        val envelope = WaveformAnalyzer().analyze(wav.absolutePath, CLIP_MS)

        assertEquals(CLIP_MS.toInt() / 1000 * 30, envelope.size)
        val loudSpeech = envelope.slice(bucket(0.5)..bucket(4.5)).average()
        val silence = envelope.slice(bucket(5.6)..bucket(7.4)).average()
        val quietSpeech = envelope.slice(bucket(9.0)..bucket(19.0)).average()
        assertTrue("silence should read as ~0, was $silence", silence < 0.1)
        // Quiet speech is recorded at 1/4 of the loud level. Normalised against the
        // spike (the old behaviour) it would sit near 0.1 of the loud speech; against
        // the 95th percentile with gamma 0.6 it keeps roughly 0.25^0.6 ≈ 0.44 of it.
        val ratio = quietSpeech / loudSpeech
        assertTrue("the spike must not flatten quiet speech: ratio $ratio (quiet $quietSpeech, loud $loudSpeech)", ratio > 0.35)
        assertTrue("quiet speech should still show bars, was $quietSpeech", quietSpeech > 0.2)
    }

    @Test
    fun exportFillsTheFrameAndTheWaveformTracksTime() = runBlocking {
        val wav = writeTestWav()
        val envelope = WaveformAnalyzer().analyze(wav.absolutePath, CLIP_MS)

        val fields = ShareTitleParser.toCardFields(
            "خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م", "khotab",
        )
        val content = ShareCardContent(
            title = fields.title, category = null, instituteName = "",
            background = ShareBackgroundSource.Gradient, logoResId = R.drawable.admin_logo_app,
            kindLabel = fields.kindLabel, hijriDate = fields.hijriDate, gregorianDate = fields.gregorianDate,
        )
        val base = File(context.cacheDir, "test_base.png")
        ShareCardBitmapRenderer().render(context, content).also { bitmap ->
            base.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }

        val output = File(context.cacheDir, "test_share.mp4").apply { delete() }
        val overlay = WaveformOverlay(context, envelope, CLIP_MS, ShareVideoExporter.OUTPUT_WIDTH)
        val result = ShareVideoExporter()
            .export(context, base, wav.absolutePath, CLIP_MS, overlay, output)
            .first { it is ExportProgress.Completed || it is ExportProgress.Failed }
        assertTrue("export failed: $result", result is ExportProgress.Completed)

        val retriever = MediaMetadataRetriever().apply { setDataSource(output.absolutePath) }
        try {
            val early = frameAt(retriever, 0.3, "early")
            val middle = frameAt(retriever, 10.0, "middle")
            val late = frameAt(retriever, 19.6, "late")

            // #5: full-bleed. Every corner and edge midpoint is the frame background, not black bars.
            for (frame in listOf(early, late)) {
                val w = frame.width
                val h = frame.height
                for ((x, y) in listOf(2 to 2, w - 3 to 2, 2 to h - 3, w - 3 to h - 3, w / 2 to 2, w / 2 to h - 3)) {
                    assertColorNear("edge ($x,$y)", ShareFrameLayout.Colors.background, frame.getPixel(x, y))
                }
            }

            // #6: progress follows the clip - little gold at the start, about half in the middle, nearly all at the end.
            val earlyGold = goldBarFraction(early)
            val middleGold = goldBarFraction(middle)
            val lateGold = goldBarFraction(late)
            assertTrue("early gold $earlyGold", earlyGold < 0.1)
            assertTrue("middle gold $middleGold", middleGold in 0.35..0.65)
            assertTrue("late gold $lateGold", lateGold > 0.9)

            // #6: the strip stays inside the side margins (it used to overflow at 126% width).
            val y = (ShareFrameLayout.waveform.centerY() / ShareFrameLayout.HEIGHT * late.height).toInt()
            val marginPx = (ShareFrameLayout.SIDE_MARGIN / ShareFrameLayout.WIDTH * late.width).toInt() - 4
            for (x in 0 until marginPx) {
                assertColorNear("left margin ($x,$y)", ShareFrameLayout.Colors.background, late.getPixel(x, y))
                assertColorNear("right margin", ShareFrameLayout.Colors.background, late.getPixel(late.width - 1 - x, y))
            }
        } finally {
            retriever.release()
        }
    }

    private fun frameAt(retriever: MediaMetadataRetriever, seconds: Double, name: String): Bitmap {
        val frame = requireNotNull(
            retriever.getFrameAtTime((seconds * 1_000_000).toLong(), MediaMetadataRetriever.OPTION_CLOSEST)
        ).copy(Bitmap.Config.ARGB_8888, false)
        File(outDir, "$name.png").outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return frame
    }

    /** Fraction of the bar columns, sampled along the strip's centre line, that are gold. */
    private fun goldBarFraction(frame: Bitmap): Double {
        val strip = ShareFrameLayout.waveform
        val scale = frame.width / ShareFrameLayout.WIDTH
        val y = (strip.centerY() * scale).toInt()
        val pitch = strip.width() / ShareFrameLayout.BAR_COUNT
        var gold = 0
        for (i in 0 until ShareFrameLayout.BAR_COUNT) {
            val x = ((strip.left + (i + 0.5f) * pitch) * scale).toInt()
            if (isNear(ShareFrameLayout.Colors.gold, frame.getPixel(x, y), 40)) gold++
        }
        return gold.toDouble() / ShareFrameLayout.BAR_COUNT
    }

    private fun assertColorNear(label: String, expected: Int, actual: Int) {
        assertTrue(
            "$label: expected #%06X, was #%06X".format(expected and 0xFFFFFF, actual and 0xFFFFFF),
            isNear(expected, actual, 14),
        )
    }

    private fun isNear(expected: Int, actual: Int, tolerance: Int): Boolean =
        abs(Color.red(expected) - Color.red(actual)) <= tolerance &&
            abs(Color.green(expected) - Color.green(actual)) <= tolerance &&
            abs(Color.blue(expected) - Color.blue(actual)) <= tolerance

    private fun bucket(seconds: Double) = (seconds * 30).toInt()

    /**
     * 20 s mono 16 kHz WAV: 0-5 s normal "speech" (noise with a syllable-rate
     * envelope), 5-8 s silence, one full-scale 100 ms spike at 8 s, then quiet
     * speech (15% level) to the end.
     */
    private fun writeTestWav(): File {
        val sampleRate = 16_000
        val total = (CLIP_MS / 1000 * sampleRate).toInt()
        val random = Random(7)
        val pcm = ShortArray(total) { i ->
            val t = i.toDouble() / sampleRate
            val syllables = 0.5 + 0.5 * sin(2 * PI * 4 * t)
            val level = when {
                t < 5.0 -> 0.6 * syllables
                t < 8.0 -> 0.0
                t < 8.1 -> 1.0
                else -> 0.15 * syllables
            }
            ((random.nextDouble() * 2 - 1) * level * Short.MAX_VALUE).toInt().toShort()
        }
        val file = File(context.cacheDir, "test_clip.wav")
        DataOutputStream(FileOutputStream(file).buffered()).use { out ->
            fun intLE(v: Int) = out.write(byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte()))
            fun shortLE(v: Int) = out.write(byteArrayOf(v.toByte(), (v shr 8).toByte()))
            val dataBytes = total * 2
            out.writeBytes("RIFF"); intLE(36 + dataBytes); out.writeBytes("WAVE")
            out.writeBytes("fmt "); intLE(16); shortLE(1); shortLE(1); intLE(sampleRate); intLE(sampleRate * 2); shortLE(2); shortLE(16)
            out.writeBytes("data"); intLE(dataBytes)
            pcm.forEach { shortLE(it.toInt()) }
        }
        return file
    }

    private companion object {
        const val CLIP_MS = 20_000L
    }
}
