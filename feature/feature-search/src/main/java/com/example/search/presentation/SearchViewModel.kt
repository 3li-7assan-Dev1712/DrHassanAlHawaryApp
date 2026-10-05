package com.example.search.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.search.model.Attribute
import com.algolia.search.model.response.ResponseSearch
import com.example.domain.repository.DataStoreRepository
import com.example.domain.text.ArabicSearchText
import com.example.search.presentation.model.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter(val type: String, val label: String) {
    ALL("all", "الكل"),
    ARTICLE("article", "مقالات"),
    AUDIO("audio", "صوتيات"),
    VIDEO("video", "فيديوهات"),
    IMAGES("image_group", "صور")
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searcher: HitsSearcher,
    private val dataStoreRepository: DataStoreRepository,
) : ViewModel() {
    private val TAG = "SearchViewModel"

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * Hits per type ("article" -> 5) from the last unfiltered search of [currentQuery]; kept
     * while a type chip is selected so every chip shows its count. Empty = no counts.
     */
    private val _typeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val typeCounts = _typeCounts.asStateFlow()

    val recentSearches: StateFlow<List<String>> = dataStoreRepository.recentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var currentQuery: String = ""
        private set

    init {
        // Set default filter to not show deleted items
        searcher.query.facetFilters = listOf(listOf("isDeleted:false"))
        // Per-type counts for the chips ("مقالات ٥"). A query parameter only: "type" is
        // already a facet (it's filtered on), so no index setting changes.
        searcher.query.facets = setOf(TYPE)

        viewModelScope.launch {
            searcher.response.subscribe { res ->
                Log.d(TAG, "res value: ${res?.hits?.size}")
                res?.let(::onResponse)
            }
        }
        viewModelScope.launch {
            _query.debounce(DEBOUNCE_MS).distinctUntilChanged().collect { runSearch(it) }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) runSearch(newQuery)
    }

    /** Keyboard "search" or a tapped suggestion: search now and remember the query. */
    fun submit(newQuery: String = _query.value) {
        _query.value = newQuery
        runSearch(newQuery)
        rememberQuery(newQuery)
    }

    fun onFilterSelected(filter: SearchFilter) {
        _selectedFilter.value = filter
        applyFilter()
        if (currentQuery.isBlank() || !ArabicSearchText.isSearchable(currentQuery)) return
        _uiState.value = SearchUiState.Loading
        searcher.searchAsync()
        Log.d(TAG, "FacetFilters: ${searcher.query.facetFilters}")
    }

    /** Called when a result is opened: the query was useful, keep it in "recent". */
    fun onResultOpened() = rememberQuery(currentQuery)

    fun clearRecentSearches() {
        viewModelScope.launch { dataStoreRepository.setRecentSearches(emptyList()) }
    }

    private fun applyFilter() {
        val filter = _selectedFilter.value
        searcher.query.facetFilters =
            if (filter == SearchFilter.ALL) listOf(listOf("isDeleted:false"))
            else listOf(listOf("type:${filter.type}"), listOf("isDeleted:false"))
    }

    private fun runSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed != currentQuery.trim()) _typeCounts.value = emptyMap()
        currentQuery = query
        when {
            trimmed.isEmpty() -> _uiState.value = SearchUiState.Idle
            // After dropping a leading "ال", some word must have 2+ letters.
            !ArabicSearchText.isSearchable(trimmed) -> _uiState.value = SearchUiState.TooShort
            else -> {
                searcher.query.query = trimmed
                applyFilter()
                _uiState.value = SearchUiState.Loading
                searcher.searchAsync()
            }
        }
    }

    private fun onResponse(response: ResponseSearch) {
        // A late response for a query the user has already cleared or shortened.
        if (!ArabicSearchText.isSearchable(currentQuery)) return
        if (_selectedFilter.value == SearchFilter.ALL) {
            _typeCounts.value = response.facetsOrNull?.get(TYPE)
                ?.associate { it.value to it.count }
                .orEmpty()
        }
        _uiState.value = SearchUiState.Success(response)
    }

    private fun rememberQuery(query: String) {
        val trimmed = query.trim()
        if (!ArabicSearchText.isSearchable(trimmed)) return
        viewModelScope.launch {
            val current = dataStoreRepository.recentSearches().first()
            val updated = (listOf(trimmed) + current.filterNot { it == trimmed }).take(MAX_RECENT)
            dataStoreRepository.setRecentSearches(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
    }

    private companion object {
        val TYPE = Attribute("type")
        const val DEBOUNCE_MS = 300L
        const val MAX_RECENT = 8
    }
}
