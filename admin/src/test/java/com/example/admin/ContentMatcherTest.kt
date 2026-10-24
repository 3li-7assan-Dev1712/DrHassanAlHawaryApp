package com.example.admin

import app.netlify.devalihassan.admin.ui.content.AdminContentItem
import app.netlify.devalihassan.admin.ui.content.CategoryFilter
import app.netlify.devalihassan.admin.ui.content.ContentFilter
import app.netlify.devalihassan.admin.ui.content.ContentMatcher
import app.netlify.devalihassan.admin.ui.content.SeriesFilter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ContentMatcherTest {

    private fun item(id: String, title: String = id, day: Long = 0, category: String? = null, series: String? = null, episode: Long? = null) =
        AdminContentItem(id, title, Date(day * 86_400_000L), category, series, episode)

    private val matcher = ContentMatcher(knownCategoryIds = setOf("fatawah", "khotab"), liveSeriesIds = setOf("hajj"))

    private fun ids(items: List<AdminContentItem>, filter: ContentFilter) = matcher.apply(items, filter).map { it.id }

    @Test
    fun `no category means none, blank or a deleted one`() {
        val items = listOf(item("a", category = "fatawah"), item("b"), item("c", category = "old_deleted"))
        assertEquals(listOf("b", "c"), ids(items, ContentFilter(category = CategoryFilter.None)).sorted())
        assertEquals(listOf("a"), ids(items, ContentFilter(category = CategoryFilter.Of("fatawah"))))
    }

    @Test
    fun `series filters - standalone, missing series, one series in episode order`() {
        val items = listOf(
            item("s3", day = 1, series = "hajj", episode = 3),
            item("s1", day = 9, series = "hajj", episode = 1),
            item("lone", day = 5),
            item("orphan", day = 2, series = "deleted_series", episode = 1),
        )
        assertEquals(listOf("lone"), ids(items, ContentFilter(series = SeriesFilter.None)))
        assertEquals(listOf("orphan"), ids(items, ContentFilter(series = SeriesFilter.Missing)))
        assertEquals(listOf("s1", "s3"), ids(items, ContentFilter(series = SeriesFilter.Of("hajj"))))
        // Without a series filter: the newest first.
        assertEquals(listOf("s1", "lone", "orphan", "s3"), ids(items, ContentFilter()))
    }

    @Test
    fun `search unifies Arabic spelling and needs every word`() {
        val items = listOf(item("1", "أحكام الصيام في رمضان"), item("2", "خطبة عيد الفطر"), item("3", "الصلاة"))
        assertEquals(listOf("1"), ids(items, ContentFilter(query = "احكام الصيام")))
        assertEquals(listOf("2"), ids(items, ContentFilter(query = "الفطر")))
        assertEquals(emptyList<String>(), ids(items, ContentFilter(query = "احكام الفطر")))
    }

    @Test
    fun `route arguments open the list filtered`() {
        assertEquals(ContentFilter(), ContentFilter.fromArgs(null, null))
        assertEquals(CategoryFilter.None, ContentFilter.fromArgs(ContentFilter.NONE_ARG, null).category)
        assertEquals(SeriesFilter.Of("hajj"), ContentFilter.fromArgs(null, "hajj").series)
    }
}
