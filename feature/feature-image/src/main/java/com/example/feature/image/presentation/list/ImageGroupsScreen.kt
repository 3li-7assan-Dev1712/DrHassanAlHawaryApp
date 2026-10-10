package com.example.feature.image.presentation.list

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.animateStaggeredGridItem
import com.example.core.ui.theme.reducedMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ImageNotSupported
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.theme.animateGridItem
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.ImageGroup
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.Illustration
import com.example.core.ui.theme.Brand
import com.example.feature.image.presentation.components.DesignTile
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesGroupsScreen(
    viewModel: ImagesGroupsViewModel = hiltViewModel(),
    onGroupClick: (groupId: String) -> Unit,
    onImageClick: (groupId: String, index: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val lazyPagingItems = viewModel.imageGroups.collectAsLazyPagingItems()

    DesignsScreenContent(
        groups = lazyPagingItems,
        // Only for the count badge; loaded lazily per visible tile.
        imageCount = { groupId ->
            val images by viewModel.imagesForGroup(groupId).collectAsStateWithLifecycle()
            images.size
        },
        onGroupClick = onGroupClick,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * The designs without the ViewModel (previews and UI tests use it too). Compact: the phone's
 * two-column staggered grid. Medium and Expanded: tiles at their designed 156dp, rows
 * aligned and centred (Figma `60:1597`: 6 across, 24 apart; `64:4089`: 4 across, 16 apart).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignsScreenContent(
    groups: LazyPagingItems<ImageGroup>,
    imageCount: @Composable (groupId: String) -> Int,
    onGroupClick: (groupId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val lazyPagingItems = groups
    val tokens = layoutTokens
    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.images), onBack = onNavigateBack) },
        containerColor = Brand.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = if (tokens.isExpanded) DesignsTablet.ExpandedTopGap else 0.dp)
        ) {
            val phase = when (lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> ContentPhase.Loading
                is LoadState.Error -> ContentPhase.Error
                is LoadState.NotLoading ->
                    if (lazyPagingItems.itemCount == 0) ContentPhase.Empty else ContentPhase.Content
            }
            val reduced = reducedMotion
            AnimatedContent(
                targetState = phase,
                transitionSpec = { Motion.contentSwap(reduced) },
                label = "designsContent",
            ) { shown ->
                when (shown) {
                    ContentPhase.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                color = Brand.colors.accentStrong
                            )
                        }
                    }
    
                    ContentPhase.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            EmptyState(
                                illustration = Illustration.ComputerAndServer,
                                title = stringResource(R.string.empty_no_connection),
                            )
                        }
                    }
    
                    ContentPhase.Empty, ContentPhase.Content -> {
                        if (shown == ContentPhase.Empty) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "لا توجد مجموعات تصاميم حاليًا.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Brand.colors.textMuted
                                )
                            }
                        } else if (!tokens.isCompact) {
                            DesignsGrid(groups = lazyPagingItems, imageCount = imageCount, onGroupClick = onGroupClick)
                        } else {
                            // Designs come in many shapes: a staggered grid keeps each image's
                            // own proportions instead of cropping them all to one ratio.
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 16.dp,
                            ) {
                                items(
                                    count = lazyPagingItems.itemCount,
                                    key = lazyPagingItems.itemKey { it.id }
                                ) { index ->
                                    val group = lazyPagingItems[index]
                                    if (group != null) {
                                        Box(animateStaggeredGridItem()) {
                                            DesignTile(
                                                group = group,
                                                imageCount = imageCount(group.id),
                                                onClick = { onGroupClick(group.id) },
                                            )
                                        }
                                    }
                                }
    
                                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Brand.colors.accentStrong)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tablet designs, measured in Figma (`60:1597` Expanded, `64:4089` Medium). */
private object DesignsTablet {
    /** DesignTile's designed width: the tiles keep it and the grid is centred. */
    val TileWidth = 156.dp
    val ExpandedGap = 24.dp
    val MediumGap = 16.dp
    /** Expanded: top bar to grid (Medium: none). */
    val ExpandedTopGap = 24.dp
}

/**
 * As many [DesignsTablet.TileWidth] columns as fit (6 on Expanded, 4 on Medium), centred,
 * rows aligned (each tile keeps its image's proportions inside the row).
 */
@Composable
private fun DesignsGrid(
    groups: LazyPagingItems<ImageGroup>,
    imageCount: @Composable (groupId: String) -> Int,
    onGroupClick: (groupId: String) -> Unit,
) {
    val gap = if (layoutTokens.isExpanded) DesignsTablet.ExpandedGap else DesignsTablet.MediumGap
    LazyVerticalGrid(
        columns = GridCells.FixedSize(DesignsTablet.TileWidth),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = gap),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        items(count = groups.itemCount, key = groups.itemKey { it.id }) { index ->
            val group = groups[index]
            if (group != null) {
                Box(animateGridItem()) {
                    DesignTile(group = group, imageCount = imageCount(group.id), onClick = { onGroupClick(group.id) })
                }
            }
        }
        if (groups.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Brand.colors.accentStrong)
                }
            }
        }
    }
}

/** Sample design groups for previews and UI tests (no images: tiles show their placeholder). */
val DesignsPreviewData: List<ImageGroup> = List(12) { index ->
    ImageGroup(id = "$index", title = "بطاقة دعوية ${index + 1}", previewImageUrl = "")
}

/** [DesignsPreviewData] as fully loaded paging data. */
fun designsPreviewPagingData(): Flow<PagingData<ImageGroup>> = flowOf(
    PagingData.from(
        DesignsPreviewData,
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)

@Composable
private fun DesignsPreview(darkTheme: Boolean) {
    val groups = remember { designsPreviewPagingData() }.collectAsLazyPagingItems()
    AdaptiveShellPreview(darkTheme = darkTheme, mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin) {
        DesignsScreenContent(groups = groups, imageCount = { if (it.toInt() % 3 == 0) 4 else 1 }, onGroupClick = {}, onNavigateBack = {})
    }
}

@Preview(name = "Designs - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun DesignsCompactLightPreview() = DesignsPreview(darkTheme = false)

@Preview(name = "Designs - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun DesignsCompactDarkPreview() = DesignsPreview(darkTheme = true)

@Preview(name = "Designs - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun DesignsMediumLightPreview() = DesignsPreview(darkTheme = false)

@Preview(name = "Designs - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun DesignsMediumDarkPreview() = DesignsPreview(darkTheme = true)

@Preview(name = "Designs - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun DesignsExpandedLightPreview() = DesignsPreview(darkTheme = false)

@Preview(name = "Designs - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun DesignsExpandedDarkPreview() = DesignsPreview(darkTheme = true)
