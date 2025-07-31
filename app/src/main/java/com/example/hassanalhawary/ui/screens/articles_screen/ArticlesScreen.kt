package com.example.hassanalhawary.ui.screens.articles_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val searchQuery by articlesViewModel.searchQuery.collectAsState()
    val articles by articlesViewModel.filteredArticles.collectAsState()
    val focusManager = LocalFocusManager.current

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
        if (articles.isEmpty() && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No articles found matching your search.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No articles available at the moment.",
                    style = MaterialTheme.typography.bodyLarge
                )
                // You could add a CircularProgressIndicator here if you were actually loading
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    ArticleItem(
                        article = article,
                        onReadMoreClicked = { onNavigateToArticleDetail(article.id) },
                        formatDate = { date -> formatDate(date) }
                    )
                }
            }
        }
    }
}