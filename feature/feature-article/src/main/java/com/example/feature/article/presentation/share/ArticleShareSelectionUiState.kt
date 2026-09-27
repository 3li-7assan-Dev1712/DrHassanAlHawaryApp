package com.example.feature.article.presentation.share

import com.example.domain.text.QuoteDocument
import com.example.domain.text.SentenceSelection
import com.example.feature.share.engine.QuotePage

data class ArticleShareSelectionUiState(
    val isLoading: Boolean = true,
    val articleId: String = "",
    /** The cleaned article title, shown as a non-selectable header. */
    val articleTitle: String = "",
    /** The article as the reader shows it, split into tappable sentences. */
    val document: QuoteDocument? = null,
    val selection: SentenceSelection? = null,
    /** Characters of the cleaned excerpt that goes on the images (after the sanitizer). */
    val characterCount: Int = 0,
    /** Images the quote-image step will make, from its own pagination. */
    val pageCount: Int = 0,
    /** The first image, for the live thumbnail; null while nothing is selected. */
    val firstPage: QuotePage? = null,
    /** The quote-image step's page limit; null = no limit (it currently has none). */
    val maxPages: Int? = null,
    val errorMessage: String? = null,
) {
    val hasSelection: Boolean get() = selection != null

    val isTooLong: Boolean get() = maxPages != null && pageCount > maxPages

    val canContinue: Boolean get() = hasSelection && pageCount > 0 && !isTooLong

    /** The selection in [QuoteDocument.displayText] coordinates (end exclusive), or null. */
    val selectionRange: IntRange?
        get() = selection?.let { document?.range(it.first, it.last) }
}
