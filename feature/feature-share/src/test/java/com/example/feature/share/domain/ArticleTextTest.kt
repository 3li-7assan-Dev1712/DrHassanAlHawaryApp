package com.example.feature.share.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleTextTest {

    @Test
    fun `the title from the broken image loses the author and gets a colon`() {
        assertEquals(
            "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
            ArticleText.cleanTitle("الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير - الشيخ د.حسن الهواري"),
        )
    }

    @Test
    fun `author suffix variants are all stripped`() {
        val expected = "فقه الصيام"
        listOf(
            "فقه الصيام - الشيخ د.حسن الهواري",
            "فقه الصيام - الشيخ د. حسن أحمد الهواري",
            "فقه الصيام - الشيخ د . حسن احمد الهوارى",
            "فقه الصيام - د.حسن الهواري",
            "فقه الصيام - حسن أحمد الهواري",
            "فقه الصيام - الشيـــخ د.حسن الهـــواري",
            "فقه الصيام - الشيخ د.حسن ...",
            "فقه الصيام | الشيخ حسن الهواري",
        ).forEach { assertEquals(it, expected, ArticleText.cleanTitle(it)) }
    }

    @Test
    fun `facebook timestamps are removed`() {
        assertEquals("فقه الصيام", ArticleText.cleanTitle("فقه الصيام September 23 at 9:55 PM"))
        assertEquals("فقه الصيام", ArticleText.cleanTitle("Yesterday at 10:02 AM فقه الصيام"))
        assertEquals("فقه الصيام", ArticleText.cleanTitle("فقه الصيام - Sep 3, 2025 at 7:05 am"))
    }

    @Test
    fun `only the first dash becomes a colon and plain titles are untouched`() {
        assertEquals("أ: ب - ج", ArticleText.cleanTitle("أ - ب - ج"))
        assertEquals("مسائل في الطهارة", ArticleText.cleanTitle("مسائل في الطهارة"))
        assertEquals("الإيمان-والإسلام", ArticleText.cleanTitle("الإيمان-والإسلام"))
    }

    @Test
    fun `display text drops blank lines and joins paragraphs with one empty line`() {
        assertEquals("أ\n\nب", ArticleText.displayText("  أ  \n\n\n   \nب"))
    }
}
