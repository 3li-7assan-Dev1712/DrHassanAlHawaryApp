package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class LessonTitleTest {

    private val bukhari = "شرح صحيح البخاري"

    private fun lesson(raw: String, series: String = bukhari) = AudioTitleCleaner.lesson(raw, series)

    @Test
    fun `the examples`() {
        assertEquals("ترجمة المصنف", lesson("شرح صحيح البخاري - 1 (ترجمة المصنف"))
        assertEquals("التعريف بصحيح البخاري", lesson("(٢) التعريف بصحيح البخاري"))
        assertEquals("تابع التعريف بالكتاب", lesson("شرح صحيح البخاري، تابع التعريف بالكتاب"))
        assertEquals(
            "باب كيف كان بدء الوحي - تابع شرح الحديث الأول - والحديث الثاني",
            lesson("(٥) باب كيف كان بدء الوحي - تابع شرح الحديث الأول - والحديث الثاني"),
        )
    }

    @Test
    fun `the series name goes with the separators after it`() {
        assertEquals("مقدمة", lesson("شرح صحيح البخاري - مقدمة"))
        assertEquals("مقدمة", lesson("شرح صحيح البخاري. مقدمة"))
        assertEquals("مقدمة", lesson("شرح  صحيح البخاري مقدمة"))
        // Only at the start, and only the whole name.
        assertEquals("مقدمة شرح صحيح البخاري", lesson("مقدمة شرح صحيح البخاري"))
        assertEquals("شرح الحجاب", lesson("شرح الحجاب", series = "شرح الحج"))
    }

    @Test
    fun `leading numbering goes`() {
        assertEquals("مقدمة", lesson("(2) مقدمة"))
        assertEquals("مقدمة", lesson("٢ - مقدمة"))
        assertEquals("مقدمة", lesson("- 1 مقدمة"))
        assertEquals("باب بدء الوحي", lesson("شرح صحيح البخاري (3) باب بدء الوحي"))
        assertEquals("باب بدء الوحي", lesson("(3) شرح صحيح البخاري - باب بدء الوحي"))
        // A number that is part of the title stays.
        assertEquals("٢٠٢٤م خطبة العيد", lesson("2024م خطبة العيد"))
    }

    @Test
    fun `only unbalanced brackets are dropped`() {
        assertEquals("باب (ما جاء في الوحي)", lesson("باب (ما جاء في الوحي)"))
        assertEquals("باب ما جاء في الوحي", lesson("باب ما جاء في الوحي)"))
        assertEquals("باب ما جاء [في الوحي]", lesson("(4) باب ما جاء [في الوحي]"))
    }

    @Test
    fun `western digits become arabic indic`() {
        assertEquals("باب ٣ من الإيمان", lesson("شرح صحيح البخاري - باب 3 من الإيمان"))
    }

    @Test
    fun `the service labels go too`() {
        assertEquals("التعريف بصحيح البخاري", lesson("مقطع بعنوان: ⏪(٢) التعريف بصحيح البخاري"))
    }

    @Test
    fun `nothing left falls back to the original`() {
        assertEquals("شرح صحيح البخاري", lesson("شرح صحيح البخاري"))
        assertEquals("(١)", lesson("(١)"))
        assertEquals("شرح صحيح البخاري - 1", lesson("شرح صحيح البخاري - 1"))
    }

    @Test
    fun `no series name only drops the numbering`() {
        assertEquals("مقدمة", lesson("(1) مقدمة", series = "  "))
    }
}
