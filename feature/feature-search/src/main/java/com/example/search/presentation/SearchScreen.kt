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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface
            )
    ) {

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            searchQuery = query,
            onQueryChanged = { 
                query = it
            },
            onSearchClicked = {
                viewModel.search(query)
            },
            hint = stringResource(R.string.search_hint),
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SearchFilter.entries) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.onFilterSelected(filter) },
                    label = { Text(text = filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
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
                    val successState = state as SearchUiState.Success
                    if (successState.results.hits.isEmpty()) {
                        Text(text = stringResource(R.string.error_msg))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = successState.results.hits,
                                key = { hit ->
                                    parseHit(hit).objectID
                                }
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
                        text = "Error: ${(state as SearchUiState.Error).throwable.message}"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    HassanAlHawaryTheme {
        SearchScreen(
            onNavigateToDetail = {}
        )
    }
}
