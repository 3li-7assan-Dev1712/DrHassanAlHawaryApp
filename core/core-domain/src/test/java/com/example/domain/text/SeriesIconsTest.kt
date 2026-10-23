package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class SeriesIconsTest {

    @Test
    fun `each rule picks its icon`() {
        assertEquals(SeriesIcon.Kaaba, SeriesIcons.forTitle("شرح مناسك الحج"))
        assertEquals(SeriesIcon.MoonStars, SeriesIcons.forTitle("فقه الصيام"))
        assertEquals(SeriesIcon.Rings, SeriesIcons.forTitle("شرح كتاب النكاح"))
        assertEquals(SeriesIcon.HomeHeart, SeriesIcons.forTitle("فقه الحياة الزوجية"))
        assertEquals(SeriesIcon.Hourglass, SeriesIcons.forTitle("فقه الجنائز"))
        assertEquals(SeriesIcon.Scale, SeriesIcons.forTitle("شرح منظومة القواعد الفقهية للسعدي"))
        assertEquals(SeriesIcon.Feather, SeriesIcons.forTitle("شرح ألفية السيوطي"))
        assertEquals(SeriesIcon.Quote, SeriesIcons.forTitle("شرح حديث جبريل الطويل"))
        assertEquals(SeriesIcon.Book2, SeriesIcons.forTitle("شرح صحيح البخاري"))
    }

    @Test
    fun `the first matching rule wins`() {
        // "كتاب" (book) comes after "النكاح" (rings); "منظومة" (feather) after "القواعد الفقهية" (scale).
        assertEquals(SeriesIcon.Rings, SeriesIcons.forTitle("شرح كتاب النكاح"))
        assertEquals(SeriesIcon.Scale, SeriesIcons.forTitle("شرح منظومة القواعد الفقهية للسعدي"))
        assertEquals(SeriesIcon.Kaaba, SeriesIcons.forTitle("كتاب الحج من صحيح مسلم"))
    }

    @Test
    fun `every keyword of a rule counts`() {
        assertEquals(SeriesIcon.Kaaba, SeriesIcons.forTitle("أحكام العمرة"))
        assertEquals(SeriesIcon.Kaaba, SeriesIcons.forTitle("المناسك الميسرة"))
        assertEquals(SeriesIcon.MoonStars, SeriesIcons.forTitle("دروس رمضان"))
        assertEquals(SeriesIcon.MoonStars, SeriesIcons.forTitle("أحكام الاعتكاف"))
        assertEquals(SeriesIcon.Rings, SeriesIcons.forTitle("أحكام الزواج"))
        assertEquals(SeriesIcon.Rings, SeriesIcons.forTitle("أحكام الطلاق"))
        assertEquals(SeriesIcon.HomeHeart, SeriesIcons.forTitle("فقه الأسرة"))
        assertEquals(SeriesIcon.Scale, SeriesIcons.forTitle("مقدمة في أصول الفقه"))
        assertEquals(SeriesIcon.Feather, SeriesIcons.forTitle("شرح نظم الورقات"))
        assertEquals(SeriesIcon.Book2, SeriesIcons.forTitle("شرح سنن أبي داود"))
        assertEquals(SeriesIcon.Book2, SeriesIcons.forTitle("شرح متن أبي شجاع"))
    }

    @Test
    fun `spelling variants match`() {
        assertEquals(SeriesIcon.Feather, SeriesIcons.forTitle("شرح الفية ابن مالك"))
        assertEquals(SeriesIcon.MoonStars, SeriesIcons.forTitle("فقه الصِّيام"))
        assertEquals(SeriesIcon.Scale, SeriesIcons.forTitle("القواعد  الفقهية"))
    }

    @Test
    fun `anything else is a stack`() {
        assertEquals(SeriesIcon.Stack2, SeriesIcons.forTitle("مجالس التزكية"))
        assertEquals(SeriesIcon.Stack2, SeriesIcons.forTitle(""))
    }
}
