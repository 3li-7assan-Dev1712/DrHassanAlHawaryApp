package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareTitleParserTest {

    // The exact string from the broken export (problem #2 in share-video-redesign.md).
    private val khutbaTitle =
        "خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م"

    @Test
    fun `splits the combined khutba title into title, kind, day and both dates`() {
        val parsed = ShareTitleParser.parse(khutbaTitle)

        assertEquals("فضل العشر، والأضحية", parsed.title)
        assertEquals("خطبة", parsed.kind)
        assertEquals("الجمعة", parsed.dayName)
        assertEquals(HijriDate(27, "ذو القعدة", 1447), parsed.hijriDate)
        assertEquals(GregorianDate(2026, 5, 15), parsed.gregorianDate)
    }

    @Test
    fun `khutba on a friday gets the friday khutba chip`() {
        val fields = ShareTitleParser.toCardFields(khutbaTitle, categoryId = "khotab")

        assertEquals("فضل العشر، والأضحية", fields.title)
        assertEquals("خطبة الجمعة", fields.kindLabel)
    }

    @Test
    fun `bare kind with a date becomes the title and hides the chip`() {
        val fields = ShareTitleParser.toCardFields("محاضرة - 6 ربيع الآخر 1448هـ", categoryId = "lectures")

        assertEquals("محاضرة", fields.title)
        assertNull(fields.kindLabel)
        assertEquals(HijriDate(6, "ربيع الآخر", 1448), fields.hijriDate)
        assertNull(fields.gregorianDate)
    }

    @Test
    fun `plain title keeps its text and takes the chip from the category`() {
        val fields = ShareTitleParser.toCardFields("شرح كتاب الطهارة", categoryId = "scientific_lessons")

        assertEquals("شرح كتاب الطهارة", fields.title)
        assertEquals("درس علمي", fields.kindLabel)
        assertNull(fields.hijriDate)
        assertNull(fields.gregorianDate)
    }

    @Test
    fun `every known category id maps to a chip label`() {
        val plain = ShareTitleParser.parse("عنوان")
        assertEquals("خطبة الجمعة", ShareTitleParser.kindLabel(plain, "khotab"))
        assertEquals("درس علمي", ShareTitleParser.kindLabel(plain, "scientific_lessons"))
        assertEquals("محاضرة", ShareTitleParser.kindLabel(plain, "lectures"))
        assertEquals("فتوى", ShareTitleParser.kindLabel(plain, "fatawah"))
        assertEquals("تلاوة", ShareTitleParser.kindLabel(plain, "telawat"))
        assertNull(ShareTitleParser.kindLabel(plain, "audio"))
        assertNull(ShareTitleParser.kindLabel(plain, null))
    }

    @Test
    fun `eid khutba is labelled as such`() {
        val fields = ShareTitleParser.toCardFields("خطبة بعنوان: أحكام عيد الأضحى", categoryId = "khotab")

        assertEquals("أحكام عيد الأضحى", fields.title)
        assertEquals("خطبة العيد", fields.kindLabel)
    }

    @Test
    fun `a weekday that is part of the title is not stripped`() {
        val parsed = ShareTitleParser.parse("فضل يوم الجمعة")

        assertEquals("فضل يوم الجمعة", parsed.title)
        assertNull(parsed.dayName)
    }

    @Test
    fun `day-first gregorian dates and alternate month spellings parse`() {
        val parsed = ShareTitleParser.parse("درس بعنوان: التوحيد - 3 ذي الحجة 1446 هـ - 30/5/2025م")

        assertEquals("التوحيد", parsed.title)
        assertEquals("درس", parsed.kind)
        assertEquals(HijriDate(3, "ذو الحجة", 1446), parsed.hijriDate)
        assertEquals(GregorianDate(2025, 5, 30), parsed.gregorianDate)
    }

    @Test
    fun `wrapping and unbalanced parentheses are removed, matched inner pairs kept`() {
        assertEquals("الصبر", ShareTitleParser.parse("(الصبر)").title)
        assertEquals("الصبر", ShareTitleParser.parse("( الصبر").title)
        assertEquals("شرح الأربعين (١)", ShareTitleParser.parse("شرح الأربعين (١)").title)
    }

    @Test
    fun `tatweel is removed`() {
        assertEquals("الشيخ الهواري", ShareTitleParser.parse("الشيـــخ الهـــواري").title)
    }

    @Test
    fun `text after the date is kept, not dropped`() {
        val parsed = ShareTitleParser.parse("محاضرة - 6 ربيع الآخر 1448هـ الجزء الثاني")

        assertEquals("الجزء الثاني", parsed.title)
        assertEquals("محاضرة", parsed.kind)
    }

    @Test
    fun `blank input yields an empty title`() {
        assertEquals("", ShareTitleParser.parse("   ").title)
    }

    @Test
    fun `invalid gregorian numbers are not treated as a date`() {
        val parsed = ShareTitleParser.parse("سلسلة 2026/13/40")

        assertNull(parsed.gregorianDate)
        assertEquals("سلسلة 2026/13/40", parsed.title)
    }
}
