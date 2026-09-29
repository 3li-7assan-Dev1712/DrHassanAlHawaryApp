package com.example.feature.article.presentation.list

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.animateListItem
import com.example.core.ui.theme.reducedMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.theme.Brand
import com.example.domain.module.Article
import com.example.feature.article.presentation.components.ArticleItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    articlesViewModel: ArticleListViewModel = hiltViewModel(),
    onNavigateToArticleDetail: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit
) {


    val articles = articlesViewModel.articles.collectAsLazyPagingItems()

    ArticlesScreenContent(
        articles,
        onNavigateToArticleDetail,
        onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesScreenContent(
    articles: LazyPagingItems<Article>,
    onNavigateToArticleDetail: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {

    val listState = rememberLazyListState()

    // Paging's local-cache-first load can insert newer items (the mediator's remote
    // fetch, or a genuinely new article) above whatever the list last anchored on,
    // which otherwise leaves the list looking "scrolled down" without ever having
    // moved. Track the newest item's key and snap/animate back to it when it changes.
    var topItemKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(articles.itemCount) {
        if (articles.itemCount == 0) return@LaunchedEffect
        val newestKey = articles.peek(0)?.id
        if (newestKey != null && newestKey != topItemKey) {
            if (topItemKey == null) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(0)
            }
            topItemKey = newestKey
        }
    }

    Scaffold(
        containerColor = Brand.colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.articles), onBack = onNavigateBack) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            val isInitialLoad = articles.loadState.refresh is LoadState.Loading

            val reduced = reducedMotion
            AnimatedContent(
                targetState = isInitialLoad,
                transitionSpec = { Motion.contentSwap(reduced) },
                label = "articlesContent",
            ) { loading ->
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Brand.colors.accentStrong,
                            strokeWidth = 3.dp
                        )
                    }
                } else {
    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            count = articles.itemCount,
                            key = articles.itemKey { it.id }
                        ) { index ->
                            val art = articles[index]
                            if (art != null) {
                                Box(animateListItem()) {
                                    ArticleItem(
                                        article = art,
                                        onClick = { onNavigateToArticleDetail(art.id) },
                                    )
                                }
                            }
                        }
    
                        // when scroll down show loading will append new arts
                        if (articles.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp,
                                        color = Brand.colors.accentStrong
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
