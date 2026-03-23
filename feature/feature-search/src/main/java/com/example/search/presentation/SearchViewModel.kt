package com.example.search.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.example.search.presentation.model.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter(val type: String, val label: String) {
    ALL("all", "الكل"),
    ARTICLE("article", "مقالات"),
    AUDIO("audio", "صوتيات"),
    VIDEO("video", "فيديوهات"),
    IMAGES("image_group", "صور")
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searcher: HitsSearcher,
) : ViewModel() {
    private val TAG = "SearchViewModel"


    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private var currentQuery: String = ""

    init {
        viewModelScope.launch {

            searcher.response.subscribe { res ->
                Log.d(TAG, "res value: ${res?.hits?.size}")
                res?.let { _uiState.value = SearchUiState.Success(it) }
            }
        }
    }

    fun onFilterSelected(filter: SearchFilter) {
        _selectedFilter.value = filter

        searcher.query.facetFilters =
            if (filter == SearchFilter.ALL) null
            else listOf(listOf("type:${filter.type}"))

        _uiState.value = SearchUiState.Loading
        searcher.searchAsync()

        Log.d(TAG, "FacetFilters: ${searcher.query.facetFilters}")
    }

    fun search(query: String) {
        currentQuery = query

        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        _uiState.value = SearchUiState.Loading

        searcher.query.query = query
        searcher.searchAsync()

        Log.d("SearchVM", "Query: ${searcher.query.query}")
        Log.d("SearchVM", "Filters: ${searcher.query.filters}")
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
    }
}