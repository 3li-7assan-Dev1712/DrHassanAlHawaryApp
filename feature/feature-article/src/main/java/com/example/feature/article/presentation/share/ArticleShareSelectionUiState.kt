package com.example.feature.article.presentation.share

data class ArticleShareSelectionUiState(
    val isLoading: Boolean = true,
    val articleId: String = "",
    val articleTitle: String = "",
    /** The full article body, paragraphs joined for a single continuous [SelectableQuoteText]. */
    val displayText: String = "",
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val errorMessage: String? = null,
) {
    val selectedText: String
        get() = if (selectionEnd > selectionStart) displayText.substring(selectionStart, selectionEnd).trim() else ""

    val selectionLength: Int get() = selectedText.length

    val hasSelection: Boolean get() = selectionLength > 0

    /** Any length can be shared: long excerpts become several ordered images. */
    val canContinue: Boolean get() = hasSelection
}
