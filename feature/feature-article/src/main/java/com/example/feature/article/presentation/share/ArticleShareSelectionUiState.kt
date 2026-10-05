package com.example.feature.article.presentation.share

import com.example.domain.text.SelectableArticle
import com.example.feature.share.engine.QuotePage

data class ArticleShareSelectionUiState(
    val isLoading: Boolean = true,
    val articleId: String = "",
    /** The cleaned article title, shown as a non-selectable header. */
    val articleTitle: String = "",
    /** The article as the reader shows it, as one selectable text. */
    val article: SelectableArticle? = null,
    /** The selection in [SelectableArticle.text] (end exclusive); empty when end <= start. */
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
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
    /** The selection in [SelectableArticle.displayText] - what the next step takes - or null. */
    val selectionRange: IntRange?
        get() = if (selectionEnd > selectionStart) article?.toSource(selectionStart, selectionEnd) else null

    val hasSelection: Boolean get() = selectionRange != null

    val isTooLong: Boolean get() = maxPages != null && pageCount > maxPages

    val canContinue: Boolean get() = hasSelection && pageCount > 0 && !isTooLong
}
