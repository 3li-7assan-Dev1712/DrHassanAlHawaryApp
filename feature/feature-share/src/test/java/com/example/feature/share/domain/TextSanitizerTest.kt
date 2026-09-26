package com.example.feature.share.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSanitizerTest {

    // From the article in the broken quote image (docs: bugs 1 and 2).
    private val article =
        "وهذا ما نراه اليوم في الجنيه. هبوط فظيع في قيمة سعر الصرف مقابل العملات الأخرى، حتى وصلت أرقامًا فلكية " +
            "يصعب حسابها، فهل تعلم عزيزي القارئ أن سعر الجنيه السوداني كان يومًا من الأيام يعادل عشرين ريالًا سعوديًا " +
            "ثم بدأ في الهبوط حتى أصبح الريال يعادل ألف جنيه سوداني فقط."

    private fun select(from: String, toBefore: String): Pair<Int, Int> {
        val start = article.indexOf(from)
        val end = article.indexOf(toBefore, start) + toBefore.length
        return start to end
    }

    @Test
    fun `a selection that grabbed the previous full stop does not start with it`() {
        val start = article.indexOf(". هبوط")
        val end = article.indexOf("فلكية") + "فلكية".length
        val result = TextSanitizer.sanitize(article, start, end)

        assertTrue(result.text, result.text.startsWith("هبوط فظيع"))
    }

    @Test
    fun `a selection ending mid-word snaps to the whole word from the full text`() {
        val (start, end) = select("هبوط", "ألف جنيه سودا")
        val result = TextSanitizer.sanitize(article, start, end)

        assertTrue(result.text, result.text.endsWith("ألف جنيه سوداني…"))
    }

    @Test
    fun `a selection starting mid-word snaps back to the word start`() {
        val start = article.indexOf("بوط فظيع")
        val end = article.indexOf("الصرف") + "الصرف".length
        assertEquals("هبوط فظيع في قيمة سعر الصرف…", TextSanitizer.sanitize(article, start, end).text)
    }

    @Test
    fun `sentence-final punctuation is kept and no ellipsis is added`() {
        val (start, end) = select("ثم بدأ", "فقط.")
        assertEquals("ثم بدأ في الهبوط حتى أصبح الريال يعادل ألف جنيه سوداني فقط.", TextSanitizer.sanitize(article, start, end).text)
    }

    @Test
    fun `trailing comma is trimmed before the ellipsis`() {
        val (start, end) = select("هبوط", "حسابها،")
        assertTrue(TextSanitizer.sanitize(article, start, end).text.endsWith("يصعب حسابها…"))
    }

    @Test
    fun `whitespace collapses, paragraphs keep one break, dots become an ellipsis`() {
        val text = "أولًا   هذا   نص.....\n\n\n  ثانيًا نص آخر."
        assertEquals("أولًا هذا نص…\nثانيًا نص آخر.", TextSanitizer.sanitize(text, 0, text.length).text)
    }

    @Test
    fun `emojis, tatweel and facebook separator lines are removed`() {
        val text = "الحمد لله 🌹🤍 على نعمه\n==========\nوالصلاة والسلام على رسول الله ﷺ\nـــــــــــــ\nقال الشيـــخ كلامًا."
        assertEquals(
            "الحمد لله على نعمه\nوالصلاة والسلام على رسول الله ﷺ\nقال الشيخ كلامًا.",
            TextSanitizer.sanitize(text, 0, text.length).text,
        )
    }

    @Test
    fun `box-drawing and flower separator lines from the real article are removed`() {
        val text = "مظاهر، أسباب، وتدابير\n═══════✿✿✿═══════\nالحمد لله، وبعد:"
        assertEquals("مظاهر، أسباب، وتدابير\nالحمد لله، وبعد…", TextSanitizer.sanitize(text, 0, text.length).text)
    }

    @Test
    fun `the facebook byline copied into the article body is removed`() {
        val text = "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير\n\n" +
            "الشيخ د.حسن أحمد الهواري • September 23 at 9:55 PM\n\nبسم الله الرحمن الرحيم"
        assertEquals(
            "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير\nبسم الله الرحمن الرحيم…",
            TextSanitizer.sanitize(text, 0, text.length).text,
        )
    }

    @Test
    fun `a line that merely mentions a date in english is kept`() {
        val text = "قال الشيخ في درسه September 23 كلامًا مهمًا."
        assertEquals(text, TextSanitizer.sanitize(text, 0, text.length).text)
    }

    @Test
    fun `unmatched brackets and quotes are dropped, matched pairs kept`() {
        val text = "قال (رحمه الله) كلامًا «مهمًا جدًا وتابع (في المسألة"
        val result = TextSanitizer.sanitize(text, 0, text.length).text

        assertEquals("قال (رحمه الله) كلامًا مهمًا جدًا وتابع في المسألة…", result)
        // Only the matched pair's "(" survives - never an unclosed one.
        assertEquals(1, result.count { it == '(' })
        assertFalse(result.contains('«'))
    }

    @Test
    fun `a leading opening quote whose closer was cut off is removed`() {
        val text = "«إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى» متفق عليه."
        val end = text.indexOf("نوى") + 3
        assertEquals("إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى…", TextSanitizer.sanitize(text, 0, end).text)
    }

    @Test
    fun `a highlight range follows the text through cleanup`() {
        val start = article.indexOf(". هبوط")
        val end = article.indexOf("سعوديًا") + "سعوديًا".length
        val hStart = article.indexOf("سعر الجنيه")
        val hEnd = article.indexOf("سعوديًا") + "سعوديًا".length
        val result = TextSanitizer.sanitize(article, start, end, hStart until hEnd)

        val highlighted = result.text.substring(result.highlightStart!!, result.highlightEnd!!)
        assertEquals("سعر الجنيه السوداني كان يومًا من الأيام يعادل عشرين ريالًا سعوديًا", highlighted)
    }

    @Test
    fun `highlight outside the selection maps to nothing`() {
        val (start, end) = select("هبوط", "الصرف")
        val result = TextSanitizer.sanitize(article, start, end, 0 until 5)
        assertNull(result.highlightStart)
    }

    @Test
    fun `empty or punctuation-only selections produce empty text`() {
        assertEquals("", TextSanitizer.sanitize(article, 10, 10).text)
        val dot = article.indexOf(". ")
        assertEquals("", TextSanitizer.sanitize(article, dot, dot + 2).text)
    }
}
