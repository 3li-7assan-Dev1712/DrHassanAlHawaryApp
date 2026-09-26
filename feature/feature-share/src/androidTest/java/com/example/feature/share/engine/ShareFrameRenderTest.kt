package com.example.feature.share.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.ui.R
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.domain.ShareCardContent
import com.example.domain.text.ShareTitleParser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.sin

/**
 * Renders the share frame for the layout edge cases and writes PNGs to
 * `cacheDir/share_frame_renders/` for visual review against the design:
 *
 *   adb shell run-as com.example.feature.share.test ls cache/share_frame_renders
 *
 * Also asserts the one hard layout rule that's checkable from pixels: nothing
 * but background inside the centre band's top edge region where the title
 * group must never reach.
 */
@RunWith(AndroidJUnit4::class)
class ShareFrameRenderTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val outDir = File(context.cacheDir, "share_frame_renders").apply { mkdirs() }

    private fun content(rawTitle: String, categoryId: String?, quote: String?): ShareCardContent {
        val fields = ShareTitleParser.toCardFields(rawTitle, categoryId)
        return ShareCardContent(
            title = fields.title,
            category = null,
            instituteName = "",
            background = ShareBackgroundSource.Gradient,
            logoResId = R.drawable.admin_logo_app,
            kindLabel = fields.kindLabel,
            hijriDate = fields.hijriDate,
            gregorianDate = fields.gregorianDate,
            quote = quote,
        )
    }

    private fun render(name: String, content: ShareCardContent, progress: Float = 0.4f): Bitmap {
        val bitmap = ShareCardBitmapRenderer().render(context, content)
        val envelope = FloatArray(1800) { i -> (0.5f + 0.45f * sin(i / 23f) * sin(i / 7f)).coerceIn(0f, 1f) }
        ShareFramePainter(context).drawWaveform(
            Canvas(bitmap), WaveformBars.fromEnvelope(envelope), progress,
            elapsedMs = (progress * 60_000).toLong(), totalMs = 60_000,
        )
        File(outDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return bitmap
    }

    @Test
    fun rendersEdgeCases() {
        val khutba = "خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م"
        val quote = "ما من أيامٍ العملُ الصالحُ فيها أحبُّ إلى الله من هذه الأيام"
        val veryLong = "خطبة بعنوان: " + "وجوب الاعتصام بالكتاب والسنة وفهم السلف الصالح والتحذير من البدع والمحدثات في الدين وأثر ذلك على الفرد والمجتمع" +
            " - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م"

        listOf(
            "01_khutba_with_quote" to content(khutba, "khotab", quote),
            "02_khutba_no_quote" to content(khutba, "khotab", null),
            "03_lecture_bare_kind" to content("محاضرة - 6 ربيع الآخر 1448هـ", "lectures", null),
            "04_short_title_lesson" to content("الصبر", "scientific_lessons", ""),
            "05_very_long_title" to content(veryLong, "khotab", quote),
            "06_no_category_no_date" to content("شرح الأربعين النووية (3)", null, null),
        ).forEach { (name, content) ->
            val bitmap = render(name, content)
            assertTitleGroupClearOfCenterBand(name, bitmap)
            bitmap.recycle()
        }
    }

    /** Rows just below CENTER_BAND_TOP (down to where the quote card may start) must be pure background. */
    private fun assertTitleGroupClearOfCenterBand(name: String, bitmap: Bitmap) {
        val background = ShareFrameLayout.Colors.background
        for (y in ShareFrameLayout.CENTER_BAND_TOP.toInt() until ShareFrameLayout.QUOTE_TOP.toInt() step 4) {
            for (x in 0 until bitmap.width step 4) {
                assertEquals("$name: non-background pixel at ($x,$y) inside the centre band", background, bitmap.getPixel(x, y))
            }
        }
    }
}
