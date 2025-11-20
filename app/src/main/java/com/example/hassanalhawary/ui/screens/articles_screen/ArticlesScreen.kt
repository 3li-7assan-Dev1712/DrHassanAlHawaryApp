package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.domain.module.Article
import com.example.hassanalhawary.R
import com.example.hassanalhawary.core.util.formatDate
import com.example.hassanalhawary.ui.components.SearchBar
import com.example.hassanalhawary.ui.components.TopAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    articlesViewModel: ArticlesViewModel = hiltViewModel(),
    onNavigateToArticleDetail: (articleId: String) -> Unit // Callback to navigate
) {


    val articles = articlesViewModel.articles.collectAsLazyPagingItems()
    val searchQuery by articlesViewModel.rawSearchInput.collectAsState()

//    val articles by articlesViewModel.filteredArticles.collectAsState()
    val focusManager = LocalFocusManager.current

    ArticlesScreenContent(
        articles,
        searchQuery,
        articlesViewModel,
        focusManager,
        onNavigateToArticleDetail
    )
}

@Composable
private fun ArticlesScreenContent(
    articles: LazyPagingItems<Article>,
    searchQuery: String,
    articlesViewModel: ArticlesViewModel,
    focusManager: FocusManager,
    onNavigateToArticleDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Column { // Use Column to stack TopAppBar and SearchBar
                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    title = stringResource(R.string.articles)
                )
                SearchBar(
                    searchQuery = searchQuery,
                    hint = stringResource(R.string.search_hint),
                    onQueryChanged = { articlesViewModel.onSearchQueryChanged(it) },
                    onSearchClicked = {
                        focusManager.clearFocus() // Hide keyboard on search
                        //c can trigger a more explicit search action here if debounce wasn't enough
                    })
            }
        }
    ) { innerPadding ->
        // Handle the initial loading state for the whole screen

        val isMediatorRefreshing = articles.loadState.mediator?.refresh is LoadState.Loading


        if (isMediatorRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Once the initial load is done, show arts
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    count = articles.itemCount,
                    key = articles.itemKey { it.id }
                ) { index ->
                    val art = articles[index]
                    if (art != null) {
                        ArticleItem(
                            article = art,
                            onReadMoreClicked = { onNavigateToArticleDetail(art.id) },
                            formatDate = { date -> formatDate(date) }
                        )
                    }
                }

                // when scroll down show loading will appending new arts
                if (articles.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

    }

}