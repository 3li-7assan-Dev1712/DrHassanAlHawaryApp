package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ArticleTextCleanerTest {

    private val cleaner = ArticleTextCleaner()

    // The exact strings from the home-redesign brief.
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
        "=======",
        "الحمد لله، وصلى الله وسلم على رسول الله، وبعد:",
        "",
        "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى وإليه الملاذ والملتجى.",
        "✍️ أقول: معلقًا على هذا الراهن الاقتصادي المنهار، وبالله التوفيق:",
    ).joinToString("\n")

    @Test
    fun `the brief's title loses the author and the facebook timestamp`() {
        assertEquals("الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير", cleaner.cleanTitle(rawTitle))
    }

    @Test
    fun `arabic facebook timestamps are stripped from titles`() {
        assertEquals("فقه الصيام", cleaner.cleanTitle("فقه الصيام - منذ ساعتين"))
        assertEquals("فقه الصيام", cleaner.cleanTitle("فقه الصيام ٤ د"))
        assertEquals("فقه الصيام", cleaner.cleanTitle("فقه الصيام · منذ ٣ أيام"))
    }

    @Test
    fun `body drops byline, basmala, boilerplate, emojis and separators`() {
        val body = cleaner.cleanBody(rawBody)

        assertFalse(body, body.contains("September"))
        assertFalse(body, body.contains("بسم الله"))
        assertFalse(body, body.contains("خدمة المقالات"))
        assertFalse(body, body.contains("📝") || body.contains("✍"))
        assertFalse(body, body.contains("===") || body.contains("═"))
        assertFalse("blank lines collapse", body.contains("\n\n"))
        assertTrue(body, body.startsWith("الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير\n"))
    }

    @Test
    fun `the excerpt is the first meaningful paragraph, not the title, a heading or the opening`() {
        assertEquals(
            "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى وإليه الملاذ والملتجى.",
            cleaner.excerpt(rawBody, rawTitle),
        )
    }

    @Test
    fun `a basmala with diacritics is dropped too, but not one mid-article`() {
        val body = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nقال تعالى في كتابه الكريم كلامًا طويلًا مهمًا.\nبسم الله الرحمن الرحيم"
        assertEquals("قال تعالى في كتابه الكريم كلامًا طويلًا مهمًا.\nبسم الله الرحمن الرحيم", cleaner.cleanBody(body))
    }

    @Test
    fun `boilerplate list is configurable`() {
        val custom = ArticleTextCleaner(boilerplate = listOf("تابعونا على"))
        assertEquals("نص مفيد.", custom.cleanBody("تابعونا على القناة\nنص مفيد."))
    }

    @Test
    fun `no meaningful paragraph gives an empty excerpt`() {
        assertEquals("", cleaner.excerpt("بسم الله الرحمن الرحيم\n=======\n📝", "عنوان"))
    }

    @Test
    fun `reading time is words over 180, at least a minute`() {
        assertEquals(1, cleaner.readingMinutes("كلمة"))
        val words = List(181) { "كلمة" }.joinToString(" ")
        assertEquals(2, cleaner.readingMinutes(words))
        assertEquals(7, cleaner.readingMinutes(List(1200) { "كلمة" }.joinToString(" ")))
    }

    @Test
    fun `publish date is read from the byline, assuming the latest past year`() {
        val utc = TimeZone.getTimeZone("UTC")
        val now = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.SEPTEMBER, 26, 12, 0) }.timeInMillis

        val published = cleaner.publishedAtInText(rawBody, now, utc)!!
        val c = Calendar.getInstance(utc).apply { timeInMillis = published }
        assertEquals(listOf(2026, Calendar.SEPTEMBER, 23, 21, 55), listOf(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)))

        val december = cleaner.publishedAtInText("• December 30 at 8:00 AM", now, utc)!!
        assertEquals(2025, Calendar.getInstance(utc).apply { timeInMillis = december }.get(Calendar.YEAR))

        assertNull(cleaner.publishedAtInText("لا يوجد تاريخ هنا", now, utc))
    }
}
