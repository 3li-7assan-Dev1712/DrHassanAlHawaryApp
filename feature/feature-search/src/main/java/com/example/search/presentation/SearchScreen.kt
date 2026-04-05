package com.example.search.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.animation.LoadingScreen
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.SearchResultMetaData
import com.example.search.presentation.components.ArticleResultCard
import com.example.search.presentation.components.AudioResultCard
import com.example.search.presentation.components.DefaultResultCard
import com.example.search.presentation.components.IdleStateScreen
import com.example.search.presentation.components.MediaResultCard
import com.example.search.presentation.components.SearchBar
import com.example.search.presentation.mapper.parseHit
import com.example.search.presentation.model.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToDetail: (SearchResultMetaData) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    SearchScreenContent(
        modifier = modifier,
        searchQuery = query,
        onQueryChanged = { query = it },
        onSearchClicked = { viewModel.search(query) },
        selectedFilter = selectedFilter,
        onFilterSelected = { viewModel.onFilterSelected(it) },
        state = state,
        onNavigateToDetail = onNavigateToDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    selectedFilter: SearchFilter,
    onFilterSelected: (SearchFilter) -> Unit,
    state: SearchUiState,
    onNavigateToDetail: (SearchResultMetaData) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(top = 16.dp)
    ) {
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            searchQuery = searchQuery,
            onQueryChanged = onQueryChanged,
            onSearchClicked = onSearchClicked,
            hint = stringResource(R.string.search_hint),
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SearchFilter.entries) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(text = filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is SearchUiState.Idle -> {
                    IdleStateScreen()
                }

                is SearchUiState.Loading -> {
                    LoadingScreen()
                }

                is SearchUiState.Success -> {
                    if (state.results.hits.isEmpty()) {
                        Text(
                            text = stringResource(R.string.error_msg),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.results.hits,
                                key = { hit -> parseHit(hit).objectID }
                            ) { hit ->
                                val parsedHit = parseHit(hit)

                                when (parsedHit.type) {
                                    "article" -> ArticleResultCard(
                                        hit = parsedHit,
                                        modifier = Modifier,
                                        onItemClick = onNavigateToDetail
                                    )

                                    "video" -> MediaResultCard(hit = parsedHit, onItemClick = onNavigateToDetail)
                                    "audio" -> AudioResultCard(hit = parsedHit, onItemClick = onNavigateToDetail)
                                    "image_group" -> MediaResultCard(hit = parsedHit, onItemClick = onNavigateToDetail)
                                    else -> {
                                        DefaultResultCard(hit = parsedHit)
                                    }
                                }
                            }
                        }
                    }
                }

                is SearchUiState.Error -> {
                    Text(
                        text = "حدث خطأ أثناء البحث. يرجى المحاولة مرة أخرى.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_7, name = "شاشة البحث")
@Composable
fun SearchScreenPreview() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                SearchScreenContent(
                    searchQuery = "العقيدة",
                    onQueryChanged = {},
                    onSearchClicked = {},
                    selectedFilter = SearchFilter.ALL,
                    onFilterSelected = {},
                    state = SearchUiState.Idle,
                    onNavigateToDetail = {}
                )
            }
        }
    }
}
