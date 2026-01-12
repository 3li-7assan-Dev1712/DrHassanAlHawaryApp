package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.core.ui.R
import com.example.domain.module.Article
import com.example.feature.article.data.util.formatDate
import com.example.feature.article.presentation.components.SearchBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    articlesViewModel: ArticleListViewModel = hiltViewModel(),
    onNavigateToArticleDetail: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit
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
        onNavigateToArticleDetail,
        onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesScreenContent(
    articles: LazyPagingItems<Article>,
    searchQuery: String,
    articleListViewModel: ArticleListViewModel,
    focusManager: FocusManager,
    onNavigateToArticleDetail: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {

    val listState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            Column { // Use Column to stack TopAppBar and SearchBar
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    title = {
                        Text(
                            text = stringResource(R.string.articles),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                        )

                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                )
                SearchBar(
                    searchQuery = searchQuery,
                    hint = stringResource(R.string.search_hint),
                    onQueryChanged = { articleListViewModel.onSearchQueryChanged(it) },
                    onSearchClicked = {
                        focusManager.clearFocus() // Hide keyboard on search
                        //c can trigger a more explicit search action here if debounce wasn't enough
                    })
            }
        }
    ) { innerPadding ->


        val isInitialLoad = articles.loadState.refresh is LoadState.Loading

        if (isInitialLoad) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(
                state = listState,
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