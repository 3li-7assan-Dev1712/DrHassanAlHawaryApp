package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicDatesTest {

    private val now = 1_800_000_000_000L
    private fun ago(minutes: Long) = now - minutes * 60_000L

    @Test
    fun `relative dates follow arabic number agreement`() {
        assertEquals("الآن", ArabicDates.relative(now, ago(0)))
        assertEquals("الآن", ArabicDates.relative(now, now + 60_000)) // clock skew
        assertEquals("منذ دقيقة", ArabicDates.relative(now, ago(1)))
        assertEquals("منذ دقيقتين", ArabicDates.relative(now, ago(2)))
        assertEquals("منذ ٣ دقائق", ArabicDates.relative(now, ago(3)))
        assertEquals("منذ ١١ دقيقة", ArabicDates.relative(now, ago(11)))
        assertEquals("منذ ساعة", ArabicDates.relative(now, ago(60)))
        assertEquals("منذ ساعتين", ArabicDates.relative(now, ago(120)))
        assertEquals("منذ ٥ ساعات", ArabicDates.relative(now, ago(300)))
        assertEquals("أمس", ArabicDates.relative(now, ago(26 * 60)))
        assertEquals("منذ يومين", ArabicDates.relative(now, ago(2 * 24 * 60)))
        assertEquals("منذ ٣ أيام", ArabicDates.relative(now, ago(3 * 24 * 60)))
        assertEquals("منذ أسبوع", ArabicDates.relative(now, ago(8 * 24 * 60)))
        assertEquals("منذ أسبوعين", ArabicDates.relative(now, ago(15 * 24 * 60)))
        assertEquals("منذ شهر", ArabicDates.relative(now, ago(31 * 24 * 60)))
        assertEquals("منذ ٣ أشهر", ArabicDates.relative(now, ago(95 * 24 * 60)))
        assertEquals("منذ ١١ شهرًا", ArabicDates.relative(now, ago(335 * 24 * 60)))
        assertEquals("منذ سنة", ArabicDates.relative(now, ago(400 * 24 * 60)))
    }

    @Test
    fun `reading time`() {
        assertEquals("قراءة دقيقة", ArabicDates.readingTime(1))
        assertEquals("قراءة دقيقتين", ArabicDates.readingTime(2))
        assertEquals("قراءة ٧ دقائق", ArabicDates.readingTime(7))
        assertEquals("قراءة ١٥ دقيقة", ArabicDates.readingTime(15))
        assertEquals("قراءة دقيقة", ArabicDates.readingTime(0))
    }
}
