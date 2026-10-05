package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PublishedPhraseTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val now = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.SEPTEMBER, 27, 12, 0) }.timeInMillis
    private fun minutesAgo(m: Long) = now - m * 60_000L
    private fun published(minutesAgo: Long) = ArabicDates.published(now, minutesAgo(minutesAgo), utc)

    @Test
    fun `minutes and hours follow arabic number agreement`() {
        assertEquals("نُشر الآن", published(0))
        assertEquals("نُشر منذ دقيقة", published(1))
        assertEquals("نُشر منذ دقيقتين", published(2))
        assertEquals("نُشر منذ ٣ دقائق", published(3))
        assertEquals("نُشر منذ ١٠ دقائق", published(10))
        assertEquals("نُشر منذ ١١ دقيقة", published(11))
        assertEquals("نُشر منذ ساعة", published(60))
        assertEquals("نُشر منذ ساعتين", published(120))
        assertEquals("نُشر منذ ٥ ساعات", published(5 * 60))
        assertEquals("نُشر منذ ١١ ساعة", published(11 * 60))
    }

    @Test
    fun `days and weeks`() {
        val day = 24 * 60L
        assertEquals("نُشر أمس", published(day + 30))
        assertEquals("نُشر منذ يومين", published(2 * day))
        assertEquals("نُشر منذ ٣ أيام", published(3 * day))
        assertEquals("نُشر منذ أسبوع", published(7 * day))
        assertEquals("نُشر منذ أسبوعين", published(14 * day))
        assertEquals("نُشر منذ ٣ أسابيع", published(21 * day))
        assertEquals("نُشر منذ ٤ أسابيع", published(30 * day))
    }

    @Test
    fun `older than 30 days is the calendar date with arabic month and digits`() {
        val april4 = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.APRIL, 4, 10, 0) }.timeInMillis
        assertEquals("نُشر في ٤ أبريل ٢٠٢٦", ArabicDates.published(now, april4, utc))
    }

    @Test
    fun `the phrase never doubles its words`() {
        val day = 24 * 60L
        listOf(0L, 5L, 90L, day * 3, day * 20, day * 200).forEach { m ->
            val phrase = published(m)
            assertFalse(phrase, phrase.contains("منذ منذ"))
            assertFalse(phrase, phrase.contains("منذ ") && phrase.contains(" في "))
            assertFalse(phrase, phrase.any { it in '0'..'9' })
        }
    }
}
