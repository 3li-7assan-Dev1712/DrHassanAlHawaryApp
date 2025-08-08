package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.use_cases.FilterArticlesUseCase
import com.example.hassanalhawary.domain.use_cases.GetAllArticlesUseCase
import com.example.hassanalhawary.ui.screens.audio_list_sceen.AudioListViewModel.Companion.SEARCH_DEBOUNCE_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val getAllArticlesUseCase: GetAllArticlesUseCase,
    private val filterArticlesUseCase: FilterArticlesUseCase
) : ViewModel() {


    private val _articlesUiState = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Loading)
    val articlesUiState: StateFlow<ArticlesUiState> = _articlesUiState.asStateFlow()


    private val _rawSearchInput = MutableStateFlow("")

    private val _debouncedSearchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {


            _rawSearchInput
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged() // Only proceed if the query actually changed after debounce
                .collectLatest { debouncedQuery -> // Use collectLatest to cancel previous filtering if a new query comes in
                    _debouncedSearchQuery.value =
                        debouncedQuery // Update the debounced query holder
                    _articlesUiState.update {
                        when (it) {
                            is ArticlesUiState.Success -> {
                                it.copy(
                                    displayedArticles = filterArticlesUseCase(
                                        articles = it.allArticles,
                                        query = debouncedQuery
                                    )
                                )
                            }

                            else -> it
                        }
                    }
                }

        }
        viewModelScope.launch {
            val articlesResult = getAllArticlesUseCase()
            if (articlesResult.articles != null) {
                _articlesUiState.value = ArticlesUiState.Success(articlesResult.articles)
            } else {
                _articlesUiState.value = ArticlesUiState.Error(articlesResult.errorMessage)
            }
        }
    }


    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {

            _rawSearchInput.value = query

            _articlesUiState.update {
                when (it) {
                    is ArticlesUiState.Success -> {
                        it.copy(
                            searchQuery = query,
                            displayedArticles = filterArticlesUseCase(
                                articles = it.allArticles,
                                query = _debouncedSearchQuery.value
                            )
                        )
                    }

                    else -> it
                }
            }
        }
    }


}