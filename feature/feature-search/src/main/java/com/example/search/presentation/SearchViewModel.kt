package com.example.search.presentation


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    val searcher: HitsSearcher,
) : ViewModel() {

    val TAG = "SearchViewModel"
    // This is the state that your UI will observe. It's private to the ViewModel.
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)

    // This is the public, read-only version for the UI.
    val uiState = _uiState.asStateFlow()

    init {
        // This is the most important part.
        // We collect the searcher's response flow. Every time a new result
        // comes in, this block will be executed.
        viewModelScope.launch {
            searcher.response.subscribe { res ->
                // If the search was successful, update the UI state with the results
                res?.let { _uiState.value = SearchUiState.Success(it) }
                if (res == null) {
                    Log.d(TAG, "null: response")
                } else {
                    Log.d(TAG, "response: ${res.hits}")

                }
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        // When a new search starts, set the state to Loading
        _uiState.value = SearchUiState.Loading
        searcher.setQuery(query)

        // Trigger the search. The result will be collected by the `init` block.

        try {
            searcher.searchAsync()

        } catch (e: Exception) {
            // If the search fails, update the UI state to Error
            _uiState.value = SearchUiState.Error(e)
        }

    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
    }

}