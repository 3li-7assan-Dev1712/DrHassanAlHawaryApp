package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.model.getFakeArticles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class ArticlesViewModel @Inject constructor() : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Simulate a data source
    private val _allArticles = MutableStateFlow(getFakeArticles())

    // Debounce search query to avoid too many recompositions/filtering operations while typing
    @OptIn(FlowPreview::class)
    val filteredArticles: StateFlow<List<Article>> =
        searchQuery
            .debounce(300) // Debounce for 300ms
            .combine(_allArticles) { query, articles ->
                if (query.isBlank()) {
                    articles
                } else {
                    articles.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.contentSnippet.contains(query, ignoreCase = true)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // 5 seconds
                initialValue = _allArticles.value
            )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }


}