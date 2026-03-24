package com.example.feature.article.presentation.list

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
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class ArticleListViewModel @Inject constructor(

    getPaginatedArticlesUseCase: GetPaginatedArticlesUseCase
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<PagingData<Article>> =
        getPaginatedArticlesUseCase("").cachedIn(viewModelScope)


}
