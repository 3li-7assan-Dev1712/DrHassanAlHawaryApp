package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class SeriesHashtagTest {

    @Test
    fun `spaces become underscores behind a hash`() {
        assertEquals("#شرح_صحيح_البخاري", SeriesHashtag.fromTitle("شرح صحيح البخاري"))
        assertEquals("#رمضانيات", SeriesHashtag.fromTitle("  رمضانيات  "))
    }

    @Test
    fun `punctuation is dropped, digits and diacritics are kept`() {
        assertEquals("#شرح_مناسك_الحج", SeriesHashtag.fromTitle("شرح مناسك الحج!"))
        assertEquals("#فقه_الزكاة_مسائل", SeriesHashtag.fromTitle("فقه الزكاة: مسائل،"))
        assertEquals("#دروس_رمضان_١٤٤٧", SeriesHashtag.fromTitle("دروس رمضان (١٤٤٧)"))
        assertEquals("#الأربعون_النووية", SeriesHashtag.fromTitle("«الأربعون» النووية"))
        assertEquals("#شَرْح", SeriesHashtag.fromTitle("شَرْح"))
        assertEquals("#Q_A_2026", SeriesHashtag.fromTitle("Q & A - 2026"))
    }

    @Test
    fun `nothing to tag gives an empty hashtag`() {
        assertEquals("", SeriesHashtag.fromTitle(""))
        assertEquals("", SeriesHashtag.fromTitle(" - ، ؟ "))
    }
}
