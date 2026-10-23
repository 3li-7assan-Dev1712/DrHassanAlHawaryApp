package app.netlify.devalihassan.admin.ui.content

import com.example.domain.module.Article
import com.example.domain.module.Audio
import com.example.domain.module.ImageGroup
import com.example.domain.module.Video
import com.example.domain.text.ArabicSearchText
import java.util.Date

/** What the admin lists need of an audio, video, article or design group. */
data class AdminContentItem(
    val id: String,
    val title: String,
    val publishDate: Date,
    /** Audios and videos only. */
    val categoryId: String? = null,
    val seriesId: String? = null,
    val episodeNumber: Long? = null,
)

fun Audio.toAdminItem() = AdminContentItem(id, title, publishDate, categoryId?.takeIf { it.isNotBlank() }, seriesId, episodeNumber)
fun Video.toAdminItem() = AdminContentItem(id, title, publishDate, categoryId?.takeIf { it.isNotBlank() }, seriesId, episodeNumber)
fun Article.toAdminItem() = AdminContentItem(id, title, publishDate, seriesId = seriesId, episodeNumber = episodeNumber)
fun ImageGroup.toAdminItem() = AdminContentItem(id, title, publishDate, seriesId = seriesId, episodeNumber = episodeNumber)

/** The category part of a list's filter. */
sealed interface CategoryFilter {
    data object All : CategoryFilter
    /** No category, or one that no longer exists: the app shows these only under «الكل». */
    data object None : CategoryFilter
    data class Of(val categoryId: String) : CategoryFilter
}

/** The series part of a list's filter. */
sealed interface SeriesFilter {
    data object All : SeriesFilter
    /** Standalone: the app's «متفرقات». */
    data object None : SeriesFilter
    /** In a series that was deleted: the app shows these nowhere. */
    data object Missing : SeriesFilter
    data class Of(val seriesId: String) : SeriesFilter
}

data class ContentFilter(
    val query: String = "",
    val category: CategoryFilter = CategoryFilter.All,
    val series: SeriesFilter = SeriesFilter.All,
) {
    companion object {
        /** A list route's argument for [CategoryFilter.None] / [SeriesFilter.None]. */
        const val NONE_ARG = "__none__"

        /** The filter a list route opens with (its categoryId / seriesId arguments). */
        fun fromArgs(categoryId: String?, seriesId: String?) = ContentFilter(
            category = when (categoryId) {
                null, "" -> CategoryFilter.All
                NONE_ARG -> CategoryFilter.None
                else -> CategoryFilter.Of(categoryId)
            },
            series = when (seriesId) {
                null, "" -> SeriesFilter.All
                NONE_ARG -> SeriesFilter.None
                else -> SeriesFilter.Of(seriesId)
            },
        )
    }
}

/**
 * Matching and ordering for the admin lists. [knownCategoryIds]: the current categories, so
 * an item of a deleted category counts as having none; [liveSeriesIds]: the series that exist.
 */
class ContentMatcher(
    private val knownCategoryIds: Set<String>,
    private val liveSeriesIds: Set<String>,
) {
    fun hasCategory(item: AdminContentItem): Boolean = item.categoryId != null && item.categoryId in knownCategoryIds

    fun matches(item: AdminContentItem, filter: CategoryFilter): Boolean = when (filter) {
        CategoryFilter.All -> true
        CategoryFilter.None -> !hasCategory(item)
        is CategoryFilter.Of -> item.categoryId == filter.categoryId
    }

    fun matches(item: AdminContentItem, filter: SeriesFilter): Boolean = when (filter) {
        SeriesFilter.All -> true
        SeriesFilter.None -> item.seriesId == null
        SeriesFilter.Missing -> item.seriesId != null && item.seriesId !in liveSeriesIds
        is SeriesFilter.Of -> item.seriesId == filter.seriesId
    }

    /** The items [filter] shows: a series' in episode order, otherwise the newest first. */
    fun apply(items: List<AdminContentItem>, filter: ContentFilter): List<AdminContentItem> {
        val matcher = titleMatcher(filter.query)
        val shown = items.filter { matches(it, filter.category) && matches(it, filter.series) && matcher(it.title) }
        return if (filter.series is SeriesFilter.Of) {
            shown.sortedWith(compareBy<AdminContentItem>({ it.episodeNumber ?: Long.MAX_VALUE }, { it.publishDate }))
        } else {
            shown.sortedByDescending { it.publishDate }
        }
    }

    companion object {
        /**
         * Every word of [query] must start a word of the title, with Arabic spelling unified
         * (ArabicSearchText: hamza forms, ة/ه, ى/ي, diacritics, a leading "ال"). A query too
         * short for that matches as plain text.
         */
        fun titleMatcher(query: String): (String) -> Boolean {
            if (query.isBlank()) return { true }
            val terms = ArabicSearchText.queryTerms(query)
            if (terms.isEmpty()) {
                val plain = ArabicSearchText.normalize(query.trim())
                return { title -> ArabicSearchText.normalize(title).contains(plain) }
            }
            return { title ->
                val words = title.split(' ', '\n', '\t', '-', '–', '—', '،', ',', ':', '(', ')').filter { it.isNotBlank() }
                terms.all { term -> words.any { ArabicSearchText.matches(it, listOf(term)) } }
            }
        }
    }
}
