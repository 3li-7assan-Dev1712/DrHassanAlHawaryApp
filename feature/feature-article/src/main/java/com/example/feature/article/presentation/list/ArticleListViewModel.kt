package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Article
import com.example.feature.article.domain.use_case.GetPaginatedArticlesUseCase
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
class ArticleListViewModel @Inject constructor(

    getPaginatedArticlesUseCase: GetPaginatedArticlesUseCase
) : ViewModel() {


    private val _rawSearchInput = MutableStateFlow("")
    val rawSearchInput = _rawSearchInput.asStateFlow()

    private val _debouncedSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<PagingData<Article>> = _rawSearchInput
        .debounce(300L) // Debounce to avoid querying on every keystroke
        .distinctUntilChanged() // Only query if the text actually changed
        .flatMapLatest { query -> //  flatMapLatest to switch to the new Flow from the use case
            getPaginatedArticlesUseCase(query)
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _rawSearchInput.value = query
        }
    }


}
