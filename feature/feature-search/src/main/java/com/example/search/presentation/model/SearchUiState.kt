package com.example.search.presentation.model

import com.algolia.search.model.response.ResponseSearch

/**
 * Represents the different states the search UI can be in.
 * This allows the UI to react declaratively to changes in data or errors.
 */
sealed interface SearchUiState {

    /**
     * The initial state before any search has been performed.
     * The UI can show a welcome message or prompt.
     */
    data object Idle : SearchUiState

    /**
     * The state when a search has been triggered but the results have not yet returned.
     * The UI should show a loading indicator.
     */
    data object Loading : SearchUiState

    /**
     * The state when a search has successfully completed.
     * It holds the search response from Algolia.
     * @param results The complete search response object, containing hits, facets, etc.
     */
    data class Success(val results: ResponseSearch) : SearchUiState

    /**
     * The state when an error has occurred during the search.
     * It holds the exception that was thrown.
     * @param throwable The error that occurred, which can be used for logging or display.
     */
    data class Error(val throwable: Throwable) : SearchUiState
}