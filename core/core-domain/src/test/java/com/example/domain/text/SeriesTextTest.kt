package com.example.domain.text

import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeriesTextTest {

    private val hour = 3_600_000L
    private val minute = 60_000L

    @Test
    fun `item counts follow arabic number agreement`() {
        val audios = SeriesCollection.AUDIOS
        assertEquals("درس واحد", SeriesText.itemCount(1, audios))
        assertEquals("درسان", SeriesText.itemCount(2, audios))
        assertEquals("٣ دروس", SeriesText.itemCount(3, audios))
        assertEquals("٢٨ درسًا", SeriesText.itemCount(28, audios))
        assertEquals("١٠٠ درس", SeriesText.itemCount(100, audios))
        assertEquals("مقالان", SeriesText.itemCount(2, SeriesCollection.ARTICLES))
        assertEquals("١١ مقالًا", SeriesText.itemCount(11, SeriesCollection.ARTICLES))
        assertEquals("٥٢ تصميمًا", SeriesText.itemCount(52, SeriesCollection.IMAGE_GROUPS))
        assertEquals("٧ تصاميم", SeriesText.itemCount(7, SeriesCollection.IMAGE_GROUPS))
    }

    @Test
    fun `duration rounds to the minute under an hour, else to the hour`() {
        assertNull(SeriesText.duration(0))
        assertEquals("دقيقة واحدة", SeriesText.duration(10_000))
        assertEquals("دقيقتان", SeriesText.duration(2 * minute))
        assertEquals("٤٥ دقيقة", SeriesText.duration(45 * minute))
        assertEquals("ساعة واحدة", SeriesText.duration(59 * minute + 50_000))
        assertEquals("ساعتان", SeriesText.duration(hour + 31 * minute))
        assertEquals("٥ ساعات", SeriesText.duration(5 * hour))
        assertEquals("٢٨ ساعة", SeriesText.duration(28 * hour + 20 * minute))
    }

    @Test
    fun `meta joins the count and the duration`() {
        val bukhari = Series(
            id = "bukhari",
            title = "شرح صحيح البخاري",
            collection = SeriesCollection.AUDIOS,
            itemCount = 59,
            totalDurationMs = 28 * hour,
        )
        assertEquals("٥٩ درسًا · ٢٨ ساعة", SeriesText.meta(bukhari))
        val designs = Series(id = "ramadan", title = "رمضانيات", collection = SeriesCollection.IMAGE_GROUPS, itemCount = 52)
        assertEquals("٥٢ تصميمًا", SeriesText.meta(designs))
    }

    @Test
    fun `episode numbers use arabic-indic digits`() {
        assertEquals("١", SeriesText.episodeNumber(1))
        assertEquals("٢٨", SeriesText.episodeNumber(28))
    }
}
