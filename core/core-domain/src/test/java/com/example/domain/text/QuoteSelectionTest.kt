package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteSelectionTest {

    private val content = listOf(
        "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير",
        "الشيخ د.حسن أحمد الهواري • September 23 at 9:55 PM",
        "📝 خدمة المقالات والمقتطفات 📝",
        "═══════✿✿✿═══════",
        "بسم الله الرحمن الرحيم",
        "الحمد لله، وصلى الله وسلم على رسول الله، وبعد:",
        "",
        "فإن بلادنا تعيش أزمة *اقتصادية*   طاحـــنة. وأسبابها جلية واضحة للجميع.",
        "ـــــــــــ",
        "▪ أولًا: المظاهر",
        "١. هبوط فظيع في قيمة العملة الوطنية.",
    ).joinToString("\n")

    private val article = SelectableArticle.build(content, "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير")

    @Test
    fun `shows the cleaned article only`() {
        assertEquals(ArticleText.displayText(content), article.displayText)
        assertFalse(article.text.contains("September") || article.text.contains("خدمة") || article.text.contains("═"))
        assertFalse(article.text, article.text.contains("*") || article.text.contains("ـ") || article.text.contains("  "))
        assertTrue(article.text.startsWith("بسم الله الرحمن الرحيم"))
        assertTrue(article.text.contains("فإن بلادنا تعيش أزمة اقتصادية طاحنة."))
        val kinds = article.blocks.map { it.kind }
        assertEquals(SelectableArticle.Kind.Basmala, kinds.first())
        assertTrue(kinds.contains(SelectableArticle.Kind.Ornament))
        val heading = article.blocks.first { it.kind == SelectableArticle.Kind.Heading }
        assertEquals("أولًا: المظاهر", article.text.substring(heading.start, heading.end))
    }

    @Test
    fun `a dragged selection maps back to the original text`() {
        val shown = "اقتصادية طاحنة"
        val start = article.text.indexOf(shown)
        val range = article.toSource(start, start + shown.length)!!
        // The original keeps its markup; the quote-image step's sanitizer cleans it.
        assertEquals("اقتصادية*   طاحـــنة", article.displayText.substring(range))
    }

    @Test
    fun `a selection across paragraphs keeps the paragraph break`() {
        val from = article.text.indexOf("وأسبابها")
        val to = article.text.indexOf("المظاهر") + "المظاهر".length
        val source = article.displayText.substring(article.toSource(from, to)!!)
        assertTrue(source, source.startsWith("وأسبابها"))
        assertTrue(source, source.endsWith("المظاهر"))
        assertTrue(source, source.contains("\n\n"))
    }

    @Test
    fun `selecting only breaks or the ornament gives nothing`() {
        val ornament = article.blocks.first { it.kind == SelectableArticle.Kind.Ornament }
        assertNull(article.toSource(ornament.start, ornament.end))
        assertNull(article.toSource(5, 5))
        val gap = article.blocks[0].end
        assertNull(article.toSource(gap, gap + SelectableArticle.BLOCK_BREAK.length))
    }

    @Test
    fun `character and image counts use every arabic plural form`() {
        fun chars(n: Long) = ArabicDates.count(n, ArabicDates.CHARACTER)
        assertEquals("حرف واحد", chars(1))
        assertEquals("حرفان", chars(2))
        assertEquals("٣ أحرف", chars(3))
        assertEquals("١٠ أحرف", chars(10))
        assertEquals("١١ حرفًا", chars(11))
        assertEquals("٩٨ حرفًا", chars(98))
        assertEquals("١٠٠ حرف", chars(100))
        assertEquals("٢١٢ حرفًا", chars(212))
        assertEquals("١٠٣ أحرف", chars(103))
        assertEquals("صورة واحدة", ArabicDates.count(1, ArabicDates.IMAGE))
        assertEquals("صورتان", ArabicDates.count(2, ArabicDates.IMAGE))
        assertEquals("٣ صور", ArabicDates.count(3, ArabicDates.IMAGE))
        assertEquals("٩٨ حرفًا · صورة واحدة", quoteCounter(98, 1))
        assertEquals("٣٤٠ حرفًا · صورتان", quoteCounter(340, 2))
    }
}
