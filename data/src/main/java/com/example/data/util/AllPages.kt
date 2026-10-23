package com.example.data.util

/** The page size of the admin's whole-collection reads: few round trips, each a few seconds on mobile data. */
internal const val ADMIN_PAGE_SIZE = 300

/** A runaway guard: far more pages than any collection has. */
private const val MAX_PAGES = 100

/**
 * Every document of a collection paged newest first by publish date: [fetch] gets the page
 * after a publish date (null: the first). It stops at an empty page, not a short one: a
 * document that fails to parse is dropped from its page, so a short page isn't the end.
 * Deleted documents are read too, so the pages advance past them.
 */
internal suspend fun <T> fetchAllPages(
    fetch: suspend (startAfterPublishDate: Long?) -> List<T>,
    publishDate: (T) -> Long?,
): List<T> {
    val all = mutableListOf<T>()
    var after: Long? = null
    repeat(MAX_PAGES) {
        val page = fetch(after)
        if (page.isEmpty()) return all
        all += page
        val next = publishDate(page.last()) ?: return all
        if (next == after) return all
        after = next
    }
    return all
}
