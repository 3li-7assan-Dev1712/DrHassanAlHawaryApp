package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.data.ArticlesRepositoryImpl
import com.example.domain.module.Article
import com.example.domain.use_cases.GetArticlesFromDbUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val getAllArticlesFromDbUseCase: GetArticlesFromDbUseCase,

    articlesRepository: ArticlesRepositoryImpl
) : ViewModel() {



    private val _rawSearchInput = MutableStateFlow("")
    val rawSearchInput = _rawSearchInput.asStateFlow()

    private val _debouncedSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<PagingData<Article>> = _rawSearchInput
        .debounce(300L) // Debounce to avoid querying on every keystroke
        .distinctUntilChanged() // Only query if the text actually changed
        .flatMapLatest { query -> //  flatMapLatest to switch to the new Flow from the use case
            articlesRepository.getArticlesPagingData(query)
        }
        .cachedIn(viewModelScope)

    /*init {
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
            getAllArticlesFromDbUseCase(_articlesUiState.value.searchQuery).collect { arts ->

                _articlesUiState.value = ArticlesUiState.Success(arts.toList())

            }
        }
    }*/


    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _rawSearchInput.value = query
        }
    }


}
