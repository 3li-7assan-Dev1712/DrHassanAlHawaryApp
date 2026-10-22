package com.example.feature.image.presentation.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.SERIES_HEADER_KEY
import com.example.core.ui.components.SERIES_STATUS_KEY
import com.example.core.ui.components.SeriesHeader
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.SeriesStatus
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.ImageGroup
import com.example.feature.image.presentation.components.DesignTile
import com.example.feature.image.presentation.list.DesignSeriesPreviewData
import com.example.feature.image.presentation.list.DesignsPreviewData
import com.example.feature.image.presentation.list.DesignsTablet

/**
 * A series of designs ("رمضانيات"): its header, then its designs by episode, each tile with
 * its number, in the designs screen's grids: Compact, the phone's two-column staggered grid;
 * Medium and Expanded, tiles at their designed 156dp, centred. A tap opens the viewer.
 */
@Composable
fun DesignSeriesScreen(
    onGroupClick: (groupId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DesignSeriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DesignSeriesContent(
        state = state,
        // Only for the count badge; loaded lazily per visible tile.
        imageCount = { groupId ->
            val images by viewModel.imagesForGroup(groupId).collectAsStateWithLifecycle()
            images.size
        },
        onGroupClick = onGroupClick,
        onNavigateBack = onNavigateBack,
    )
}

/** [DesignSeriesScreen] without the ViewModel (previews use it too). */
@Composable
fun DesignSeriesContent(
    state: SeriesScreenState<ImageGroup>,
    imageCount: @Composable (groupId: String) -> Int,
    onGroupClick: (groupId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val tokens = layoutTokens
    val staggeredState = rememberLazyStaggeredGridState()
    val gridState = rememberLazyGridState()
    // The header's title shows in the top bar once the header has scrolled away.
    val titleVisible by remember(tokens.isCompact) {
        derivedStateOf {
            if (tokens.isCompact) staggeredState.firstVisibleItemIndex > 0 else gridState.firstVisibleItemIndex > 0
        }
    }
    val emptyText = stringResource(R.string.series_empty)
    Scaffold(
        containerColor = Brand.colors.background,
        topBar = { AppTopBar(title = state.series?.title.orEmpty(), onBack = onNavigateBack, titleVisible = titleVisible) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = if (tokens.isExpanded) DesignsTablet.ExpandedTopGap else 0.dp),
        ) {
            val tile: @Composable (Int, ImageGroup) -> Unit = { index, group ->
                DesignTile(
                    group = group,
                    imageCount = imageCount(group.id),
                    onClick = { onGroupClick(group.id) },
                    episodeNumber = group.episodeNumber ?: (index + 1L),
                )
            }
            if (tokens.isCompact) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = staggeredState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 16.dp,
                ) {
                    state.series?.let { series ->
                        item(key = SERIES_HEADER_KEY, span = StaggeredGridItemSpan.FullLine) { SeriesHeader(series) }
                    }
                    if (state.phase == ContentPhase.Content) {
                        itemsIndexed(state.items, key = { _, group -> group.id }) { index, group -> tile(index, group) }
                    } else {
                        item(key = SERIES_STATUS_KEY, span = StaggeredGridItemSpan.FullLine) { SeriesStatus(state.phase, emptyText) }
                    }
                }
            } else {
                val gap = if (tokens.isExpanded) DesignsTablet.ExpandedGap else DesignsTablet.MediumGap
                LazyVerticalGrid(
                    columns = GridCells.FixedSize(DesignsTablet.TileWidth),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = gap),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    state.series?.let { series ->
                        item(key = SERIES_HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) { SeriesHeader(series) }
                    }
                    if (state.phase == ContentPhase.Content) {
                        itemsIndexed(state.items, key = { _, group -> group.id }) { index, group -> tile(index, group) }
                    } else {
                        item(key = SERIES_STATUS_KEY, span = { GridItemSpan(maxLineSpan) }) { SeriesStatus(state.phase, emptyText) }
                    }
                }
            }
        }
    }
}

private val SeriesPreviewState = SeriesScreenState(
    series = DesignSeriesPreviewData,
    items = DesignsPreviewData.mapIndexed { index, group ->
        group.copy(seriesId = DesignSeriesPreviewData.id, episodeNumber = index + 1L)
    },
    isRefreshing = false,
)

@Composable
private fun DesignSeriesPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme, mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin) {
        DesignSeriesContent(
            state = SeriesPreviewState,
            imageCount = { if (it.toInt() % 3 == 0) 4 else 1 },
            onGroupClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Design series - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun DesignSeriesCompactLightPreview() = DesignSeriesPreview(darkTheme = false)

@Preview(name = "Design series - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun DesignSeriesCompactDarkPreview() = DesignSeriesPreview(darkTheme = true)

@Preview(name = "Design series - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun DesignSeriesMediumLightPreview() = DesignSeriesPreview(darkTheme = false)

@Preview(name = "Design series - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun DesignSeriesMediumDarkPreview() = DesignSeriesPreview(darkTheme = true)

@Preview(name = "Design series - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun DesignSeriesExpandedLightPreview() = DesignSeriesPreview(darkTheme = false)

@Preview(name = "Design series - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun DesignSeriesExpandedDarkPreview() = DesignSeriesPreview(darkTheme = true)
