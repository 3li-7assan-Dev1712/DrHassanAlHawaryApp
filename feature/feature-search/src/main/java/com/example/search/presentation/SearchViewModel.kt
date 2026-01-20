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

@HiltViewModel
class SearchViewModel @Inject constructor(
    val searcher: HitsSearcher,
) : ViewModel() {

    val TAG = "SearchViewModel"

    // This is the state that your UI will observe. It's private to the ViewModel.
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)

    val uiState = _uiState.asStateFlow()

    init {
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

        _uiState.value = SearchUiState.Loading
        searcher.setQuery(query)


        try {
            searcher.searchAsync()

        } catch (e: Exception) {
            Log.d(TAG, "search: error: ${e.message}")
            _uiState.value = SearchUiState.Error(e)
        }

    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
    }

}