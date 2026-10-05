package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArabicNumeralsTest {

    @Test
    fun `western and persian digits become arabic-indic, text untouched`() {
        assertEquals("٠١٢٣٤٥٦٧٨٩", ArabicNumerals.digits("0123456789"))
        assertEquals("٤٥", ArabicNumerals.digits("۴۵"))
        assertEquals("شرح الأربعين (٣)", ArabicNumerals.digits("شرح الأربعين (3)"))
        assertEquals("٢٠٢٦", ArabicNumerals.digits(2026))
    }

    @Test
    fun `toWestern is the inverse of digits`() {
        assertEquals("27 ذو القعدة 1447", ArabicNumerals.toWestern("٢٧ ذو القعدة ١٤٤٧"))
    }

    @Test
    fun `hijri and gregorian dates format with arabic month names and digits`() {
        assertEquals("٢٧ ذو القعدة ١٤٤٧هـ", ArabicNumerals.formatHijri(HijriDate(27, "ذو القعدة", 1447)))
        assertEquals("١٥ مايو ٢٠٢٦م", ArabicNumerals.formatGregorian(GregorianDate(2026, 5, 15)))
        assertEquals("١ يناير ٢٠٢٥م", ArabicNumerals.formatGregorian(GregorianDate(2025, 1, 1)))
        assertEquals("٣١ ديسمبر ٢٠٢٥م", ArabicNumerals.formatGregorian(GregorianDate(2025, 12, 31)))
    }

    @Test
    fun `the exact broken title from problem 2 produces the design's date line`() {
        val parsed = ShareTitleParser.parse(
            "خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م"
        )

        assertEquals(
            "٢٧ ذو القعدة ١٤٤٧هـ · ١٥ مايو ٢٠٢٦م",
            ArabicNumerals.formatDateLine(parsed.hijriDate, parsed.gregorianDate),
        )
    }

    @Test
    fun `date line with one half or none`() {
        assertEquals("٦ ربيع الآخر ١٤٤٨هـ", ArabicNumerals.formatDateLine(HijriDate(6, "ربيع الآخر", 1448), null))
        assertEquals("١٥ مايو ٢٠٢٦م", ArabicNumerals.formatDateLine(null, GregorianDate(2026, 5, 15)))
        assertNull(ArabicNumerals.formatDateLine(null, null))
    }

    @Test
    fun `durations are zero-padded mm-ss, with hours when needed`() {
        assertEquals("٠٠:٠٠", ArabicNumerals.formatDuration(0))
        assertEquals("٠٠:٠٠", ArabicNumerals.formatDuration(-500))
        assertEquals("٠٠:٣٠", ArabicNumerals.formatDuration(30_000))
        assertEquals("٠١:٠٠", ArabicNumerals.formatDuration(60_999))
        assertEquals("٣٢:١٥", ArabicNumerals.formatDuration(32 * 60_000L + 15_000L))
        assertEquals("١:٠٢:٠٥", ArabicNumerals.formatDuration(3_725_000))
    }

    @Test
    fun `media times have no leading zero on minutes`() {
        org.junit.Assert.assertEquals("٠:٢٠", ArabicNumerals.formatMediaTime(20_000))
        org.junit.Assert.assertEquals("٣:٠٩", ArabicNumerals.formatMediaTime(189_000))
        org.junit.Assert.assertEquals("٢٢:٠٠", ArabicNumerals.formatMediaTime(1_320_000))
        org.junit.Assert.assertEquals("١:٠٥:٣٠", ArabicNumerals.formatMediaTime(3_930_000))
        org.junit.Assert.assertEquals("٠:٠٠", ArabicNumerals.formatMediaTime(-5))
    }
}
