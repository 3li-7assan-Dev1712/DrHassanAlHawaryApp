package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayCleanersTest {

    private val cleaner = ArticleTextCleaner()

    @Test
    fun `audio titles lose the service labels`() {
        assertEquals(
            "حكم شراء الذهب من التطبيقات والتجار",
            AudioTitleCleaner.clean("خدمة المقاطع الصوتية - مقطع بعنوان: حكم شراء الذهب من التطبيقات والتجار"),
        )
        assertEquals(
            "حكم لبس النقاب",
            AudioTitleCleaner.clean("خدمة المقاطع الصوتية - مقطع بعنوان: حكم لبس النقاب - خدمة فضيلة الشيخ د. حسن"),
        )
        assertEquals("(٢) التعريف بصحيح البخاري", AudioTitleCleaner.clean("مقطع بعنوان: ⏪(٢) التعريف بصحيح البخاري"))
        assertEquals("أحكام الصيام", AudioTitleCleaner.clean("  أحكام   الصيام "))
    }

    @Test
    fun `an audio title that would be empty falls back to the original`() {
        assertEquals("خدمة المقاطع الصوتية", AudioTitleCleaner.clean("خدمة المقاطع الصوتية"))
    }

    @Test
    fun `design titles drop the section prefix and use arabic digits`() {
        assertEquals("٩ ذو الحجة ١٤٤٧هـ", DesignTitle.clean("تصميم - 9 ذو الحجة 1447هـ"))
        assertEquals("فضل العشر", DesignTitle.clean("فضل العشر"))
        assertEquals("تصميم", DesignTitle.clean("تصميم"))
        assertEquals("صورة", DesignTitle.imageCount(1))
        assertEquals("صورتان", DesignTitle.imageCount(2))
        assertEquals("٣ صور", DesignTitle.imageCount(3))
        assertEquals("١١ صورة", DesignTitle.imageCount(11))
    }

    @Test
    fun `handles and ltr fragments are isolated`() {
        assertEquals("⁦@ali_7assan⁩", BidiText.handle("ali_7assan"))
        assertEquals("⁦@ali_7assan⁩", BidiText.handle("@ali_7assan"))
        assertEquals("", BidiText.handle("  "))
        assertEquals("⁦a@b.com⁩", BidiText.ltr("a@b.com"))
    }

    @Test
    fun `whatsapp bold becomes spans or plain text`() {
        assertEquals("قال تعالى في كتابه", InlineBold.strip("قال *تعالى* في كتابه"))
        val parsed = InlineBold.parse("قال *تعالى* في *كتابه*")
        assertEquals("قال تعالى في كتابه", parsed.text)
        assertEquals(listOf(4 until 9, 13 until 18), parsed.bold)
        assertEquals("5 * 3 * 2", InlineBold.strip("5 * 3 * 2"))
    }

    private val rawTitle =
        "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير - الشيخ د.حسن أحمد الهواري September 23 at 9:55 PM"

    private val rawBody = listOf(
        "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير",
        "الشيخ د.حسن أحمد الهواري • September 23 at 9:55 PM",
        "بسم الله الرحمن الرحيم",
        "📝 خدمة المقالات والمقتطفات 📝",
        "═══════✿✿✿═══════",
        "الأزمة الاقتصادية الطاحنة",
        "مظاهر، أسباب، وتدابير",
        "- الشيخ د. حسن الهواري -",
        "=======",
        "الحمد لله، وصلى الله وسلم على رسول الله، وبعد:",
        "",
        "",
        "فإن بلادنا تعيش أزمة *اقتصادية* طاحنة 😔، وأسبابها جلية واضحة.",
        "ـــــــــــ",
        "▪ أولًا: المظاهر",
        "✍️ أقول: معلقًا على هذا الراهن الاقتصادي المنهار، وبالله التوفيق:",
        "••••••",
    ).joinToString("\n")

    @Test
    fun `with the title, the header lines repeating it are dropped`() {
        val body = cleaner.cleanBody(rawBody, rawTitle)
        assertTrue(body, body.startsWith("الحمد لله"))
        assertFalse(body, body.contains("*"))
        assertFalse(body, body.contains("الهواري"))
    }

    @Test
    fun `reader keeps the basmala first, emojis and bold markers, and turns inner separators into one ornament`() {
        val paragraphs = cleaner.readerParagraphs(rawBody, rawTitle)
        assertEquals("بسم الله الرحمن الرحيم", paragraphs.first())
        assertEquals("الحمد لله، وصلى الله وسلم على رسول الله، وبعد:", paragraphs[1])
        assertTrue(paragraphs.toString(), paragraphs.any { it.contains("*اقتصادية*") && it.contains("😔") })
        assertEquals(1, paragraphs.count { it == ArticleTextCleaner.ORNAMENT })
        assertTrue(paragraphs.last() != ArticleTextCleaner.ORNAMENT)
        assertFalse(paragraphs.any { it.contains("خدمة المقالات") || it.contains("═") })
    }

    @Test
    fun `section headings drop the bullet glyph`() {
        assertEquals("أولًا: المظاهر", cleaner.sectionHeading("▪ أولًا: المظاهر"))
        assertEquals("ثانياً: الأسباب", cleaner.sectionHeading("ثانياً: الأسباب"))
        assertEquals("عاشرا : الخاتمة", cleaner.sectionHeading("• عاشرا : الخاتمة"))
        assertNull(cleaner.sectionHeading("أولًا نبدأ بالحديث"))
        assertNull(cleaner.sectionHeading("قال أولًا: كذا"))
    }

    @Test
    fun `the excerpt skips a leading basmala`() {
        assertEquals(
            "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة.",
            cleaner.excerpt(rawBody, rawTitle),
        )
    }
}
