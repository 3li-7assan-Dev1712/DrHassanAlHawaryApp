package com.example.search.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.animatedPillStyle
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.Illustration
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.animateListItem
import com.example.core.ui.theme.reducedMotion
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.SearchResultMetaData
import com.example.domain.text.ArabicNumerals
import com.example.search.presentation.components.SearchBar
import com.example.search.presentation.components.SearchResultRow
import com.example.search.presentation.components.toMetaData
import com.example.search.presentation.mapper.parseHit
import com.example.search.presentation.model.SearchHit
import com.example.search.presentation.model.SearchUiState

/** Static topic suggestions for the empty state; tapping one searches it. */
private val SUGGESTED_TOPICS = listOf("الزكاة", "الصيام", "الحج", "البيوع", "الأسرة")

/** How many hits each group shows under "الكل" before "عرض الكل". */
private const val GROUP_PREVIEW = 3

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToDetail: (SearchResultMetaData) -> Unit
) {
    val query by viewModel.query.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val typeCounts by viewModel.typeCounts.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    SearchScreenContent(
        modifier = modifier,
        searchQuery = query,
        onQueryChanged = viewModel::onQueryChange,
        onSearchClicked = { viewModel.submit() },
        onSuggestionClicked = { viewModel.submit(it) },
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::onFilterSelected,
        typeCounts = typeCounts,
        recentSearches = recentSearches,
        onClearRecent = viewModel::clearRecentSearches,
        state = state,
        onNavigateToDetail = {
            viewModel.onResultOpened()
            onNavigateToDetail(it)
        },
    )
}

@Composable
fun SearchScreenContent(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onSuggestionClicked: (String) -> Unit,
    selectedFilter: SearchFilter,
    onFilterSelected: (SearchFilter) -> Unit,
    typeCounts: Map<String, Int>,
    recentSearches: List<String>,
    onClearRecent: () -> Unit,
    state: SearchUiState,
    onNavigateToDetail: (SearchResultMetaData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 0.dp),
    ) {
        AppTopBar(title = stringResource(R.string.search_title))
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            searchQuery = searchQuery,
            onQueryChanged = onQueryChanged,
            onSearchClicked = onSearchClicked,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SearchFilter.entries) { filter ->
                FilterPill(
                    label = filter.label,
                    count = typeCounts[filter.type],
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                )
            }
        }

        val reduced = reducedMotion
        AnimatedContent(
            targetState = state,
            transitionSpec = { Motion.contentSwap(reduced) },
            // Crossfade only when the kind of content changes; new results for the same kind
            // rearrange through the list items instead.
            contentKey = { s ->
                when (s) {
                    is SearchUiState.Success -> if (s.results.hits.isEmpty()) "empty" else "results"
                    else -> s::class
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "searchContent",
        ) { state ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is SearchUiState.Idle -> SearchIdleContent(
                        recentSearches = recentSearches,
                        onSuggestionClicked = onSuggestionClicked,
                        onClearRecent = onClearRecent,
                    )
    
                    is SearchUiState.TooShort -> CenteredMessage(stringResource(R.string.search_too_short))
    
                    is SearchUiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.accentStrong,
                    )
    
                    is SearchUiState.Success -> {
                        val hits = remember(state.results) { state.results.hits.map(::parseHit) }
                        if (hits.isEmpty()) {
                            EmptyState(
                                illustration = Illustration.Document,
                                title = stringResource(R.string.empty_no_results),
                                body = stringResource(R.string.empty_try_another_word),
                                modifier = Modifier.align(Alignment.Center),
                            )
                        } else {
                            SearchResults(
                                hits = hits,
                                query = searchQuery,
                                grouped = selectedFilter == SearchFilter.ALL,
                                typeCounts = typeCounts,
                                onFilterSelected = onFilterSelected,
                                onNavigateToDetail = onNavigateToDetail,
                            )
                        }
                    }
    
                    is SearchUiState.Error -> EmptyState(
                        illustration = Illustration.ComputerAndServer,
                        title = stringResource(R.string.empty_no_connection),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    hits: List<SearchHit>,
    query: String,
    grouped: Boolean,
    typeCounts: Map<String, Int>,
    onFilterSelected: (SearchFilter) -> Unit,
    onNavigateToDetail: (SearchResultMetaData) -> Unit,
) {
    val groups = remember(hits, grouped) {
        if (!grouped) emptyList()
        else SearchFilter.entries.filter { it != SearchFilter.ALL }
            .mapNotNull { filter -> hits.filter { it.type == filter.type }.takeIf { it.isNotEmpty() }?.let { filter to it } }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!grouped) {
            items(hits, key = { it.objectID }) { hit ->
                SearchResultRow(
                        hit = hit,
                        query = query,
                        onClick = { onNavigateToDetail(hit.toMetaData()) },
                        modifier = animateListItem(),
                    )
            }
        } else {
            groups.forEach { (filter, groupHits) ->
                item(key = "header-${filter.type}") {
                    GroupHeader(
                        modifier = animateListItem(),
                        label = filter.label,
                        count = typeCounts[filter.type] ?: groupHits.size,
                        onViewAll = { onFilterSelected(filter) },
                    )
                }
                items(groupHits.take(GROUP_PREVIEW), key = { it.objectID }) { hit ->
                    SearchResultRow(
                        hit = hit,
                        query = query,
                        onClick = { onNavigateToDetail(hit.toMetaData()) },
                        modifier = animateListItem(),
                    )
                }
            }
        }
    }
}

/** "صوتيات · ٩" with "عرض الكل" at the end, which selects that type's chip. */
@Composable
private fun GroupHeader(
    label: String,
    count: Int,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label${ArabicNumerals.DATE_SEPARATOR}${ArabicNumerals.digits(count)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onViewAll) {
            Text(stringResource(R.string.search_view_all), color = colors.accentText, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit, count: Int? = null) {
    val style = animatedPillStyle(selected)
    val reduced = reducedMotion
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = style.fill,
        border = style.border,
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = style.content)
            if (count != null) {
                Spacer(Modifier.width(4.dp))
                // The count slides up to its new value when a search changes it.
                AnimatedContent(
                    targetState = count,
                    transitionSpec = { Motion.countSlide(reduced, up = targetState > initialState) },
                    label = "pillCount",
                ) { shown ->
                    Text(
                        text = ArabicNumerals.digits(shown),
                        style = MaterialTheme.typography.labelLarge,
                        color = style.content,
                    )
                }
            }
        }
    }
}

/** Empty state: suggested topics, then up to 8 recent searches with "مسح". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchIdleContent(
    recentSearches: List<String>,
    onSuggestionClicked: (String) -> Unit,
    onClearRecent: () -> Unit,
) {
    val colors = Brand.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.search_suggested),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SUGGESTED_TOPICS.forEach { topic ->
                FilterPill(label = topic, selected = false, onClick = { onSuggestionClicked(topic) })
            }
        }
        if (recentSearches.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.search_recent),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearRecent) {
                    Text(stringResource(R.string.search_clear_recent), color = colors.accentText)
                }
            }
            recentSearches.take(8).forEach { recent ->
                Surface(onClick = { onSuggestionClicked(recent) }, color = colors.background) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(TablerIcons.Clock),
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = recent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Brand.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun SearchPreviewContent(query: String, state: SearchUiState) {
    SearchScreenContent(
        searchQuery = query,
        onQueryChanged = {},
        onSearchClicked = {},
        onSuggestionClicked = {},
        selectedFilter = SearchFilter.ALL,
        onFilterSelected = {},
        typeCounts = emptyMap(),
        recentSearches = listOf("أحكام الزكاة", "صيام الست"),
        onClearRecent = {},
        state = state,
        onNavigateToDetail = {},
        modifier = Modifier,
    )
}

@Preview(name = "Search idle - light", locale = "ar", widthDp = 360, heightDp = 640, showBackground = true, backgroundColor = 0xFFF4EEE5)
@Composable
private fun SearchIdleLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { SearchPreviewContent("", SearchUiState.Idle) }
}

@Preview(name = "Search too short - dark", locale = "ar", widthDp = 360, heightDp = 640, showBackground = true, backgroundColor = 0xFF1A1512)
@Composable
private fun SearchShortDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { SearchPreviewContent("ال", SearchUiState.TooShort) }
}
