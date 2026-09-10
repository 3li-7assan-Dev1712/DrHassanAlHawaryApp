package com.example.feature.article.presentation.share

data class ArticleShareSelectionUiState(
    val isLoading: Boolean = true,
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

    val isTooLong: Boolean get() = selectionLength > MAX_EXCERPT_LENGTH

    val hasSelection: Boolean get() = selectionLength > 0

    val canContinue: Boolean get() = hasSelection && !isTooLong

    companion object {
        const val MAX_EXCERPT_LENGTH = 450
    }
}
