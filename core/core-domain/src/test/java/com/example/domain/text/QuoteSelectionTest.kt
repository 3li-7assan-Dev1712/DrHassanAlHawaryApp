package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteSelectionTest {

    private fun sentences(paragraph: String) = SentenceSplitter.split(paragraph).map { paragraph.substring(it.first, it.last + 1) }

    // --- Sentence splitting ---

    @Test
    fun `a leading list number stays with its sentence`() {
        val s = sentences("١. هبوط فظيع في قيمة العملة الوطنية أمام العملات الأخرى. ٢. غلاء الأسعار في كل السلع والخدمات.")
        assertEquals(2, s.size)
        assertTrue(s[0], s[0].startsWith("١. هبوط فظيع"))
        assertTrue(s[1], s[1].startsWith("٢. غلاء الأسعار"))
    }

    @Test
    fun `never splits on the arabic comma`() {
        val s = sentences("فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى.")
        assertEquals(1, s.size)
    }

    @Test
    fun `splits on full stops and question marks`() {
        val s = sentences("هذا هو السؤال الأول في الباب. وما حكم من فعل ذلك متعمدًا؟ الجواب أن ذلك لا يجوز شرعًا.")
        assertEquals(3, s.size)
        assertEquals("وما حكم من فعل ذلك متعمدًا؟", s[1])
    }

    @Test
    fun `short fragments merge into the next sentence, a short last one into the previous`() {
        val s = sentences("قلت: نعم. هذا الكلام صحيح في جملته وتفصيله. انتهى.")
        assertEquals(1, s.size)
        assertTrue(s[0].startsWith("قلت: نعم."))
        assertTrue(s[0].endsWith("انتهى."))
    }

    private val content = listOf(
        "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير",
        "الشيخ د.حسن أحمد الهواري • September 23 at 9:55 PM",
        "📝 خدمة المقالات والمقتطفات 📝",
        "═══════✿✿✿═══════",
        "بسم الله الرحمن الرحيم",
        "الحمد لله، وصلى الله وسلم على رسول الله، وبعد:",
        "",
        "فإن بلادنا تعيش أزمة *اقتصادية* طاحنة. وأسبابها جلية واضحة للجميع.",
        "ـــــــــــ",
        "▪ أولًا: المظاهر",
        "١. هبوط فظيع في قيمة العملة الوطنية.",
    ).joinToString("\n")

    @Test
    fun `paragraph breaks are hard boundaries and ranges point into displayText`() {
        val doc = QuoteDocument.build(content, "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير")
        assertEquals(ArticleText.displayText(content), doc.displayText)
        // Boilerplate, byline, title repeat and separators are gone; the ornament stays.
        assertFalse(doc.sentences.any { it.text.contains("September") || it.text.contains("خدمة") || it.text.contains("═") })
        assertTrue(doc.blocks.contains(QuoteBlock.Ornament))
        // Every sentence's range holds exactly its raw text, and no sentence crosses a paragraph.
        doc.sentences.forEach { sentence ->
            val raw = doc.displayText.substring(sentence.start, sentence.end)
            assertFalse(raw, raw.contains("\n"))
        }
        val kinds = doc.blocks.filterIsInstance<QuoteBlock.Paragraph>().map { it.kind }
        assertEquals(QuoteBlock.Kind.Basmala, kinds.first())
        assertTrue(kinds.contains(QuoteBlock.Kind.Heading))
        val heading = doc.blocks.filterIsInstance<QuoteBlock.Paragraph>().first { it.kind == QuoteBlock.Kind.Heading }
        assertEquals("أولًا: المظاهر", heading.sentences.single().text)
        // Display form drops the *bold* markers; the range keeps the original.
        val bold = doc.sentences.first { it.text.contains("اقتصادية") }
        assertEquals("فإن بلادنا تعيش أزمة اقتصادية طاحنة.", bold.text)
        // A selection across paragraphs keeps the paragraph break between them.
        val first = doc.sentences.indexOf(bold)
        val range = doc.range(first, first + 1)
        assertEquals("فإن بلادنا تعيش أزمة *اقتصادية* طاحنة. وأسبابها جلية واضحة للجميع.", doc.displayText.substring(range))
        val across = doc.range(first, doc.sentences.lastIndex)
        assertTrue(doc.displayText.substring(across).contains("\n\n"))
    }

    // --- Tap selection ---

    @Test
    fun `tap selects, extends either way, removes from either end, resets elsewhere`() {
        var sel: SentenceSelection? = null
        sel = SentenceSelection.tap(sel, 5); assertEquals(SentenceSelection(5, 5), sel)
        sel = SentenceSelection.tap(sel, 6); assertEquals(SentenceSelection(5, 6), sel)
        sel = SentenceSelection.tap(sel, 4); assertEquals(SentenceSelection(4, 6), sel)
        sel = SentenceSelection.tap(sel, 4); assertEquals(SentenceSelection(5, 6), sel)
        sel = SentenceSelection.tap(sel, 6); assertEquals(SentenceSelection(5, 5), sel)
        sel = SentenceSelection.tap(sel, 5); assertNull(sel)
        sel = SentenceSelection.tap(SentenceSelection(2, 8), 20); assertEquals(SentenceSelection(20, 20), sel)
        // Tapping the middle of a selection starts a new one there (it can't leave a gap).
        sel = SentenceSelection.tap(SentenceSelection(2, 8), 5); assertEquals(SentenceSelection(5, 5), sel)
    }

    @Test
    fun `the selection is always contiguous`() {
        var sel: SentenceSelection? = null
        val taps = listOf(3, 4, 5, 2, 9, 8, 10, 10, 9, 7, 8, 8, 1, 0, 2, 2)
        for (t in taps) {
            sel = SentenceSelection.tap(sel, t)
            sel?.let { assertTrue(it.first <= it.last) }
        }
    }

    // --- Counter ---

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
        assertEquals("٣٤٠ حرفًا", chars(340))
        assertEquals("١٠٣ أحرف", chars(103))

        assertEquals("صورة واحدة", ArabicDates.count(1, ArabicDates.IMAGE))
        assertEquals("صورتان", ArabicDates.count(2, ArabicDates.IMAGE))
        assertEquals("٣ صور", ArabicDates.count(3, ArabicDates.IMAGE))

        assertEquals("٩٨ حرفًا · صورة واحدة", quoteCounter(98, 1))
        assertEquals("٣٤٠ حرفًا · صورتان", quoteCounter(340, 2))
    }
}
