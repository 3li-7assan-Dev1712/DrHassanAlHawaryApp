package com.example.feature.home.domain.model

/**
 * An article as the home screen shows it - already cleaned for display by the
 * repository (ArticleTextCleaner); the stored article is never modified.
 */
data class ArticleFeed(
    val id: String,
    /** Title only: no author suffix or Facebook timestamp. */
    val title: String,
    /** First meaningful paragraph; empty when the article has none. */
    val excerpt: String,
    /** Epoch millis; null when neither the record nor the text has a date. */
    val publishedAt: Long? = null,
    val readingMinutes: Int = 1,
)
