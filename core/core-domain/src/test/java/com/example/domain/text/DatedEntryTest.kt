package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DatedEntryTest {

    @Test
    fun `qualification splits title, institution and year`() {
        val entry = DatedEntry.parse("بكالوريوس الشريعة - كلية الشريعة الجامعة الإسلامية بالمدينة المنورة (1990م)")
        assertEquals("بكالوريوس الشريعة", entry.title)
        assertEquals("كلية الشريعة الجامعة الإسلامية بالمدينة المنورة", entry.detail)
        assertEquals("١٩٩٠م", entry.dateLabel)
        assertEquals(1990, entry.year)
        assertFalse(entry.unpublished)
    }

    @Test
    fun `en dash, line breaks and a month in the date`() {
        val entry = DatedEntry.parse("دكتوراه في الشريعة – من كلية الشريعة\n(أحكام الأدوية في الشريعة الإسلامية) (2002م)")
        assertEquals("دكتوراه في الشريعة", entry.title)
        assertEquals("من كلية الشريعة (أحكام الأدوية في الشريعة الإسلامية)", entry.detail)
        assertEquals(2002, entry.year)

        val research = DatedEntry.parse("مصرف الفتوى – جامعة القرآن الكريم والعلوم الإسلامية (نوفمبر 2009م)")
        assertEquals("نوفمبر ٢٠٠٩م", research.dateLabel)
        assertEquals(2009, research.year)
    }

    @Test
    fun `hijri date keeps its label and sorts by the approximate gregorian year`() {
        val entry = DatedEntry.parse("بكالوريوس الشريعة - كلية الشريعة، الجامعة الإسلامية بالمدينة المنورة (1411هـ)")
        assertEquals("بكالوريوس الشريعة", entry.title)
        assertEquals("كلية الشريعة، الجامعة الإسلامية بالمدينة المنورة", entry.detail)
        assertEquals("١٤١١هـ", entry.dateLabel)
        assertEquals(1990, entry.year)
        assertEquals(2004, DatedEntry.parse("أحكام الأدوية – رسالة الدكتوراه (1425هـ)").year)
    }

    @Test
    fun `two years in one date sort by the later one`() {
        val entry = DatedEntry.parse("الحجاب – رسالة صغيرة (2010م و2014م)")
        assertEquals("٢٠١٠م و٢٠١٤م", entry.dateLabel)
        assertEquals(2014, entry.year)
    }

    @Test
    fun `unpublished research is flagged, no date gives nulls`() {
        assertTrue(DatedEntry.parse("بحث بعنوان: أثر القواعد الفقهية – حكم ولم ينشر بعد (2020م)").unpublished)
        val plain = DatedEntry.parse("ملتقيات علمية")
        assertNull(plain.dateLabel)
        assertNull(plain.year)
        assertNull(plain.detail)
    }
}
