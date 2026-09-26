package com.example.feature.share.engine

import android.graphics.Bitmap
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.ui.R
import com.example.feature.share.domain.ArabicNumerals
import com.example.feature.share.domain.ArticleText
import com.example.feature.share.domain.TextSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

/**
 * Sanitize -> paginate -> render for short and long article excerpts. Writes every
 * page to `cacheDir/quote_card_renders/` for visual review, and checks that
 * pagination never loses, duplicates or reorders text and never overflows a page.
 */
@RunWith(AndroidJUnit4::class)
class QuoteCardRenderTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val outDir = File(context.cacheDir, "quote_card_renders").apply { mkdirs() }

    private val paragraphs = listOf(
        "إن ما تمر به البلاد اليوم من أزمة اقتصادية طاحنة لم يأت من فراغ، بل هو نتيجة تراكمات طويلة من سوء التدبير وضعف الإنتاج والاعتماد على الاستيراد في كل شيء تقريبًا.",
        "هبوط فظيع في قيمة سعر الصرف مقابل العملات الأخرى، حتى وصلت أرقامًا فلكية يصعب حسابها؛ فهل تعلم عزيزي القارئ أن سعر الجنيه السوداني كان يومًا يعادل عشرين ريالًا سعوديًا، ثم بدأ في الهبوط حتى أصبح يعادل ريالًا واحدًا، وإلى تلك اللحظة كان يمكن حساب السقوط والهبوط فيقال مثلًا نزل بنسبة مائة بالمائة فوصل إلى درجة الصفر، ثم أخذ في الهبوط تحت الصفر حتى وصل ألف درجة تحت الصفر عندما أصبح الريال يعادل ألف جنيه سوداني.",
        "==========",
        "ومن مظاهر هذه الأزمة ارتفاع الأسعار ارتفاعًا جنونيًا 📈 في السلع الضرورية من طعام وشراب ودواء، حتى عجز كثير من الناس عن توفير قوت يومهم، وانتشرت البطالة بين الشباب فهاجر منهم من هاجر وبقي من بقي ينتظر الفرج.",
        "أما أسبابها فكثيرة، أعظمها الذنوب والمعاصي، قال الله تعالى: «وما أصابكم من مصيبة فبما كسبت أيديكم ويعفو عن كثير»، ومنها الظلم وأكل أموال الناس بالباطل، ومنها ترك الإنتاج والاعتماد على غيرنا.",
        "وأما التدابير فأولها التوبة الصادقة والرجوع إلى الله، ثم العمل والإنتاج، والاقتصاد في المعيشة، والتكافل بين أفراد المجتمع، وأن يرحم الغني الفقير، وأن يتقي التجار الله في الأسعار...",
    )
    private val article = ArticleText.displayText(paragraphs.joinToString("\n"))
    private val title = ArticleText.cleanTitle("الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير - الشيخ د.حسن الهواري")

    @Test
    fun shortExcerptIsOnePageWithHighlight() {
        // Starts on the previous paragraph's full stop - the stray "." of the old image.
        val start = article.indexOf("تقريبًا.") + "تقريبًا".length
        val end = article.indexOf("سعوديًا") + "سعوديًا".length
        val hStart = article.indexOf("سعر الجنيه")
        val sanitized = TextSanitizer.sanitize(article, start, end, hStart until end)
        val text = highlighted(sanitized.text, sanitized.highlightStart, sanitized.highlightEnd)

        val painter = QuoteCardPainter(context)
        val pages = painter.paginate(text)
        assertEquals(1, pages.size)
        renderAll("short", painter, pages)
    }

    @Test
    fun wholeArticleSplitsIntoOrderedPagesWithoutLosingText() {
        val sanitized = TextSanitizer.sanitize(article, 0, article.length)
        val painter = QuoteCardPainter(context)
        val pages = painter.paginate(ArabicNumerals.digits(sanitized.text))

        assertTrue("expected several pages, got ${pages.size}", pages.size >= 3)
        // Concatenated pages == the excerpt: nothing lost, duplicated or reordered.
        // (Only non-final pages can have a paginator-added "…"; the excerpt's own ending stays.)
        val rejoined = pages.mapIndexed { i, page ->
            if (i < pages.lastIndex) page.toString().removeSuffix("…") else page.toString()
        }.joinToString(" ")
        assertEquals(normalize(ArabicNumerals.digits(sanitized.text)), normalize(rejoined))
        // Every non-final page ends a sentence, ends a paragraph, or ends with "…" - never ":…".
        pages.dropLast(1).forEach { page ->
            assertTrue("page ends with a doubled mark: …${page.takeLast(20)}", !page.endsWith(":…") && !page.endsWith("،…"))
        }
        renderAll("long", painter, pages)
    }

    private fun renderAll(prefix: String, painter: QuoteCardPainter, pages: List<CharSequence>) {
        outDir.listFiles { f -> f.name.startsWith("${prefix}_") }?.forEach { it.delete() }
        pages.forEachIndexed { i, text ->
            val bitmap = QuoteCardRenderer().render(painter, QuotePage(text, i, pages.size, title, R.drawable.admin_logo_app))
            File(outDir, "${prefix}_${i + 1}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            assertGapIsBackground("$prefix page ${i + 1}", bitmap)
            bitmap.recycle()
        }
    }

    /** Body text must never run into the source card: the gap between them stays empty. */
    private fun assertGapIsBackground(name: String, bitmap: Bitmap) {
        val bg = ShareFrameLayout.Colors.background
        for (y in QuoteCardLayout.BODY_BOTTOM.toInt() + 4 until QuoteCardLayout.SOURCE_CARD_TOP.toInt() - 4 step 3) {
            for (x in 0 until bitmap.width step 3) {
                val p = bitmap.getPixel(x, y)
                assertTrue(
                    "$name: text overflowed into the source-card gap at ($x,$y)",
                    abs(Color.red(p) - Color.red(bg)) < 6 && abs(Color.green(p) - Color.green(bg)) < 6,
                )
            }
        }
    }

    private fun highlighted(text: String, start: Int?, end: Int?): CharSequence {
        val s = SpannableString(ArabicNumerals.digits(text))
        if (start != null && end != null) {
            s.setSpan(ForegroundColorSpan(ShareFrameLayout.Colors.goldLight), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return s
    }

    private fun normalize(s: String) = s.replace(Regex("\\s+"), " ").trim()
}
